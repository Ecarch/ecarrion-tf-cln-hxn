package com.mfpe.port.in;

/*
 * Interfaz que define el caso de uso para cancelar una orden.
 */
public interface CancelOrderUseCase {
    void cancelOrder(String orderId);
}
