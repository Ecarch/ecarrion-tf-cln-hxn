package com.mfpe.command;

/**
 * Comando para crear una nueva orden de compra.
 */
public record CreateOrderCommand(String customerId) {
    public CreateOrderCommand {
        if (customerId == null || customerId.isBlank())
            throw new IllegalArgumentException("customerId is required");
    }
}
