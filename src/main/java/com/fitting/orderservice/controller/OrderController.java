package com.fitting.orderservice.controller;

import com.fitting.orderservice.dto.OrderRequest;
import com.fitting.orderservice.dto.OrderResponse;
import com.fitting.orderservice.entity.OrderStatus;
import com.fitting.orderservice.service.OrderService;
import com.fitting.orderservice.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Tag(name = "Órdenes", description = "Gestión del ciclo de vida de órdenes de compra")
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "Crear orden", description = "Valida stock, reserva unidades y crea la orden en estado PENDING")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Orden creada"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Stock insuficiente o producto no encontrado")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> create(
            @Valid @RequestBody OrderRequest request) {
        log.info("POST /api/v1/orders - Cliente: {}", request.getCustomerEmail());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Orden creada exitosamente",
                        orderService.create(request)));
    }

    @Operation(summary = "Listar órdenes")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lista obtenida")
    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderResponse>>> findAll() {
        log.info("GET /api/v1/orders");
        return ResponseEntity.ok(ApiResponse.ok("Lista de órdenes",
                orderService.findAll()));
    }

    @Operation(summary = "Buscar orden por ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Orden encontrada"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Orden no encontrada")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> findById(@PathVariable Long id) {
        log.info("GET /api/v1/orders/{}", id);
        return ResponseEntity.ok(ApiResponse.ok("Orden encontrada",
                orderService.findById(id)));
    }

    @Operation(summary = "Buscar por número de orden")
    @GetMapping("/number/{orderNumber}")
    public ResponseEntity<ApiResponse<OrderResponse>> findByOrderNumber(
            @PathVariable String orderNumber) {
        log.info("GET /api/v1/orders/number/{}", orderNumber);
        return ResponseEntity.ok(ApiResponse.ok("Orden encontrada",
                orderService.findByOrderNumber(orderNumber)));
    }

    @Operation(summary = "Buscar órdenes por cliente")
    @GetMapping("/customer/{email}")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> findByCustomer(
            @PathVariable String email) {
        log.info("GET /api/v1/orders/customer/{}", email);
        return ResponseEntity.ok(ApiResponse.ok("Órdenes del cliente",
                orderService.findByCustomerEmail(email)));
    }

    @Operation(summary = "Filtrar órdenes por estado")
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> findByStatus(
            @PathVariable OrderStatus status) {
        log.info("GET /api/v1/orders/status/{}", status);
        return ResponseEntity.ok(ApiResponse.ok("Órdenes por estado",
                orderService.findByStatus(status)));
    }

    @Operation(summary = "Actualizar estado de orden", description = "Ciclo válido: PENDING → CONFIRMED → SHIPPED → DELIVERED")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Estado actualizado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Transición de estado inválida")
    })
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<OrderResponse>> updateStatus(
            @PathVariable Long id,
            @RequestParam OrderStatus status) {
        log.info("PATCH /api/v1/orders/{}/status -> {}", id, status);
        return ResponseEntity.ok(ApiResponse.ok("Estado actualizado",
                orderService.updateStatus(id, status)));
    }

    @Operation(summary = "Cancelar orden", description = "Cancela la orden y libera el stock reservado")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Orden cancelada"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "No se puede cancelar una orden entregada")
    })
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancel(@PathVariable Long id) {
        log.info("PATCH /api/v1/orders/{}/cancel", id);
        orderService.cancel(id);
        return ResponseEntity.ok(ApiResponse.ok("Orden cancelada exitosamente", null));
    }
}