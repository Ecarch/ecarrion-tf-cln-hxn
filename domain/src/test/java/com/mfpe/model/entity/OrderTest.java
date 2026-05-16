package com.mfpe.model.entity;

import com.mfpe.exception.EmptyOrderException;
import com.mfpe.exception.OrderAlreadyCancelledException;
import com.mfpe.exception.OrderAlreadyPaidException;
import com.mfpe.exception.OrderDomainException;
import com.mfpe.model.enums.OrderStatus;
import com.mfpe.model.vo.Money;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

public class OrderTest {
    @Test
    void should_create_order_with_pending_status() {
        Order order = Order.create("customer-1");
        Assertions.assertEquals(OrderStatus.PENDING, order.getStatus());
        Assertions.assertNotNull(order.getId());
        Assertions.assertTrue(order.getItems().isEmpty());
    }

    @Test
    void should_throw_when_customerId_is_blank() {
        Assertions.assertThrows(OrderDomainException.class, () -> Order.create(""));
    }

    @Test
    void should_calculate_total_correctly() {
        Order order = Order.create("customer-1");
        order.addItem(item("P1", 2, "10.00"));
        order.addItem(item("P2", 1, "15.00"));
        order.calculateTotal();
        Assertions.assertEquals(0, new BigDecimal("35.00").compareTo(order.getTotal().amount()));
    }

    @Test
    void should_throw_when_paying_empty_order() {
        Order order = Order.create("customer-1");
        Assertions.assertThrows(EmptyOrderException.class, order::pay);
    }

    @Test
    void should_transition_to_paid_status() {
        Order order = Order.create("customer-1");
        order.addItem(item("P1", 1, "50.00"));
        order.pay();
        Assertions.assertEquals(OrderStatus.PAID, order.getStatus());
    }

    @Test
    void should_throw_when_paying_already_paid_order() {
        Order order = Order.create("customer-1");
        order.addItem(item("P1", 1, "50.00"));
        order.pay();
        Assertions.assertThrows(OrderAlreadyPaidException.class, order::pay);
    }

    @Test
    void should_throw_when_cancelling_already_cancel_order() {
        Order order = Order.create("customer-1");
        order.addItem(item("P1", 1, "50.00"));
        order.cancel();
        Assertions.assertThrows(OrderAlreadyCancelledException.class, order::cancel);
    }

    @Test
    void should_throw_when_cancelling_paid_order() {
        Order order = Order.create("customer-1");
        order.addItem(item("P1", 1, "50.00"));
        order.pay();
        OrderDomainException ex = Assertions.assertThrows(OrderDomainException.class, order::cancel);
        Assertions.assertTrue(ex.getMessage().contains("paid"));
    }

    @Test
    void should_transition_to_cancelled_status() {
        Order order = Order.create("customer-1");
        order.cancel();
        Assertions.assertEquals(OrderStatus.CANCELLED, order.getStatus());
    }

    // ── Helper ────────────────────────────────────────────────────

    private OrderItem item(String productId, int qty, String price) {
        return new OrderItem(productId, "Product", qty,
                Money.of(new BigDecimal(price), "USD"));
    }
}
