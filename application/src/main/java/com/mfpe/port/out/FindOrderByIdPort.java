package com.mfpe.port.out;

import com.mfpe.model.entity.Order;
import com.mfpe.model.vo.OrderId;

import java.util.Optional;

/*
 * Interfaz que define el puerto de salida para encontrar una orden por su ID.
 */
public interface FindOrderByIdPort {
    Optional<Order> findById(OrderId orderId);
}
