package com.tradematching.controller;

import com.tradematching.dto.ApiResponse;
import com.tradematching.dto.OrderRequest;
import com.tradematching.dto.OrderResponse;
import com.tradematching.model.Order;
import com.tradematching.model.OrderType;
import com.tradematching.service.MatchingEngineService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    private final MatchingEngineService matchingEngine;

    public OrderController(MatchingEngineService matchingEngine) {
        this.matchingEngine = matchingEngine;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> submitOrder(@Valid @RequestBody OrderRequest request) {
        if (request.getType() == OrderType.LIMIT && (request.getPrice() == null || request.getPrice().compareTo(java.math.BigDecimal.ZERO) <= 0)) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Price must be greater than 0 for LIMIT orders"));
        }

        OrderResponse response = matchingEngine.placeOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Order processed", response));
    }

    @GetMapping("/{symbol}/{orderId}")
    public ResponseEntity<ApiResponse<Order>> getOrder(
            @PathVariable String symbol,
            @PathVariable Long orderId) {
        return matchingEngine.getOrder(symbol, orderId)
                .map(order -> ResponseEntity.ok(ApiResponse.ok(order)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Order not found with ID: " + orderId)));
    }

    @DeleteMapping("/{symbol}/{orderId}")
    public ResponseEntity<ApiResponse<String>> cancelOrder(
            @PathVariable String symbol,
            @PathVariable Long orderId) {
        boolean cancelled = matchingEngine.cancelOrder(symbol, orderId);
        if (cancelled) {
            return ResponseEntity.ok(ApiResponse.ok("Order cancelled successfully", "CANCELLED"));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Could not cancel order. It may be filled, already cancelled, or not found."));
    }
}
