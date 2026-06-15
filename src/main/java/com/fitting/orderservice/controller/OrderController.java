package com.fitting.orderservice.controller;

import com.fitting.orderservice.dto.OrderRequest;
import com.fitting.orderservice.dto.OrderResponse;
import com.fitting.orderservice.entity.OrderStatus;
import com.fitting.orderservice.service.OrderService;
import com.fitting.orderservice.util.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> create(
            @Valid @RequestBody OrderRequest request) {
        log.info("POST /api/v1/orders - Cliente: {}", request.getCustomerEmail());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Orden creada exitosamente",
                        orderService.create(request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderResponse>>> findAll() {
        log.info("GET /api/v1/orders");
        return ResponseEntity.ok(ApiResponse.ok("Lista de órdenes",
                orderService.findAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> findById(@PathVariable Long id) {
        log.info("GET /api/v1/orders/{}", id);
        return ResponseEntity.ok(ApiResponse.ok("Orden encontrada",
                orderService.findById(id)));
    }

    @GetMapping("/number/{orderNumber}")
    public ResponseEntity<ApiResponse<OrderResponse>> findByOrderNumber(
            @PathVariable String orderNumber) {
        log.info("GET /api/v1/orders/number/{}", orderNumber);
        return ResponseEntity.ok(ApiResponse.ok("Orden encontrada",
                orderService.findByOrderNumber(orderNumber)));
    }

    @GetMapping("/customer/{email}")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> findByCustomer(
            @PathVariable String email) {
        log.info("GET /api/v1/orders/customer/{}", email);
        return ResponseEntity.ok(ApiResponse.ok("Órdenes del cliente",
                orderService.findByCustomerEmail(email)));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> findByStatus(
            @PathVariable OrderStatus status) {
        log.info("GET /api/v1/orders/status/{}", status);
        return ResponseEntity.ok(ApiResponse.ok("Órdenes por estado",
                orderService.findByStatus(status)));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<OrderResponse>> updateStatus(
            @PathVariable Long id,
            @RequestParam OrderStatus status) {
        log.info("PATCH /api/v1/orders/{}/status -> {}", id, status);
        return ResponseEntity.ok(ApiResponse.ok("Estado actualizado",
                orderService.updateStatus(id, status)));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancel(@PathVariable Long id) {
        log.info("PATCH /api/v1/orders/{}/cancel", id);
        orderService.cancel(id);
        return ResponseEntity.ok(ApiResponse.ok("Orden cancelada exitosamente", null));
    }
}