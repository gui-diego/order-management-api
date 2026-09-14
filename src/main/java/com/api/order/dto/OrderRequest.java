package com.api.order.dto;

import com.api.orderItem.dto.OrderItemDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record OrderRequest(
        List<@NotNull(message = "Os itens do pedido não podem ser nulos") @Valid OrderItemDTO> items) {
}
