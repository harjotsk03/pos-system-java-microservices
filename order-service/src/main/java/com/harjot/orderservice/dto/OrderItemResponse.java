package com.harjot.orderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
public class OrderItemResponse {
    private UUID id;
    private UUID productId;
    private Integer quantity;
    private BigDecimal price;
}
