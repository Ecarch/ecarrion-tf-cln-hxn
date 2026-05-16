package com.mfpe.service;

import com.mfpe.exception.OrderNotFoundException;
import com.mfpe.model.entity.Order;
import com.mfpe.model.vo.OrderId;
import com.mfpe.port.in.CancelOrderUseCase;
import com.mfpe.port.out.FindOrderByIdPort;
import com.mfpe.port.out.NotificationService;
import com.mfpe.port.out.SaveOrderPort;

public class CancelOrderService implements CancelOrderUseCase {

    private final FindOrderByIdPort findOrderByIdPort;
    private final SaveOrderPort saveOrderPort;
    private final NotificationService notificationService;

    public CancelOrderService(FindOrderByIdPort findOrderByIdPort,
                              SaveOrderPort saveOrderPort,
                              NotificationService notificationService) {
        this.findOrderByIdPort = findOrderByIdPort;
        this.saveOrderPort = saveOrderPort;
        this.notificationService = notificationService;
    }

    @Override
    public void cancelOrder(String orderId) {
        Order order = findOrderByIdPort.findById(OrderId.of(orderId))
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        order.cancel();
        saveOrderPort.save(order);
        notificationService.notifyOrderStatusChange(orderId, order.getStatus());
    }
}
