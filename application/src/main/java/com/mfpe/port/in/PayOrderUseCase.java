package com.mfpe.port.in;

/*
 * Interfaz que define el caso de uso para pagar una orden.
 */
public interface PayOrderUseCase {
    void payOrder(String orderId);
}
