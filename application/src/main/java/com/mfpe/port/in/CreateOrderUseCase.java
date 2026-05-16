package com.mfpe.port.in;

import com.mfpe.command.CreateOrderCommand;
import com.mfpe.model.entity.Order;

/**
 * Interfaz que define el caso de uso para crear una nueva orden.
 */
public interface CreateOrderUseCase {
    Order createOrder(CreateOrderCommand command);
}
