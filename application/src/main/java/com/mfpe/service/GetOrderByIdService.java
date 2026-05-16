package com.mfpe.service;

import com.mfpe.exception.OrderNotFoundException;
import com.mfpe.model.entity.Order;
import com.mfpe.model.vo.OrderId;
import com.mfpe.port.in.GetOrderByIdUseCase;
import com.mfpe.port.out.FindOrderByIdPort;

public class GetOrderByIdService implements GetOrderByIdUseCase {

    private final FindOrderByIdPort findOrderByIdPort;

    public GetOrderByIdService(FindOrderByIdPort findOrderByIdPort) {
        this.findOrderByIdPort = findOrderByIdPort;
    }

    @Override
    public Order getOrderById(String orderId) {
        return findOrderByIdPort.findById(OrderId.of(orderId))
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }
}
