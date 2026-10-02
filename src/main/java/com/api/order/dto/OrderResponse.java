package com.api.order.dto;

import com.api.order.enums.StatusOrder;
import com.api.orderItem.dto.OrderItemResponse;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Integer id,
        @JsonInclude(JsonInclude.Include.NON_NULL) Integer customerId,
        LocalDateTime createdAt,
        StatusOrder status,
        BigDecimal total,
        @JsonInclude(JsonInclude.Include.NON_NULL) String paymentReason,
        List<OrderItemResponse> items) {
}
