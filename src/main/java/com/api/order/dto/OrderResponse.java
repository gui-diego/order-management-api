package com.api.order.dto;

import com.api.order.enums.StatusOrder;
import com.api.orderItem.entity.OrderItemResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(Integer id, LocalDateTime createdAt, StatusOrder status, BigDecimal total, List<OrderItemResponse> items) {
}
