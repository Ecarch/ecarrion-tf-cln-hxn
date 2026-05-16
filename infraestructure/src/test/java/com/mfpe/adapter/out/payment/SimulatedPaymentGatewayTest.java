package com.mfpe.adapter.out.payment;

import com.mfpe.model.vo.Money;
import org.junit.jupiter.api.RepeatedTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class SimulatedPaymentGatewayTest {

    private final SimulatedPaymentGateway gateway = new SimulatedPaymentGateway();

    @RepeatedTest(20)
    void processPayment_shouldNotThrowException() {
        Money amount = Money.of(new BigDecimal("50.00"), "USD");

        assertDoesNotThrow(() -> gateway.processPayment("order-2", amount));
    }
}
