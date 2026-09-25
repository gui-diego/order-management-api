package com.api.payment.dto;

public record PaymentResult(boolean approved, String reason) {
}
