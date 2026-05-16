package com.mfpe.command;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

public class AddItemToOrderCommandTest {

    @Test
    void should_create_command_with_valid_parameters() {
        AddItemToOrderCommand cmd = new AddItemToOrderCommand(
                "order-1", "prod-1", "Widget", 3, new BigDecimal("9.99"), "USD");

        Assertions.assertEquals("order-1", cmd.orderId());
        Assertions.assertEquals("prod-1", cmd.productId());
        Assertions.assertEquals("Widget", cmd.productName());
        Assertions.assertEquals(3, cmd.quantity());
        Assertions.assertEquals(new BigDecimal("9.99"), cmd.unitPrice());
        Assertions.assertEquals("USD", cmd.currency());
    }

    @Test
    void should_throw_when_orderId_is_null() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new AddItemToOrderCommand(null, "prod-1", "Widget", 1, BigDecimal.TEN, "USD"));
    }

    @Test
    void should_throw_when_orderId_is_blank() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new AddItemToOrderCommand("", "prod-1", "Widget", 1, BigDecimal.TEN, "USD"));
    }

    @Test
    void should_throw_when_productId_is_null() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new AddItemToOrderCommand("order-1", null, "Widget", 1, BigDecimal.TEN, "USD"));
    }

    @Test
    void should_throw_when_productId_is_blank() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new AddItemToOrderCommand("order-1", "", "Widget", 1, BigDecimal.TEN, "USD"));
    }

    @Test
    void should_throw_when_quantity_is_zero() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new AddItemToOrderCommand("order-1", "prod-1", "Widget", 0, BigDecimal.TEN, "USD"));
    }

    @Test
    void should_throw_when_quantity_is_negative() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new AddItemToOrderCommand("order-1", "prod-1", "Widget", -1, BigDecimal.TEN, "USD"));
    }

    @Test
    void should_throw_when_unitPrice_is_null() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new AddItemToOrderCommand("order-1", "prod-1", "Widget", 1, null, "USD"));
    }

    @Test
    void should_throw_when_unitPrice_is_zero() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new AddItemToOrderCommand("order-1", "prod-1", "Widget", 1, BigDecimal.ZERO, "USD"));
    }

    @Test
    void should_throw_when_unitPrice_is_negative() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new AddItemToOrderCommand("order-1", "prod-1", "Widget", 1, new BigDecimal("-5.00"), "USD"));
    }


}
