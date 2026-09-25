package com.api.payment.service;

import com.api.payment.dto.PaymentResult;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaymentServiceTest {

    private final PaymentService paymentService = new PaymentService(new BigDecimal("10000.00"));

    @Test
    void shouldApproveAmountWithinLimit() {
        PaymentResult result = paymentService.processPayment(new BigDecimal("10000.00"));

        assertTrue(result.approved());
        assertEquals(PaymentService.APPROVED_REASON, result.reason());
    }

    @Test
    void shouldRejectAmountAboveLimit() {
        PaymentResult result = paymentService.processPayment(new BigDecimal("10000.01"));

        assertFalse(result.approved());
        assertEquals(PaymentService.ABOVE_LIMIT_REASON, result.reason());
    }

}
