package com.fitting.orderservice.client;

import com.fitting.orderservice.util.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@FeignClient(name = "product-service")
public interface ProductClient {

    @GetMapping("/api/v1/products/{id}")
    ApiResponse<Map<String, Object>> getProductById(@PathVariable Long id);
}