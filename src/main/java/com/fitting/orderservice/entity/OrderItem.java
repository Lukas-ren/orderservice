package com.fitting.orderservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Referencia al producto en product-service (sin FK real entre DBs)
    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false, length = 150)
    private String productName;             // desnormalizado

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;           // precio al momento de la orden

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;            // quantity * unitPrice

    // Muchos items pertenecen a una orden
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
}
