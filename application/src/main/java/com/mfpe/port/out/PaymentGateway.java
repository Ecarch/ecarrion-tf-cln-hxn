package com.mfpe.port.out;

import com.mfpe.model.vo.Money;

/*
 * Interfaz que define el contrato para la pasarela de pago.
 */
public interface PaymentGateway {
    boolean processPayment(String orderId, Money amount);
}
