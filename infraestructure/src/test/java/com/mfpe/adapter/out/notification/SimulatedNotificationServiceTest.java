package com.mfpe.adapter.out.notification;

import com.mfpe.model.enums.OrderStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class SimulatedNotificationServiceTest {

    private final SimulatedNotificationService service = new SimulatedNotificationService();

    @Test
    void should_not_throw_when_notifying_paid_status() {
        // Arrange
        String orderId = "order-123";

        // Act & Assert
        assertDoesNotThrow(() ->
                service.notifyOrderStatusChange(orderId, OrderStatus.PAID));
    }

    @Test
    void should_not_throw_when_notifying_cancelled_status() {
        // Arrange
        String orderId = "order-456";

        // Act & Assert
        assertDoesNotThrow(() ->
                service.notifyOrderStatusChange(orderId, OrderStatus.CANCELLED));
    }
}
