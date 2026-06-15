package com.fitting.orderservice.service.impl;

import com.fitting.orderservice.client.InventoryClient;
import com.fitting.orderservice.client.ProductClient;
import com.fitting.orderservice.dto.OrderItemRequest;
import com.fitting.orderservice.dto.OrderRequest;
import com.fitting.orderservice.dto.OrderItemResponse;
import com.fitting.orderservice.dto.OrderResponse;
import com.fitting.orderservice.entity.Order;
import com.fitting.orderservice.entity.OrderItem;
import com.fitting.orderservice.entity.OrderStatus;
import com.fitting.orderservice.exception.BusinessException;
import com.fitting.orderservice.exception.ResourceNotFoundException;
import com.fitting.orderservice.repository.OrderRepository;
import com.fitting.orderservice.service.OrderService;
import com.fitting.orderservice.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductClient productClient;
    private final InventoryClient inventoryClient;

    // Secuencia simple para el número de orden (académico)
    private static final AtomicLong sequence = new AtomicLong(1);

    // ── Crear orden ─────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public OrderResponse create(OrderRequest request) {
        log.info("Creando orden para cliente: {}", request.getCustomerEmail());

        List<OrderItem> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        // 1. Validar productos y disponibilidad de stock
        for (OrderItemRequest itemReq : request.getItems()) {
            log.debug("Validando producto ID: {}", itemReq.getProductId());

            // Obtener datos del producto desde product-service
            Map<String, Object> productData = fetchProduct(itemReq.getProductId());
            String productName = (String) productData.get("name");
            BigDecimal unitPrice = new BigDecimal(productData.get("price").toString());

            // Verificar disponibilidad en inventory-service
            boolean available = checkAvailability(itemReq.getProductId(), itemReq.getQuantity());
            if (!available) {
                throw new BusinessException(String.format(
                        "Stock insuficiente para el producto '%s' (ID: %d). Cantidad solicitada: %d",
                        productName, itemReq.getProductId(), itemReq.getQuantity()));
            }

            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            total = total.add(subtotal);

            items.add(OrderItem.builder()
                    .productId(itemReq.getProductId())
                    .productName(productName)
                    .quantity(itemReq.getQuantity())
                    .unitPrice(unitPrice)
                    .subtotal(subtotal)
                    .build());
        }

        // 2. Persistir la orden
        Order order = Order.builder()
                .orderNumber(generateOrderNumber())
                .customerName(request.getCustomerName())
                .customerEmail(request.getCustomerEmail())
                .shippingAddress(request.getShippingAddress())
                .totalAmount(total)
                .status(OrderStatus.PENDING)
                .build();

        // Asociar items a la orden
        items.forEach(item -> item.setOrder(order));
        order.getItems().addAll(items);

        Order saved = orderRepository.save(order);

        // 3. Reservar stock en inventory-service para cada item
        for (OrderItem item : saved.getItems()) {
            reserveStock(item.getProductId(), item.getQuantity(), saved.getOrderNumber());
        }

        log.info("Orden creada: {} - Total: {}", saved.getOrderNumber(), saved.getTotalAmount());
        return toResponse(saved);
    }

    // ── Consultas ───────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public OrderResponse findById(Long id) {
        log.debug("Buscando orden con ID: {}", id);
        return toResponse(getOrderOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse findByOrderNumber(String orderNumber) {
        log.debug("Buscando orden: {}", orderNumber);
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Orden con número " + orderNumber + " no encontrada"));
        return toResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> findAll() {
        log.debug("Listando todas las órdenes");
        return orderRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> findByCustomerEmail(String email) {
        log.debug("Buscando órdenes del cliente: {}", email);
        return orderRepository.findByCustomerEmail(email).stream()
                .map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> findByStatus(OrderStatus status) {
        log.debug("Buscando órdenes con estado: {}", status);
        return orderRepository.findByStatus(status).stream()
                .map(this::toResponse).toList();
    }

    // ── Cambio de estado ────────────────────────────────────────────────────────

    @Override
    @Transactional
    public OrderResponse updateStatus(Long id, OrderStatus newStatus) {
        log.info("Actualizando estado de orden ID {} a {}", id, newStatus);

        Order order = getOrderOrThrow(id);
        validateStatusTransition(order.getStatus(), newStatus);
        order.setStatus(newStatus);

        Order updated = orderRepository.save(order);
        log.info("Orden {} actualizada a estado: {}", updated.getOrderNumber(), newStatus);
        return toResponse(updated);
    }

    // ── Cancelar ────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void cancel(Long id) {
        log.info("Cancelando orden con ID: {}", id);

        Order order = getOrderOrThrow(id);

        if (order.getStatus() == OrderStatus.DELIVERED) {
            throw new BusinessException("No se puede cancelar una orden ya entregada");
        }
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new BusinessException("La orden ya está cancelada");
        }

        // Liberar stock reservado en inventory-service
        for (OrderItem item : order.getItems()) {
            releaseStock(item.getProductId(), item.getQuantity(), order.getOrderNumber());
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
        log.info("Orden {} cancelada. Stock liberado.", order.getOrderNumber());
    }

    // ── Helpers Feign ───────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private Map<String, Object> fetchProduct(Long productId) {
        try {
            ApiResponse<Map<String, Object>> response = productClient.getProductById(productId);
            if (response == null || !response.isSuccess() || response.getData() == null) {
                throw new BusinessException("Producto ID " + productId + " no encontrado");
            }
            return response.getData();
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Error consultando product-service para producto {}: {}",
                    productId, ex.getMessage());
            throw new BusinessException("No se pudo obtener información del producto ID: " + productId);
        }
    }

    private boolean checkAvailability(Long productId, int quantity) {
        try {
            ApiResponse<Boolean> response = inventoryClient.isAvailable(productId, quantity);
            return response != null && Boolean.TRUE.equals(response.getData());
        } catch (Exception ex) {
            log.error("Error consultando inventory-service para producto {}: {}",
                    productId, ex.getMessage());
            throw new BusinessException(
                    "No se pudo verificar el stock del producto ID: " + productId);
        }
    }

    private void reserveStock(Long productId, int quantity, String orderNumber) {
        try {
            inventoryClient.reserveStock(productId, Map.of("quantity", quantity));
            log.debug("Stock reservado - producto ID: {}, cantidad: {}, orden: {}",
                    productId, quantity, orderNumber);
        } catch (Exception ex) {
            log.error("Error reservando stock para producto {}: {}", productId, ex.getMessage());
            throw new BusinessException(
                    "Error al reservar stock del producto ID: " + productId);
        }
    }

    private void releaseStock(Long productId, int quantity, String orderNumber) {
        try {
            inventoryClient.releaseStock(productId, Map.of("quantity", quantity));
            log.debug("Stock liberado - producto ID: {}, cantidad: {}, orden: {}",
                    productId, quantity, orderNumber);
        } catch (Exception ex) {
            // No lanzamos excepción para no bloquear la cancelación
            log.error("Error liberando stock para producto {}: {}", productId, ex.getMessage());
        }
    }

    // ── Validación de transición de estados ─────────────────────────────────────

    private void validateStatusTransition(OrderStatus current, OrderStatus next) {
        boolean valid = switch (current) {
            case PENDING   -> next == OrderStatus.CONFIRMED || next == OrderStatus.CANCELLED;
            case CONFIRMED -> next == OrderStatus.SHIPPED   || next == OrderStatus.CANCELLED;
            case SHIPPED   -> next == OrderStatus.DELIVERED;
            default        -> false;
        };
        if (!valid) {
            throw new BusinessException(String.format(
                    "Transición de estado inválida: %s → %s", current, next));
        }
    }

    // ── Generador de número de orden ────────────────────────────────────────────

    private String generateOrderNumber() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String seq  = String.format("%04d", sequence.getAndIncrement());
        String candidate = "ORD-" + date + "-" + seq;

        // Garantía de unicidad
        while (orderRepository.existsByOrderNumber(candidate)) {
            candidate = "ORD-" + date + "-" + String.format("%04d", sequence.getAndIncrement());
        }
        return candidate;
    }

    // ── Helper repository ───────────────────────────────────────────────────────

    private Order getOrderOrThrow(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Orden", id));
    }

    // ── Mapper interno ──────────────────────────────────────────────────────────

    private OrderResponse toResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(item -> OrderItemResponse.builder()
                        .id(item.getId())
                        .productId(item.getProductId())
                        .productName(item.getProductName())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .subtotal(item.getSubtotal())
                        .build())
                .toList();

        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .customerName(order.getCustomerName())
                .customerEmail(order.getCustomerEmail())
                .shippingAddress(order.getShippingAddress())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .items(itemResponses)
                .build();
    }
}