package com.mfpe.exception;

public class OrderNotFoundException extends OrderDomainException {

    public OrderNotFoundException(String orderId) {
        super("Order with id [" + orderId + "] was not found.");
    }
}
