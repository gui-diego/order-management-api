package com.api.payment.service;

import com.api.payment.dto.PaymentResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PaymentService {

    static final String APPROVED_REASON = "Pagamento aprovado pelo gateway simulado";
    static final String ABOVE_LIMIT_REASON = "Pagamento recusado: valor acima do limite do gateway simulado";

    private final BigDecimal maxAmount;

    public PaymentService(@Value("${payment.simulated-max-amount}") BigDecimal maxAmount) {
        this.maxAmount = maxAmount;
    }

    public PaymentResult processPayment(BigDecimal total) {
        if (total.compareTo(maxAmount) > 0) {
            return new PaymentResult(false, ABOVE_LIMIT_REASON);
        }
        return new PaymentResult(true, APPROVED_REASON);
    }
}
