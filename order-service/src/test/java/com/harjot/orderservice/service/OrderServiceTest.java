package com.harjot.orderservice.service;

import com.harjot.orderservice.dto.CreateOrderRequest;
import com.harjot.orderservice.dto.OrderItemRequest;
import com.harjot.orderservice.dto.OrderResponse;
import com.harjot.orderservice.entity.Order;
import com.harjot.orderservice.entity.OrderItem;
import com.harjot.orderservice.entity.Order.OrderStatus;
import com.harjot.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    void createOrder_createsNewOrder_whenIdempotencyKeyIsNew() {
        String idempotencyKey = "order-uuid-123";
        OrderItemRequest item = new OrderItemRequest();
        item.setProductId(UUID.randomUUID());
        item.setQuantity(2);
        item.setPrice(new BigDecimal("9.99"));
        CreateOrderRequest request = createRequest(idempotencyKey, List.of(item));

        when(orderRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.empty());
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setId(UUID.randomUUID());
            if (o.getItems() != null) {
                for (int i = 0; i < o.getItems().size(); i++) {
                    o.getItems().get(i).setId(UUID.randomUUID());
                }
            }
            return o;
        });

        OrderResponse response = orderService.createOrder(request);

        assertThat(response).isNotNull();
        assertThat(response.getIdempotencyKey()).isEqualTo(idempotencyKey);
        assertThat(response.getStatus()).isEqualTo(OrderStatus.PENDING.name());
        assertThat(response.getTotalAmount()).isEqualByComparingTo(new BigDecimal("19.98"));
        assertThat(response.getItems()).hasSize(1);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void createOrder_returnsExistingOrder_whenIdempotencyKeyAlreadyUsed() {
        String idempotencyKey = "duplicate-key-456";
        UUID orderId = UUID.randomUUID();
        OrderItemRequest item = new OrderItemRequest();
        item.setProductId(UUID.randomUUID());
        item.setQuantity(1);
        item.setPrice(new BigDecimal("10.00"));
        CreateOrderRequest request = createRequest(idempotencyKey, List.of(item));

        OrderItem orderItem = OrderItem.builder().id(UUID.randomUUID()).productId(item.getProductId()).quantity(1).price(new BigDecimal("10.00")).build();
        Order existingOrder = Order.builder()
                .id(orderId)
                .idempotencyKey(idempotencyKey)
                .totalAmount(new BigDecimal("10.00"))
                .status(OrderStatus.PENDING)
                .items(new java.util.ArrayList<>(List.of(orderItem)))
                .build();
        orderItem.setOrder(existingOrder);

        when(orderRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.of(existingOrder));

        OrderResponse response = orderService.createOrder(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(orderId);
            assertThat(response.getIdempotencyKey()).isEqualTo(idempotencyKey);
        verify(orderRepository, never()).save(any(Order.class));
    }

    private static CreateOrderRequest createRequest(String idempotencyKey, List<OrderItemRequest> items) {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setIdempotencyKey(idempotencyKey);
        request.setItems(items);
        return request;
    }
}
