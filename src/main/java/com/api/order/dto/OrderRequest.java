package com.api.order.dto;

import com.api.orderItem.dto.OrderItemDTO;
import jakarta.validation.Valid;

import java.util.List;

public record OrderRequest(@Valid List<OrderItemDTO> items) {
}
