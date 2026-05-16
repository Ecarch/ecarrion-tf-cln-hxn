package com.mfpe.exception;

/**
 * Clase base para excepciones relacionadas con el dominio de órdenes.
 */
public class OrderDomainException extends RuntimeException {
    public OrderDomainException(String message) {
        super(message);
    }
}
