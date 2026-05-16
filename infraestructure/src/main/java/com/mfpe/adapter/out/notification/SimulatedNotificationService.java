package com.mfpe.adapter.out.notification;

import com.mfpe.model.enums.OrderStatus;
import com.mfpe.port.out.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SimulatedNotificationService implements NotificationService {

    private static final Logger log =
            LoggerFactory.getLogger(SimulatedNotificationService.class);

    @Override
    public void notifyOrderStatusChange(String orderId, OrderStatus status) {
        log.info("[NOTIFICATION] Order [{}] changed to status: {}", orderId, status);
    }
}
