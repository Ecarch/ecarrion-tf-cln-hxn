package com.mfpe.model.vo;

import java.util.Objects;
import java.util.UUID;

/**
 * Identificador tipodo para las ordenes.
 */
public record OrderId(UUID value) {

    public OrderId {
        Objects.requireNonNull(value, "OrderId value must not be null");
    }

    public static OrderId generate() {
        return new OrderId(UUID.randomUUID());
    }

    public static OrderId of(String uuid) {
        return new OrderId(UUID.fromString(uuid));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
