package com.harjot.orderservice.service;

import com.harjot.orderservice.dto.CreateOrderRequest;
import com.harjot.orderservice.dto.OrderResponse;
import com.harjot.orderservice.entity.Order;
import com.harjot.orderservice.entity.OrderItem;
import com.harjot.orderservice.entity.Order.OrderStatus;
import com.harjot.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    /**
     * Creates an order in an idempotent way: if an order with the same idempotencyKey
     * already exists, returns that order without creating a duplicate.
     */
    @Transactional(readOnly = true)
    public OrderResponse findByIdempotencyKey(String idempotencyKey) {
        return orderRepository.findByIdempotencyKey(idempotencyKey)
                .map(OrderResponse::from)
                .orElse(null);
    }

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        // Idempotency: return existing order if same key was already used
        Order existing = orderRepository.findByIdempotencyKey(request.getIdempotencyKey()).orElse(null);
        if (existing != null) {
            return OrderResponse.from(existing);
        }

        Order order = Order.builder()
                .idempotencyKey(request.getIdempotencyKey())
                .status(OrderStatus.PENDING)
                .build();

        List<OrderItem> items = request.getItems().stream()
                .map(req -> {
                    BigDecimal lineTotal = req.getPrice().multiply(BigDecimal.valueOf(req.getQuantity()));
                    return OrderItem.builder()
                            .productId(req.getProductId())
                            .quantity(req.getQuantity())
                            .price(req.getPrice())
                            .order(order)
                            .build();
                })
                .collect(Collectors.toList());

        order.setItems(items);
        BigDecimal total = items.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalAmount(total);

        try {
            Order saved = orderRepository.save(order);
            return OrderResponse.from(saved);
        } catch (DataIntegrityViolationException e) {
            // Race: another request created order with same idempotency key; return existing
            Order existingOrder = orderRepository.findByIdempotencyKey(request.getIdempotencyKey())
                    .orElseThrow(() -> new IllegalStateException("Duplicate key but order not found", e));
            return OrderResponse.from(existingOrder);
        }
    }

    public OrderResponse getOrderById(UUID id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found: " + id));
        return OrderResponse.from(order);
    }

    @Transactional
    public OrderResponse confirmOrder(UUID id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found: " + id));
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Order cannot be confirmed: current status is " + order.getStatus());
        }
        order.setStatus(OrderStatus.CONFIRMED);
        Order saved = orderRepository.save(order);
        return OrderResponse.from(saved);
    }
}
