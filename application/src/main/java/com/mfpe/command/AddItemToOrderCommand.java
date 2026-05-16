package com.mfpe.command;

import java.math.BigDecimal;

/**
 * Comando para agregar un item a una orden existente.
 */
public record AddItemToOrderCommand(
        String orderId,
        String productId,
        String productName,
        int quantity,
        BigDecimal unitPrice,
        String currency
) {
    public AddItemToOrderCommand {
        if (orderId == null || orderId.isBlank())
            throw new IllegalArgumentException("orderId is required");
        if (productId == null || productId.isBlank())
            throw new IllegalArgumentException("productId is required");
        if (quantity < 1)
            throw new IllegalArgumentException("quantity must be >= 1");
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("unitPrice must be > 0");
    }
}
