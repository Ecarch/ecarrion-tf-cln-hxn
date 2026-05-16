package com.mfpe.model.vo;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Value Object: representa un monto con su moneda.
 * Inmutable. Se compara por su valor, nunca por una referencia.
 */
public record Money(BigDecimal amount, String currency) {

    public Money {
        Objects.requireNonNull(amount,   "amount must not be null");
        Objects.requireNonNull(currency, "currency must not be null");
        if (currency.isBlank())
            throw new IllegalArgumentException("currency must not be blank");
        if (amount.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("amount must not be negative");
    }

    public Money add(Money other) {
        if (!this.currency.equals(other.currency))
            throw new IllegalArgumentException(
                    "Cannot add amounts with different currencies: "
                            + this.currency + " vs " + other.currency);
        return new Money(this.amount.add(other.amount), this.currency);
    }

    public Money multiply(int quantity) {
        if (quantity < 0)
            throw new IllegalArgumentException("quantity must not be negative");
        return new Money(this.amount.multiply(BigDecimal.valueOf(quantity)), this.currency);
    }

    public boolean isGreaterThanZero() {
        return this.amount.compareTo(BigDecimal.ZERO) > 0;
    }

    public static Money of(BigDecimal amount, String currency) {
        return new Money(amount, currency);
    }

    public static Money zero(String currency) {
        return new Money(BigDecimal.ZERO, currency);
    }

}
