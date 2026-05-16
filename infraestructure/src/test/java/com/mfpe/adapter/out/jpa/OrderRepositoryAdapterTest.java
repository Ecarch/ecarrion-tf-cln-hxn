package com.mfpe.adapter.out.jpa;

import com.mfpe.adapter.out.jpa.entity.OrderJpaEntity;
import com.mfpe.model.entity.Order;
import com.mfpe.model.entity.OrderItem;
import com.mfpe.model.enums.OrderStatus;
import com.mfpe.model.vo.Money;
import com.mfpe.model.vo.OrderId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderRepositoryAdapterTest {

    @Mock
    private SpringDataOrderRepository springRepo;

    @Mock
    private OrderMapper mapper;

    @InjectMocks
    private OrderRepositoryAdapter adapter;

    private Order createDomainOrder() {
        OrderId id = OrderId.of("550e8400-e29b-41d4-a716-446655440000");
        Money total = Money.of(new BigDecimal("200.00"), "EUR");
        OrderItem item = new OrderItem("PROD-1", "Laptop", 2,
                Money.of(new BigDecimal("100.00"), "EUR"));
        return Order.reconstitute(id, "customer-1", OrderStatus.PENDING,
                total, LocalDateTime.of(2025, 6, 1, 10, 0), List.of(item));
    }

    private OrderJpaEntity createJpaEntity() {
        OrderJpaEntity entity = new OrderJpaEntity();
        entity.setId("550e8400-e29b-41d4-a716-446655440000");
        entity.setCustomerId("customer-1");
        entity.setStatus(OrderStatus.PENDING);
        entity.setTotalAmount(new BigDecimal("200.00"));
        entity.setTotalCurrency("EUR");
        entity.setCreatedAt(LocalDateTime.of(2025, 6, 1, 10, 0));
        entity.setItems(List.of());
        return entity;
    }

    // ── save ────────────────────────────────────────────────────────

    @Test
    void save_shouldConvertToJpaSaveAndConvertBack() {
        Order domainOrder = createDomainOrder();
        OrderJpaEntity jpaEntity = createJpaEntity();

        when(mapper.toJpa(domainOrder)).thenReturn(jpaEntity);
        when(springRepo.save(jpaEntity)).thenReturn(jpaEntity);
        when(mapper.toDomain(jpaEntity)).thenReturn(domainOrder);

        Order result = adapter.save(domainOrder);

        assertNotNull(result);
        assertEquals("customer-1", result.getCustomerId());
        verify(mapper).toJpa(domainOrder);
        verify(springRepo).save(jpaEntity);
        verify(mapper).toDomain(jpaEntity);
    }

    // ── findById ────────────────────────────────────────────────────

    @Test
    void findById_shouldReturnDomainOrderWhenFound() {
        OrderId orderId = OrderId.of("550e8400-e29b-41d4-a716-446655440000");
        OrderJpaEntity jpaEntity = createJpaEntity();
        Order domainOrder = createDomainOrder();

        when(springRepo.findById("550e8400-e29b-41d4-a716-446655440000"))
                .thenReturn(Optional.of(jpaEntity));
        when(mapper.toDomain(jpaEntity)).thenReturn(domainOrder);

        Optional<Order> result = adapter.findById(orderId);

        assertTrue(result.isPresent());
        assertEquals("customer-1", result.get().getCustomerId());
        verify(springRepo).findById("550e8400-e29b-41d4-a716-446655440000");
        verify(mapper).toDomain(jpaEntity);
    }

    @Test
    void findById_shouldReturnEmptyWhenNotFound() {
        OrderId orderId = OrderId.of("550e8400-e29b-41d4-a716-446655440000");

        when(springRepo.findById("550e8400-e29b-41d4-a716-446655440000"))
                .thenReturn(Optional.empty());

        Optional<Order> result = adapter.findById(orderId);

        assertTrue(result.isEmpty());
        verify(springRepo).findById("550e8400-e29b-41d4-a716-446655440000");
        verify(mapper, never()).toDomain(any());
    }
}
