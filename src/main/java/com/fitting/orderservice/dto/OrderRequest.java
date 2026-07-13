package com.fitting.orderservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Datos para crear una orden de compra")
public class OrderRequest {

    @Schema(description = "Nombre del cliente", example = "Juan Pérez")
    @NotBlank(message = "El nombre del cliente es obligatorio")
    @Size(max = 100)
    private String customerName;

    @Schema(description = "Email del cliente", example = "juan@fitting.com")
    @NotBlank(message = "El email del cliente es obligatorio")
    @Email
    @Size(max = 150)
    private String customerEmail;

    @Schema(description = "Dirección de envío", example = "Av. Siempre Viva 123, Santiago")
    @NotBlank(message = "La dirección de envío es obligatoria")
    @Size(max = 255)
    private String shippingAddress;

    @Schema(description = "Lista de productos de la orden")
    @NotEmpty(message = "La orden debe tener al menos un producto")
    @Valid
    private List<OrderItemRequest> items;
}