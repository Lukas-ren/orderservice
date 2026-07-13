package com.fitting.orderservice.dto;

import com.fitting.orderservice.entity.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Datos de la orden retornados por la API")
public class OrderResponse {

    @Schema(description = "ID de la orden", example = "1")
    private Long id;

    @Schema(description = "Número de orden", example = "ORD-20260522-0001")
    private String orderNumber;

    @Schema(description = "Nombre del cliente", example = "Juan Pérez")
    private String customerName;

    @Schema(description = "Email del cliente", example = "juan@fitting.com")
    private String customerEmail;

    @Schema(description = "Dirección de envío", example = "Av. Siempre Viva 123, Santiago")
    private String shippingAddress;

    @Schema(description = "Total de la orden", example = "89.97")
    private BigDecimal totalAmount;

    @Schema(description = "Estado de la orden", example = "PENDING",
            allowableValues = {"PENDING","CONFIRMED","SHIPPED","DELIVERED","CANCELLED"})
    private OrderStatus status;

    @Schema(description = "Fecha de creación", example = "2026-05-22T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Fecha de actualización", example = "2026-05-22T11:00:00")
    private LocalDateTime updatedAt;

    @Schema(description = "Items de la orden")
    private List<OrderItemResponse> items;
}