package com.mfpe.adapter.out.payment;

import com.mfpe.model.vo.Money;
import com.mfpe.port.out.PaymentGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SimulatedPaymentGateway implements PaymentGateway {

    private static final Logger log =
            LoggerFactory.getLogger(SimulatedPaymentGateway.class);

    @Override
    public boolean processPayment(String orderId, Money amount) {
        log.info("[PAYMENT] Processing {} {} for order {}", amount.amount(), amount.currency(), orderId);
        if (Math.random() < 0.1) {
            log.error("[PAYMENT] Failed to process payment for order {}", orderId);
            return false;
        }
        return true;
    }
}
