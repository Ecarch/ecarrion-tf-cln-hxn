package com.mfpe.adapter.out.jpa;

import com.mfpe.adapter.out.jpa.entity.OrderItemJpaEntity;
import com.mfpe.adapter.out.jpa.entity.OrderJpaEntity;
import com.mfpe.model.entity.Order;
import com.mfpe.model.entity.OrderItem;
import com.mfpe.model.enums.OrderStatus;
import com.mfpe.model.vo.Money;
import com.mfpe.model.vo.OrderId;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderMapperTest {

    private final OrderMapper mapper = new OrderMapper();

    // ── Helper para crear una Order de dominio ──────────────────────

    private Order createDomainOrder() {
        OrderId id = OrderId.generate();
        Money total = Money.of(new BigDecimal("200.00"), "EUR");
        OrderItem item = new OrderItem("PROD-1", "Laptop", 2,
                Money.of(new BigDecimal("100.00"), "EUR"));
        return Order.reconstitute(id, "customer-1", OrderStatus.PENDING,
                total, LocalDateTime.of(2025, 6, 1, 10, 0), List.of(item));
    }

    private Order createDomainOrderWithoutTotal() {
        OrderId id = OrderId.generate();
        return Order.reconstitute(id, "customer-2", OrderStatus.PENDING,
                null, LocalDateTime.of(2025, 6, 1, 10, 0), List.of());
    }

    // ── Helper para crear una OrderJpaEntity ────────────────────────

    private OrderJpaEntity createJpaEntity() {
        OrderJpaEntity entity = new OrderJpaEntity();
        entity.setId("550e8400-e29b-41d4-a716-446655440000");
        entity.setCustomerId("customer-1");
        entity.setStatus(OrderStatus.PAID);
        entity.setTotalAmount(new BigDecimal("300.00"));
        entity.setTotalCurrency("USD");
        entity.setCreatedAt(LocalDateTime.of(2025, 5, 15, 8, 30));

        OrderItemJpaEntity itemEntity = new OrderItemJpaEntity();
        itemEntity.setOrder(entity);
        itemEntity.setProductId("PROD-2");
        itemEntity.setProductName("Phone");
        itemEntity.setQuantity(3);
        itemEntity.setUnitPrice(new BigDecimal("100.00"));
        itemEntity.setCurrency("USD");

        entity.setItems(List.of(itemEntity));
        return entity;
    }

    // ── Tests toJpa ─────────────────────────────────────────────────

    @Test
    void toJpa_shouldMapBasicFields() {
        Order domainOrder = createDomainOrder();

        OrderJpaEntity jpaEntity = mapper.toJpa(domainOrder);

        assertEquals(domainOrder.getId().toString(), jpaEntity.getId());
        assertEquals("customer-1", jpaEntity.getCustomerId());
        assertEquals(OrderStatus.PENDING, jpaEntity.getStatus());
        assertEquals(domainOrder.getCreatedAt(), jpaEntity.getCreatedAt());
    }

    @Test
    void toJpa_shouldMapTotal() {
        Order domainOrder = createDomainOrder();

        OrderJpaEntity jpaEntity = mapper.toJpa(domainOrder);

        assertEquals(new BigDecimal("200.00"), jpaEntity.getTotalAmount());
        assertEquals("EUR", jpaEntity.getTotalCurrency());
    }

    @Test
    void toJpa_shouldHandleNullTotal() {
        Order domainOrder = createDomainOrderWithoutTotal();

        OrderJpaEntity jpaEntity = mapper.toJpa(domainOrder);

        assertNull(jpaEntity.getTotalAmount());
        assertNull(jpaEntity.getTotalCurrency());
    }

    @Test
    void toJpa_shouldMapItems() {
        Order domainOrder = createDomainOrder();

        OrderJpaEntity jpaEntity = mapper.toJpa(domainOrder);

        assertEquals(1, jpaEntity.getItems().size());
        OrderItemJpaEntity itemJpa = jpaEntity.getItems().getFirst();
        assertEquals("PROD-1", itemJpa.getProductId());
        assertEquals("Laptop", itemJpa.getProductName());
        assertEquals(2, itemJpa.getQuantity());
        assertEquals(new BigDecimal("100.00"), itemJpa.getUnitPrice());
        assertEquals("EUR", itemJpa.getCurrency());
    }

    @Test
    void toJpa_itemsShouldReferenceParentEntity() {
        Order domainOrder = createDomainOrder();

        OrderJpaEntity jpaEntity = mapper.toJpa(domainOrder);

        for (OrderItemJpaEntity item : jpaEntity.getItems()) {
            assertSame(jpaEntity, item.getOrder());
        }
    }

    // ── Tests toDomain ──────────────────────────────────────────────

    @Test
    void toDomain_shouldMapBasicFields() {
        OrderJpaEntity jpaEntity = createJpaEntity();

        Order domain = mapper.toDomain(jpaEntity);

        assertEquals("550e8400-e29b-41d4-a716-446655440000", domain.getId().toString());
        assertEquals("customer-1", domain.getCustomerId());
        assertEquals(OrderStatus.PAID, domain.getStatus());
        assertEquals(jpaEntity.getCreatedAt(), domain.getCreatedAt());
    }

    @Test
    void toDomain_shouldMapTotal() {
        OrderJpaEntity jpaEntity = createJpaEntity();

        Order domain = mapper.toDomain(jpaEntity);

        assertNotNull(domain.getTotal());
        assertEquals(new BigDecimal("300.00"), domain.getTotal().amount());
        assertEquals("USD", domain.getTotal().currency());
    }

    @Test
    void toDomain_shouldHandleNullTotal() {
        OrderJpaEntity jpaEntity = createJpaEntity();
        jpaEntity.setTotalAmount(null);
        jpaEntity.setTotalCurrency(null);

        Order domain = mapper.toDomain(jpaEntity);

        assertNull(domain.getTotal());
    }

    @Test
    void toDomain_shouldMapItems() {
        OrderJpaEntity jpaEntity = createJpaEntity();

        Order domain = mapper.toDomain(jpaEntity);

        assertEquals(1, domain.getItems().size());
        OrderItem item = domain.getItems().getFirst();
        assertEquals("PROD-2", item.getProductId());
        assertEquals("Phone", item.getProductName());
        assertEquals(3, item.getQuantity());
        assertEquals(new BigDecimal("100.00"), item.getUnitPrice().amount());
        assertEquals("USD", item.getUnitPrice().currency());
    }

    // ── Round-trip test ─────────────────────────────────────────────

    @Test
    void roundTrip_domainToJpaToDomain_shouldPreserveData() {
        Order original = createDomainOrder();

        OrderJpaEntity jpa = mapper.toJpa(original);
        Order restored = mapper.toDomain(jpa);

        assertEquals(original.getId().toString(), restored.getId().toString());
        assertEquals(original.getCustomerId(), restored.getCustomerId());
        assertEquals(original.getStatus(), restored.getStatus());
        assertEquals(original.getTotal().amount(), restored.getTotal().amount());
        assertEquals(original.getTotal().currency(), restored.getTotal().currency());
        assertEquals(original.getCreatedAt(), restored.getCreatedAt());
        assertEquals(original.getItems().size(), restored.getItems().size());
    }
}
