package com.harjot.orderservice.dto;

import com.harjot.orderservice.entity.Order;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
public class OrderResponse {
    private UUID id;
    private String idempotencyKey;
    private BigDecimal totalAmount;
    private String status;
    private List<OrderItemResponse> items;

    public static OrderResponse from(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems() == null
                ? List.of()
                : order.getItems().stream()
                        .map(item -> new OrderItemResponse(
                                item.getId(),
                                item.getProductId(),
                                item.getQuantity(),
                                item.getPrice()))
                        .toList();
        return new OrderResponse(
                order.getId(),
                order.getIdempotencyKey(),
                order.getTotalAmount(),
                order.getStatus().name(),
                itemResponses);
    }
}