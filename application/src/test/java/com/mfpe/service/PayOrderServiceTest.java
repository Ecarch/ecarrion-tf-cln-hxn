package com.mfpe.service;

import com.mfpe.exception.OrderDomainException;
import com.mfpe.exception.OrderNotFoundException;
import com.mfpe.model.entity.Order;
import com.mfpe.model.entity.OrderItem;
import com.mfpe.model.enums.OrderStatus;
import com.mfpe.model.vo.Money;
import com.mfpe.model.vo.OrderId;
import com.mfpe.port.out.FindOrderByIdPort;
import com.mfpe.port.out.PaymentGateway;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PayOrderServiceTest {

    @Mock
    private SaveOrderPort saveOrderPort;

    @Mock
    private FindOrderByIdPort findOrderByIdPort;

    @Mock
    private PaymentGateway paymentGateway;

    @InjectMocks
    private PayOrderService payOrderService;

    @Test
    void should_pay_order_successfully() {
        // Arrange
        Order order = Order.create("customer-1");
        order.addItem(new OrderItem("prod-1", "Product", 2,
                Money.of(new BigDecimal("25.00"), "USD")));
        String orderId = order.getId().toString();

        when(findOrderByIdPort.findById(OrderId.of(orderId))).thenReturn(Optional.of(order));
        when(paymentGateway.processPayment(eq(orderId), any(Money.class))).thenReturn(true);

        // Act
        payOrderService.payOrder(orderId);

        // Assert
        Assertions.assertEquals(OrderStatus.PAID, order.getStatus());
        verify(saveOrderPort).save(order);
    }

    @Test
    void should_throw_when_order_not_found() {
        // Arrange
        String orderId = OrderId.generate().toString();
        when(findOrderByIdPort.findById(OrderId.of(orderId))).thenReturn(Optional.empty());

        // Act & Assert
        Assertions.assertThrows(OrderNotFoundException.class,
                () -> payOrderService.payOrder(orderId));
        verify(paymentGateway, never()).processPayment(any(), any());
        verify(saveOrderPort, never()).save(any());
    }

    @Test
    void should_throw_when_payment_fails() {
        // Arrange
        Order order = Order.create("customer-1");
        order.addItem(new OrderItem("prod-1", "Product", 1,
                Money.of(new BigDecimal("10.00"), "USD")));
        String orderId = order.getId().toString();

        when(findOrderByIdPort.findById(OrderId.of(orderId))).thenReturn(Optional.of(order));
        when(paymentGateway.processPayment(eq(orderId), any(Money.class))).thenReturn(false);

        // Act & Assert
        OrderDomainException ex = Assertions.assertThrows(OrderDomainException.class,
                () -> payOrderService.payOrder(orderId));
        Assertions.assertTrue(ex.getMessage().contains("Payment processing failed"));
        Assertions.assertEquals(OrderStatus.PENDING, order.getStatus());
        verify(saveOrderPort, never()).save(any());
    }

    @Test
    void should_throw_when_order_has_no_items() {
        // Arrange
        Order order = Order.create("customer-1");
        String orderId = order.getId().toString();
        when(findOrderByIdPort.findById(OrderId.of(orderId))).thenReturn(Optional.of(order));

        // Act & Assert
        Assertions.assertThrows(OrderDomainException.class,
                () -> payOrderService.payOrder(orderId));
        verify(paymentGateway, never()).processPayment(any(), any());
        verify(saveOrderPort, never()).save(any());
    }

    @Test
    void should_process_payment_with_correct_total() {
        // Arrange
        Order order = Order.create("customer-1");
        order.addItem(new OrderItem("prod-1", "Product A", 2,
                Money.of(new BigDecimal("15.00"), "USD")));
        order.addItem(new OrderItem("prod-2", "Product B", 1,
                Money.of(new BigDecimal("20.00"), "USD")));
        String orderId = order.getId().toString();

        when(findOrderByIdPort.findById(OrderId.of(orderId))).thenReturn(Optional.of(order));
        when(paymentGateway.processPayment(eq(orderId), any(Money.class))).thenReturn(true);

        // Act
        payOrderService.payOrder(orderId);

        // Assert – total = (2 × 15) + (1 × 20) = 50.00
        verify(paymentGateway).processPayment(eq(orderId), argThat(money ->
                money.amount().compareTo(new BigDecimal("50.00")) == 0
                        && "USD".equals(money.currency())
        ));
    }

    @Test
    void should_save_order_after_successful_payment() {
        // Arrange
        Order order = Order.create("customer-1");
        order.addItem(new OrderItem("prod-1", "Product", 1,
                Money.of(new BigDecimal("30.00"), "USD")));
        String orderId = order.getId().toString();

        when(findOrderByIdPort.findById(OrderId.of(orderId))).thenReturn(Optional.of(order));
        when(paymentGateway.processPayment(eq(orderId), any(Money.class))).thenReturn(true);

        // Act
        payOrderService.payOrder(orderId);

        // Assert
        verify(saveOrderPort).save(argThat(o ->
                OrderStatus.PAID == o.getStatus()
        ));
    }
}
