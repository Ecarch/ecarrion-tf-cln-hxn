package com.mfpe.model.entity;

import com.mfpe.exception.OrderDomainException;
import com.mfpe.model.vo.Money;

import java.util.Objects;

/**
 * Entidad: representa un item de una orden.
 * No tiene un ciclo de vida propio fuera de una Orden.
 */
public class OrderItem {

    private final String productId;
    private final String productName;
    private final int    quantity;
    private final Money unitPrice;

    public OrderItem(String productId, String productName,
                     int quantity, Money unitPrice) {
        Objects.requireNonNull(productId,   "productId must not be null");
        Objects.requireNonNull(productName, "productName must not be null");
        Objects.requireNonNull(unitPrice,   "unitPrice must not be null");
        if (productId.isBlank())
            throw new OrderDomainException("productId must not be blank");
        if (quantity < 1)
            throw new OrderDomainException(
                    "quantity must be at least 1, got: " + quantity);
        this.productId   = productId;
        this.productName = productName;
        this.quantity    = quantity;
        this.unitPrice   = unitPrice;
    }

    /** Precio unitario multiplicado por cantidad. */
    public Money calculateSubtotal() {
        return unitPrice.multiply(quantity);
    }

    public String getProductId()   { return productId;   }
    public String getProductName() { return productName; }
    public int    getQuantity()    { return quantity;    }
    public Money  getUnitPrice()   { return unitPrice;   }
}
