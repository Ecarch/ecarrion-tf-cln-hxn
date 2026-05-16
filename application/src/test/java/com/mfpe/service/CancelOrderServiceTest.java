package com.mfpe.service;

import com.mfpe.exception.OrderAlreadyCancelledException;
import com.mfpe.exception.OrderDomainException;
import com.mfpe.exception.OrderNotFoundException;
import com.mfpe.model.entity.Order;
import com.mfpe.model.entity.OrderItem;
import com.mfpe.model.enums.OrderStatus;
import com.mfpe.model.vo.Money;
import com.mfpe.model.vo.OrderId;
import com.mfpe.port.out.FindOrderByIdPort;
import com.mfpe.port.out.NotificationService;
import com.mfpe.port.out.SaveOrderPort;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CancelOrderServiceTest {

    @Mock
    private SaveOrderPort saveOrderPort;

    @Mock
    private FindOrderByIdPort findOrderByIdPort;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private CancelOrderService cancelOrderService;

    @Test
    void should_cancel_pending_order_successfully() {
        // Arrange
        Order order = Order.create("customer-1");
        String orderId = order.getId().toString();
        when(findOrderByIdPort.findById(OrderId.of(orderId))).thenReturn(Optional.of(order));

        // Act
        cancelOrderService.cancelOrder(orderId);

        // Assert
        Assertions.assertEquals(OrderStatus.CANCELLED, order.getStatus());
        verify(saveOrderPort).save(order);
    }

    @Test
    void should_throw_when_order_not_found() {
        // Arrange
        String orderId = OrderId.generate().toString();
        when(findOrderByIdPort.findById(OrderId.of(orderId))).thenReturn(Optional.empty());

        // Act & Assert
        Assertions.assertThrows(OrderNotFoundException.class,
                () -> cancelOrderService.cancelOrder(orderId));
        verify(saveOrderPort, never()).save(any());
    }

    @Test
    void should_throw_when_cancelling_paid_order() {
        // Arrange
        Order order = Order.create("customer-1");
        order.addItem(new OrderItem("prod-1", "Product", 1,
                Money.of(new BigDecimal("50.00"), "USD")));
        order.pay();
        String orderId = order.getId().toString();
        when(findOrderByIdPort.findById(OrderId.of(orderId))).thenReturn(Optional.of(order));

        // Act & Assert
        Assertions.assertThrows(OrderDomainException.class,
                () -> cancelOrderService.cancelOrder(orderId));
    }

    @Test
    void should_throw_when_cancelling_already_cancelled_order() {
        // Arrange
        Order order = Order.create("customer-1");
        order.cancel();
        String orderId = order.getId().toString();
        when(findOrderByIdPort.findById(OrderId.of(orderId))).thenReturn(Optional.of(order));

        // Act & Assert
        Assertions.assertThrows(OrderAlreadyCancelledException.class,
                () -> cancelOrderService.cancelOrder(orderId));
    }

    @Test
    void should_save_cancelled_order() {
        // Arrange
        Order order = Order.create("customer-1");
        String orderId = order.getId().toString();
        when(findOrderByIdPort.findById(OrderId.of(orderId))).thenReturn(Optional.of(order));

        // Act
        cancelOrderService.cancelOrder(orderId);

        // Assert
        verify(saveOrderPort, times(1)).save(argThat(o ->
                OrderStatus.CANCELLED == o.getStatus()
        ));
    }
}
