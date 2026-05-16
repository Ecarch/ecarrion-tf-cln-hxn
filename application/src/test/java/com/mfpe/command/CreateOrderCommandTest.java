package com.mfpe.command;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CreateOrderCommandTest {

    @Test
    void should_create_command_with_valid_customerId() {
        CreateOrderCommand cmd = new CreateOrderCommand("customer-1");
        Assertions.assertEquals("customer-1", cmd.customerId());
    }

    @Test
    void should_throw_when_customerId_is_null() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new CreateOrderCommand(null));
    }

    @Test
    void should_throw_when_customerId_is_blank() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new CreateOrderCommand(""));
    }

}
