package com.mfpe.adapter.in.rest.dto;

import com.mfpe.model.entity.Order;
import com.mfpe.model.entity.OrderItem;
import com.mfpe.model.enums.OrderStatus;
import com.mfpe.model.vo.Money;
import com.mfpe.model.vo.OrderId;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderResponseMapperTest {

    private final OrderResponseMapper mapper = new OrderResponseMapper();

    private Order createOrderWithItems() {
        OrderId id = OrderId.of("550e8400-e29b-41d4-a716-446655440000");
        Money total = Money.of(new BigDecimal("300.00"), "EUR");
        OrderItem item1 = new OrderItem("PROD-1", "Laptop", 2,
                Money.of(new BigDecimal("100.00"), "EUR"));
        OrderItem item2 = new OrderItem("PROD-2", "Mouse", 1,
                Money.of(new BigDecimal("100.00"), "EUR"));
        return Order.reconstitute(id, "customer-1", OrderStatus.PAID,
                total, LocalDateTime.of(2025, 6, 1, 10, 0), List.of(item1, item2));
    }

    private Order createOrderWithoutTotal() {
        OrderId id = OrderId.of("660e8400-e29b-41d4-a716-446655440000");
        return Order.reconstitute(id, "customer-2", OrderStatus.PENDING,
                null, LocalDateTime.of(2025, 7, 1, 12, 0), List.of());
    }

    @Test
    void toResponse_shouldMapBasicFields() {
        Order order = createOrderWithItems();

        OrderResponse response = mapper.toResponse(order);

        assertEquals("550e8400-e29b-41d4-a716-446655440000", response.id());
        assertEquals("customer-1", response.customerId());
        assertEquals(OrderStatus.PAID, response.status());
        assertEquals(LocalDateTime.of(2025, 6, 1, 10, 0), response.createdAt());
    }

    @Test
    void toResponse_shouldMapTotal() {
        Order order = createOrderWithItems();

        OrderResponse response = mapper.toResponse(order);

        assertEquals(new BigDecimal("300.00"), response.totalAmount());
        assertEquals("EUR", response.totalCurrency());
    }

    @Test
    void toResponse_shouldHandleNullTotal() {
        Order order = createOrderWithoutTotal();

        OrderResponse response = mapper.toResponse(order);

        assertNull(response.totalAmount());
        assertNull(response.totalCurrency());
    }

    @Test
    void toResponse_shouldMapItems() {
        Order order = createOrderWithItems();

        OrderResponse response = mapper.toResponse(order);

        assertEquals(2, response.items().size());

        OrderItemResponse first = response.items().getFirst();
        assertEquals("PROD-1", first.productId());
        assertEquals("Laptop", first.productName());
        assertEquals(2, first.quantity());
        assertEquals(new BigDecimal("100.00"), first.unitPrice());
        assertEquals("EUR", first.currency());
    }

    @Test
    void toResponse_shouldCalculateSubtotalForItems() {
        Order order = createOrderWithItems();

        OrderResponse response = mapper.toResponse(order);

        OrderItemResponse first = response.items().getFirst();
        // 100.00 * 2 = 200.00
        assertEquals(new BigDecimal("200.00"), first.subtotal());

        OrderItemResponse second = response.items().get(1);
        // 100.00 * 1 = 100.00
        assertEquals(new BigDecimal("100.00"), second.subtotal());
    }

    @Test
    void toResponse_shouldReturnEmptyItemsForOrderWithoutItems() {
        Order order = createOrderWithoutTotal();

        OrderResponse response = mapper.toResponse(order);

        assertTrue(response.items().isEmpty());
    }
}
