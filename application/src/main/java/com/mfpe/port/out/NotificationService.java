package com.mfpe.port.out;

import com.mfpe.model.enums.OrderStatus;

public interface NotificationService {
    void notifyOrderStatusChange(String orderId, OrderStatus status);
}
