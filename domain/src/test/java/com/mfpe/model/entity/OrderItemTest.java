package com.mfpe.model.entity;

import com.mfpe.exception.OrderDomainException;
import com.mfpe.model.vo.Money;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

public class OrderItemTest {

    @Test
    void should_create_order_item_with_valid_data() {
        OrderItem item = item("P1", "Product 1", 3, "10.00");

        Assertions.assertEquals("P1", item.getProductId());
        Assertions.assertEquals("Product 1", item.getProductName());
        Assertions.assertEquals(3, item.getQuantity());
        Assertions.assertEquals(Money.of(new BigDecimal("10.00"), "USD"), item.getUnitPrice());
    }

    @Test
    void should_calculate_subtotal_correctly() {
        OrderItem item = item("P1", "Product 1", 5, "20.00");

        Money subtotal = item.calculateSubtotal();

        Assertions.assertEquals(0, new BigDecimal("100.00").compareTo(subtotal.amount()));
        Assertions.assertEquals("USD", subtotal.currency());
    }

    @Test
    void should_calculate_subtotal_with_quantity_one() {
        OrderItem item = item("P1", "Product 1", 1, "15.50");

        Money subtotal = item.calculateSubtotal();

        Assertions.assertEquals(0, new BigDecimal("15.50").compareTo(subtotal.amount()));
    }

    @Test
    void should_throw_when_productId_is_null() {
        Assertions.assertThrows(NullPointerException.class,
                () -> new OrderItem(null, "Product", 1, Money.of(BigDecimal.TEN, "USD")));
    }

    @Test
    void should_throw_when_productId_is_blank() {
        Assertions.assertThrows(OrderDomainException.class,
                () -> new OrderItem("", "Product", 1, Money.of(BigDecimal.TEN, "USD")));
    }

    @Test
    void should_throw_when_productName_is_null() {
        Assertions.assertThrows(NullPointerException.class,
                () -> new OrderItem("P1", null, 1, Money.of(BigDecimal.TEN, "USD")));
    }

    @Test
    void should_throw_when_unitPrice_is_null() {
        Assertions.assertThrows(NullPointerException.class,
                () -> new OrderItem("P1", "Product", 1, null));
    }

    @Test
    void should_throw_when_quantity_is_zero() {
        Assertions.assertThrows(OrderDomainException.class,
                () -> item("P1", "Product", 0, "10.00"));
    }

    @Test
    void should_throw_when_quantity_is_negative() {
        Assertions.assertThrows(OrderDomainException.class,
                () -> item("P1", "Product", -1, "10.00"));
    }

    // ── Helper ────────────────────────────────────────────────────

    private OrderItem item(String productId, String productName, int qty, String price) {
        return new OrderItem(productId, productName, qty,
                Money.of(new BigDecimal(price), "USD"));
    }
}

