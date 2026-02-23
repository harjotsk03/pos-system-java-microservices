package com.harjot.orderservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class CreateOrderRequest {

    /**
     * Idempotency key: client must send a unique key (e.g. UUID) per logical order.
     * Repeating the same key returns the existing order without creating a duplicate.
     */
    @NotBlank(message = "idempotencyKey is required for idempotent ordering")
    private String idempotencyKey;

    @NotEmpty(message = "order must have at least one item")
    @Valid
    private List<OrderItemRequest> items;
}
