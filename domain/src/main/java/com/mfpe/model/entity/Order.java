package com.mfpe.model.entity;

import com.mfpe.exception.EmptyOrderException;
import com.mfpe.exception.OrderAlreadyCancelledException;
import com.mfpe.exception.OrderAlreadyPaidException;
import com.mfpe.exception.OrderDomainException;
import com.mfpe.model.enums.OrderStatus;
import com.mfpe.model.vo.Money;
import com.mfpe.model.vo.OrderId;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Entidad: representa una orden de compra.
 * Tiene un ciclo de vida propio, con su identidad y estado.
 */
public class Order {

    private final OrderId id;
    private final String customerId;
    private final List<OrderItem> items;
    private OrderStatus status;
    private Money total;
    private final LocalDateTime  createdAt;

    private Order(OrderId id, String customerId) {
        this.id         = id;
        this.customerId = customerId;
        this.items      = new ArrayList<>();
        this.status     = OrderStatus.PENDING;
        this.createdAt  = LocalDateTime.now();
    }

    private Order(OrderId id, String customerId, OrderStatus status,
                  Money total, LocalDateTime createdAt, List<OrderItem> items) {
        this.id         = id;
        this.customerId = customerId;
        this.status     = status;
        this.total      = total;
        this.createdAt  = createdAt;
        this.items      = new ArrayList<>(items);
    }

    // ── Factory ──────────────────────────────────────────────────

    public static Order create(String customerId) {
        if (customerId == null || customerId.isBlank())
            throw new OrderDomainException("customerId must not be blank");
        return new Order(OrderId.generate(), customerId);
    }

    public static Order reconstitute(OrderId id, String customerId, OrderStatus status,
                                     Money total, LocalDateTime createdAt, List<OrderItem> items) {
        return new Order(id, customerId, status, total, createdAt, items);
    }

    // ── Comportamiento de dominio ─────────────────────────────────

    public void addItem(OrderItem item) {
        Objects.requireNonNull(item, "item must not be null");
        if (status != OrderStatus.PENDING)
            throw new OrderDomainException(
                    "Cannot add items to an order in status: " + status);
        items.add(item);
    }

    public void calculateTotal() {
        if (items.isEmpty())
            throw new EmptyOrderException();
        String currency = items.getFirst().getUnitPrice().currency();
        Money sum = items.stream()
                .map(OrderItem::calculateSubtotal)
                .reduce(Money.zero(currency), Money::add);
        if (!sum.isGreaterThanZero())
            throw new OrderDomainException(
                    "Order total must be greater than zero");
        this.total = sum;
    }

    public void pay() {
        if (status == OrderStatus.PAID)
            throw new OrderAlreadyPaidException(id.toString());
        if (status == OrderStatus.CANCELLED)
            throw new OrderDomainException(
                    "Cannot pay a cancelled order: " + id);
        calculateTotal();
        this.status = OrderStatus.PAID;
    }

    public void cancel() {
        if (status == OrderStatus.PAID)
            throw new OrderDomainException(
                    "Cannot cancel a paid order: " + id);
        if (status == OrderStatus.CANCELLED)
            throw new OrderAlreadyCancelledException(id.toString());
        this.status = OrderStatus.CANCELLED;
    }

    // ── Getters ───────────────────────────────────────────────────

    public OrderId getId(){ return id; }
    public String getCustomerId(){ return customerId; }
    public OrderStatus getStatus(){ return status; }
    public Money getTotal(){ return total; }
    public LocalDateTime getCreatedAt(){ return createdAt; }
    public List<OrderItem> getItems(){
        return Collections.unmodifiableList(items);
    }
}