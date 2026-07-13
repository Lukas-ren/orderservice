package com.fitting.orderservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Item de la orden retornado por la API")
public class OrderItemResponse {

    @Schema(description = "ID del item", example = "1")
    private Long id;

    @Schema(description = "ID del producto", example = "1")
    private Long productId;

    @Schema(description = "Nombre del producto", example = "Camiseta Básica Blanca")
    private String productName;

    @Schema(description = "Cantidad", example = "2")
    private Integer quantity;

    @Schema(description = "Precio unitario al momento de la compra", example = "19.99")
    private BigDecimal unitPrice;

    @Schema(description = "Subtotal del item", example = "39.98")
    private BigDecimal subtotal;
}