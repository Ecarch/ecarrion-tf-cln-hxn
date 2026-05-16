package com.mfpe.integration;

import com.mfpe.config.TestSecurityConfig;
import com.mfpe.exception.OrderDomainException;
import com.mfpe.exception.OrderNotFoundException;
import com.mfpe.model.entity.Order;
import com.mfpe.model.enums.OrderStatus;
import com.mfpe.model.vo.OrderId;
import com.mfpe.port.in.AddItemToOrderUseCase;
import com.mfpe.port.in.CancelOrderUseCase;
import com.mfpe.port.in.CreateOrderUseCase;
import com.mfpe.port.in.PayOrderUseCase;
import com.mfpe.port.out.FindOrderByIdPort;
import com.mfpe.command.AddItemToOrderCommand;
import com.mfpe.command.CreateOrderCommand;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test de integración de casos de uso.
 *
 * @SpringBootTest levanta el contexto completo de la aplicación:
 * - Use cases reales (CreateOrderService, AddItemToOrderService, etc.)
 * - Adaptadores JPA reales (OrderRepositoryAdapter + SpringDataOrderRepository)
 * - H2 en memoria (no hay base de datos externa necesaria)
 * - Servicios simulados reales (SimulatedInventoryService, SimulatedPaymentGateway)
 * Estos tests verifican que todas las capas están correctamente ensambladas.
 *
 * @DirtiesContext resetea el contexto entre tests para evitar contaminación de datos.
 */

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Import(TestSecurityConfig.class)
public class UseCaseIntegrationTest {

    @Autowired
    private CreateOrderUseCase createOrderUseCase;

    @Autowired
    private AddItemToOrderUseCase addItemToOrderUseCase;

    @Autowired
    private PayOrderUseCase payOrderUseCase;

    @Autowired
    private CancelOrderUseCase cancelOrderUseCase;

    @Autowired
    private FindOrderByIdPort findOrderByIdPort;

    // ── createOrder ─────────────────────────────────────────────────

    @Test
    void createOrder_shouldPersistAndReturnOrder() {
        Order order = createOrderUseCase.createOrder(
                new CreateOrderCommand("customer-integration-1"));

        assertNotNull(order.getId());
        assertEquals("customer-integration-1", order.getCustomerId());
        assertEquals(OrderStatus.PENDING, order.getStatus());

        // Verificamos que realmente se persistió en BD
        Optional<Order> fromDb = findOrderByIdPort.findById(order.getId());
        assertTrue(fromDb.isPresent());
        assertEquals(order.getId().toString(), fromDb.get().getId().toString());
    }

    // ── addItem ─────────────────────────────────────────────────────

    @Test
    void addItem_shouldPersistItemOnExistingOrder() {
        // Primero creamos la orden
        Order created = createOrderUseCase.createOrder(
                new CreateOrderCommand("customer-integration-2"));

        // Luego añadimos un ítem
        AddItemToOrderCommand cmd = new AddItemToOrderCommand(
                created.getId().toString(),
                "PROD-1", "Laptop", 2,
                new BigDecimal("100.00"), "EUR");

        Order withItem = addItemToOrderUseCase.addItem(cmd);

        assertEquals(1, withItem.getItems().size());
        assertEquals("PROD-1", withItem.getItems().getFirst().getProductId());
        assertNotNull(withItem.getTotal());
        assertEquals(new BigDecimal("200.00"), withItem.getTotal().amount());

        // Verificamos persistencia
        Optional<Order> fromDb = findOrderByIdPort.findById(created.getId());
        assertTrue(fromDb.isPresent());
        assertEquals(1, fromDb.get().getItems().size());
    }

    // ── payOrder ────────────────────────────────────────────────────

    @Test
    void payOrder_shouldThrowWhenOrderHasNoItems() {
        Order order = createOrderUseCase.createOrder(
                new CreateOrderCommand("customer-integration-4"));

        assertThrows(OrderDomainException.class,
                () -> payOrderUseCase.payOrder(order.getId().toString()));
    }

    // ── cancelOrder ─────────────────────────────────────────────────

    @Test
    void cancelOrder_shouldTransitionOrderToCancelled() {
        Order order = createOrderUseCase.createOrder(
                new CreateOrderCommand("customer-integration-5"));

        cancelOrderUseCase.cancelOrder(order.getId().toString());

        Optional<Order> cancelled = findOrderByIdPort.findById(order.getId());
        assertTrue(cancelled.isPresent());
        assertEquals(OrderStatus.CANCELLED, cancelled.get().getStatus());
    }

    // ── Flujo completo: create → addItem → cancel ────────────────────

    @Test
    void fullFlow_createAddItemAndCancel() {
        Order order = createOrderUseCase.createOrder(
                new CreateOrderCommand("customer-flow-1"));

        addItemToOrderUseCase.addItem(new AddItemToOrderCommand(
                order.getId().toString(),
                "PROD-3", "Keyboard", 1,
                new BigDecimal("75.00"), "USD"));

        cancelOrderUseCase.cancelOrder(order.getId().toString());

        Optional<Order> result = findOrderByIdPort.findById(order.getId());
        assertTrue(result.isPresent());
        assertEquals(OrderStatus.CANCELLED, result.get().getStatus());
        assertEquals(1, result.get().getItems().size());
    }

}
