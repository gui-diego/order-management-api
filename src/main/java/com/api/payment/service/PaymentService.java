package com.api.payment.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PaymentService {

    public boolean processPayment(BigDecimal total) {
        return true;
    }

}
