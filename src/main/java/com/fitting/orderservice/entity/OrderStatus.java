package com.fitting.orderservice.entity;

public enum OrderStatus {
    PENDING,      // recién creada, stock reservado
    CONFIRMED,    // pago confirmado
    SHIPPED,      // en camino
    DELIVERED,    // entregada
    CANCELLED     // cancelada, stock liberado
}