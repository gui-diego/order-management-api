package com.api.order.dto;

import com.api.orderItem.dto.OrderItemDTO;

import java.util.List;

public record OrderRequest(List<OrderItemDTO> items) {
}
