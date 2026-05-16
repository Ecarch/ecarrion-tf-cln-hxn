package com.mfpe.service;

import com.mfpe.command.AddItemToOrderCommand;
import com.mfpe.exception.OrderDomainException;
import com.mfpe.exception.OrderNotFoundException;
import com.mfpe.model.entity.Order;
import com.mfpe.model.vo.OrderId;
import com.mfpe.port.out.FindOrderByIdPort;
import com.mfpe.port.out.InventoryService;
import com.mfpe.port.out.SaveOrderPort;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddItemToOrderServiceTest {

    @Mock
    private SaveOrderPort saveOrderPort;

    @Mock
    private FindOrderByIdPort findOrderByIdPort;

    @Mock
    private InventoryService inventoryService;

    @InjectMocks
    private AddItemToOrderService addItemToOrderService;

    private Order existingOrder;
    private String orderId;

    @BeforeEach
    void setUp() {
        existingOrder = Order.create("customer-1");
        orderId = existingOrder.getId().toString();
    }

    @Test
    void should_add_item_to_order_successfully() {
        // Arrange
        AddItemToOrderCommand command = new AddItemToOrderCommand(
                orderId, "prod-1", "Widget", 2, new BigDecimal("10.00"), "USD");
        when(findOrderByIdPort.findById(OrderId.of(orderId))).thenReturn(Optional.of(existingOrder));
        when(inventoryService.isAvailable("prod-1", 2)).thenReturn(true);
        when(saveOrderPort.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Order result = addItemToOrderService.addItem(command);

        // Assert
        Assertions.assertEquals(1, result.getItems().size());
        Assertions.assertEquals("prod-1", result.getItems().getFirst().getProductId());
        Assertions.assertEquals(2, result.getItems().getFirst().getQuantity());
        Assertions.assertNotNull(result.getTotal());
        verify(saveOrderPort).save(existingOrder);
    }

    @Test
    void should_throw_when_order_not_found() {
        // Arrange
        AddItemToOrderCommand command = new AddItemToOrderCommand(
                orderId, "prod-1", "Widget", 1, BigDecimal.TEN, "USD");
        when(findOrderByIdPort.findById(OrderId.of(orderId))).thenReturn(Optional.empty());

        // Act & Assert
        Assertions.assertThrows(OrderNotFoundException.class,
                () -> addItemToOrderService.addItem(command));
        verify(saveOrderPort, never()).save(any());
    }

    @Test
    void should_throw_when_inventory_not_available() {
        // Arrange
        AddItemToOrderCommand command = new AddItemToOrderCommand(
                orderId, "prod-1", "Widget", 5, BigDecimal.TEN, "USD");
        when(findOrderByIdPort.findById(OrderId.of(orderId))).thenReturn(Optional.of(existingOrder));
        when(inventoryService.isAvailable("prod-1", 5)).thenReturn(false);

        // Act & Assert
        OrderDomainException ex = Assertions.assertThrows(OrderDomainException.class,
                () -> addItemToOrderService.addItem(command));
        Assertions.assertTrue(ex.getMessage().contains("not available"));
        verify(saveOrderPort, never()).save(any());
    }

    @Test
    void should_calculate_total_after_adding_item() {
        // Arrange
        AddItemToOrderCommand command = new AddItemToOrderCommand(
                orderId, "prod-1", "Widget", 3, new BigDecimal("5.00"), "USD");
        when(findOrderByIdPort.findById(OrderId.of(orderId))).thenReturn(Optional.of(existingOrder));
        when(inventoryService.isAvailable("prod-1", 3)).thenReturn(true);
        when(saveOrderPort.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Order result = addItemToOrderService.addItem(command);

        // Assert
        Assertions.assertEquals(0, new BigDecimal("15.00").compareTo(result.getTotal().amount()));
    }

    @Test
    void should_verify_inventory_before_adding_item() {
        // Arrange
        AddItemToOrderCommand command = new AddItemToOrderCommand(
                orderId, "prod-1", "Widget", 2, BigDecimal.TEN, "USD");
        when(findOrderByIdPort.findById(OrderId.of(orderId))).thenReturn(Optional.of(existingOrder));
        when(inventoryService.isAvailable("prod-1", 2)).thenReturn(true);
        when(saveOrderPort.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        addItemToOrderService.addItem(command);

        // Assert
        verify(inventoryService).isAvailable("prod-1", 2);
    }
}
