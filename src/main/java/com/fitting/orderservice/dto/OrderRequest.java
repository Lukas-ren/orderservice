package com.fitting.orderservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderRequest {

    @NotBlank(message = "El nombre del cliente es obligatorio")
    @Size(max = 100)
    private String customerName;

    @NotBlank(message = "El email del cliente es obligatorio")
    @Email(message = "El email no tiene formato válido")
    @Size(max = 150)
    private String customerEmail;

    @NotBlank(message = "La dirección de envío es obligatoria")
    @Size(max = 255)
    private String shippingAddress;

    @NotEmpty(message = "La orden debe tener al menos un producto")
    @Valid
    private List<OrderItemRequest> items;
}