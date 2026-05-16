package com.mfpe.port.in;

import com.mfpe.model.entity.Order;

public interface GetOrderByIdUseCase {
    Order getOrderById(String orderId);
}
