package com.fitting.orderservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Item de una orden de compra")
public class OrderItemRequest {

    @Schema(description = "ID del producto", example = "1")
    @NotNull(message = "El ID del producto es obligatorio")
    private Long productId;

    @Schema(description = "Cantidad solicitada", example = "2")
    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1)
    private Integer quantity;
}