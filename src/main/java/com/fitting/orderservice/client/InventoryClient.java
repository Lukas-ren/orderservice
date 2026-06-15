package com.fitting.orderservice.client;

import com.fitting.orderservice.util.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@FeignClient(name = "inventory-service", url = "${inventory.service.url}")
public interface InventoryClient {

    @GetMapping("/api/v1/inventory/product/{productId}/available")
    ApiResponse<Boolean> isAvailable(
            @PathVariable Long productId,
            @RequestParam int quantity);

    @PatchMapping("/api/v1/inventory/product/{productId}/reserve")
    ApiResponse<Map<String, Object>> reserveStock(
            @PathVariable Long productId,
            @RequestBody Map<String, Integer> request);

    @PatchMapping("/api/v1/inventory/product/{productId}/release")
    ApiResponse<Map<String, Object>> releaseStock(
            @PathVariable Long productId,
            @RequestBody Map<String, Integer> request);
}