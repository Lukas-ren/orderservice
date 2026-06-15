package com.fitting.orderservice.service;

import com.fitting.orderservice.dto.OrderRequest;
import com.fitting.orderservice.dto.OrderResponse;
import com.fitting.orderservice.entity.OrderStatus;

import java.util.List;

public interface OrderService {

    OrderResponse create(OrderRequest request);

    OrderResponse findById(Long id);

    OrderResponse findByOrderNumber(String orderNumber);

    List<OrderResponse> findAll();

    List<OrderResponse> findByCustomerEmail(String email);

    List<OrderResponse> findByStatus(OrderStatus status);

    OrderResponse updateStatus(Long id, OrderStatus newStatus);

    void cancel(Long id);
}
