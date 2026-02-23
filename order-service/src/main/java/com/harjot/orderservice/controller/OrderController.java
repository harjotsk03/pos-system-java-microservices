package com.harjot.orderservice.controller;

import com.harjot.orderservice.dto.CreateOrderRequest;
import com.harjot.orderservice.dto.OrderResponse;
import com.harjot.orderservice.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * Create an order. Idempotent: send the same idempotencyKey in the body to get
     * the same order back without creating a duplicate (e.g. on retries).
     */
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        // Check idempotency: if we already have an order for this key, return it (200)
        OrderResponse existing = orderService.findByIdempotencyKey(request.getIdempotencyKey());
        if (existing != null) {
            return ResponseEntity.ok(existing);
        }
        OrderResponse created = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public OrderResponse getOrder(@PathVariable UUID id) {
        return orderService.getOrderById(id);
    }

    /**
     * Confirm an order (PENDING → CONFIRMED). Fails if order is not PENDING.
     */
    @PostMapping("/{id}/confirm")
    public OrderResponse confirmOrder(@PathVariable UUID id) {
        return orderService.confirmOrder(id);
    }
}
