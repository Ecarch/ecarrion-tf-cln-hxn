package com.mfpe.adapter.in.rest.dto;

import java.math.BigDecimal;

/*
 * Response DTO para devolver los datos de un item de la orden.
 */
public record OrderItemResponse(
    String productId,
    String productName,
    int quantity,
    BigDecimal unitPrice,
    BigDecimal subtotal,
    String currency
) {
}
