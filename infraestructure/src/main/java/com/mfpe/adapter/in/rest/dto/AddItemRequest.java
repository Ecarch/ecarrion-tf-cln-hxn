package com.mfpe.adapter.in.rest.dto;

import java.math.BigDecimal;

/*
 * DTO para la solicitud de agregar un nuevo ítem a una orden existente.
 */
public record AddItemRequest(
        String productId,
        String productName,
        int quantity,
        BigDecimal unitPrice,
        String currency
) { }
