package com.mfpe.service;

import com.mfpe.command.CreateOrderCommand;
import com.mfpe.model.entity.Order;
import com.mfpe.model.enums.OrderStatus;
import com.mfpe.port.out.SaveOrderPort;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateOrderServiceTest {

    @Mock
    private SaveOrderPort saveOrderPort;

    @InjectMocks
    private CreateOrderService createOrderService;

    @Test
    void should_create_order_and_save_it() {
        // Arrange
        CreateOrderCommand command = new CreateOrderCommand("customer-1");
        when(saveOrderPort.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Order result = createOrderService.createOrder(command);

        // Assert
        Assertions.assertNotNull(result);
        Assertions.assertNotNull(result.getId());
        Assertions.assertEquals("customer-1", result.getCustomerId());
        Assertions.assertEquals(OrderStatus.PENDING, result.getStatus());
        Assertions.assertTrue(result.getItems().isEmpty());
        verify(saveOrderPort, times(1)).save(any(Order.class));
    }

    @Test
    void should_pass_created_order_to_save_port() {
        // Arrange
        CreateOrderCommand command = new CreateOrderCommand("customer-2");
        when(saveOrderPort.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        createOrderService.createOrder(command);

        // Assert
        verify(saveOrderPort).save(argThat(order ->
                "customer-2".equals(order.getCustomerId())
                        && OrderStatus.PENDING == order.getStatus()
        ));
    }
}
