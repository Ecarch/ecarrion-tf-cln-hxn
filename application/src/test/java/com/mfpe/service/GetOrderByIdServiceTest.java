package com.mfpe.service;

import com.mfpe.exception.OrderNotFoundException;
import com.mfpe.model.entity.Order;
import com.mfpe.model.enums.OrderStatus;
import com.mfpe.model.vo.OrderId;
import com.mfpe.port.out.FindOrderByIdPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetOrderByIdServiceTest {

    @Mock
    private FindOrderByIdPort findOrderByIdPort;

    @InjectMocks
    private GetOrderByIdService getOrderByIdService;

    @Test
    void should_return_order_when_found() {
        // Arrange
        String orderId = OrderId.generate().toString();
        Order expected = Order.create("customer-1");
        when(findOrderByIdPort.findById(OrderId.of(orderId))).thenReturn(Optional.of(expected));

        // Act
        Order result = getOrderByIdService.getOrderById(orderId);

        // Assert
        assertNotNull(result);
        assertEquals("customer-1", result.getCustomerId());
        assertEquals(OrderStatus.PENDING, result.getStatus());
        verify(findOrderByIdPort).findById(OrderId.of(orderId));
    }

    @Test
    void should_throw_OrderNotFoundException_when_not_found() {
        // Arrange
        String orderId = OrderId.generate().toString();
        when(findOrderByIdPort.findById(OrderId.of(orderId))).thenReturn(Optional.empty());

        // Act & Assert
        OrderNotFoundException ex = assertThrows(OrderNotFoundException.class,
                () -> getOrderByIdService.getOrderById(orderId));

        assertTrue(ex.getMessage().contains(orderId));
        verify(findOrderByIdPort).findById(OrderId.of(orderId));
    }
}
