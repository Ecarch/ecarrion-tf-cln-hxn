package com.mfpe.adapter.out.jpa;

import com.mfpe.model.entity.Order;
import com.mfpe.model.entity.OrderItem;
import com.mfpe.model.enums.OrderStatus;
import com.mfpe.model.vo.Money;
import com.mfpe.model.vo.OrderId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test de integración JPA.
 *
 * @DataJpaTest levanta únicamente el context de JPA:
 * - Configura H2 en memoria automáticamente
 * - Registra entidades JPA (@Entity)
 * - NO levanta controllers, services ni el contexto completo de Spring
 *
 * @Import(OrderMapper.class) inyecta el mapper como bean real (no mock),
 * así probamos la conversión dominio ↔ JPA junto con la persistencia real.
 */
@DataJpaTest
@Import(OrderMapper.class)
public class OrderRepositoryAdapterIT {

    @Autowired
    private SpringDataOrderRepository springRepo;

    @Autowired
    private OrderMapper mapper;

    private OrderRepositoryAdapter buildAdapter() {
        return new OrderRepositoryAdapter(springRepo, mapper);
    }

    // TODO clean DB before each test.

    // ── findById ────────────────────────────────────────────────────

    @Test
    void findById_shouldReturnSavedOrder() {
        OrderRepositoryAdapter adapter = buildAdapter();
        Order order = Order.create("customer-4");
        Order saved = adapter.save(order);

        Optional<Order> found = adapter.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals(saved.getId().toString(), found.get().getId().toString());
        assertEquals("customer-4", found.get().getCustomerId());
        assertEquals(OrderStatus.PENDING, found.get().getStatus());
    }

    @Test
    void findById_shouldReturnEmptyForNonExistentId() {
        OrderRepositoryAdapter adapter = buildAdapter();
        OrderId nonExistent = OrderId.generate();

        Optional<Order> found = adapter.findById(nonExistent);

        assertTrue(found.isEmpty());
    }

    // ── save ────────────────────────────────────────────────────────

    @Test
    void save_shouldPersistOrderInDatabase() {
        OrderRepositoryAdapter adapter = buildAdapter();
        Order order = Order.create("customer-1");

        Order saved = adapter.save(order);

        assertNotNull(saved.getId());
        assertEquals("customer-1", saved.getCustomerId());
        assertEquals(OrderStatus.PENDING, saved.getStatus());
        assertNotNull(saved.getCreatedAt());
    }

    @Test
    void save_shouldPersistOrderWithItems() {
        OrderRepositoryAdapter adapter = buildAdapter();
        Order order = Order.create("customer-2");
        order.addItem(new OrderItem("PROD-1", "Laptop", 2,
                Money.of(new BigDecimal("100.00"), "EUR")));

        Order saved = adapter.save(order);

        // Verificamos directamente en BD que el ítem se guardó
        var jpaEntity = springRepo.findById(saved.getId().toString());
        assertTrue(jpaEntity.isPresent());
        assertEquals(1, jpaEntity.get().getItems().size());
        assertEquals("PROD-1", jpaEntity.get().getItems().getFirst().getProductId());
    }

    @Test
    void save_shouldUpdateExistingOrder() {
        OrderRepositoryAdapter adapter = buildAdapter();
        Order order = Order.create("customer-3");
        adapter.save(order);

        // Añadimos un ítem y volvemos a guardar (update)
        order.addItem(new OrderItem("PROD-2", "Mouse", 1,
                Money.of(new BigDecimal("25.00"), "EUR")));
        Order updated = adapter.save(order);

        assertEquals(1, updated.getItems().size());
        assertEquals("PROD-2", updated.getItems().getFirst().getProductId());
    }

}
