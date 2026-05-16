package com.mfpe.port.out;

import com.mfpe.model.entity.Order;

/*
 * Interfaz que define el puerto de salida para guardar una orden.
 */
public interface SaveOrderPort {
    Order save(Order order);
}