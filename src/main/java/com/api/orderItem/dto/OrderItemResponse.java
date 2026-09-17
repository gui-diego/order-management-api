package com.api.orderItem.dto;

import com.api.product.dto.ProductOrderResponse;

import java.math.BigDecimal;

public record OrderItemResponse(Integer id, ProductOrderResponse product, Integer quantity, BigDecimal unitPrice, BigDecimal subtotal) {
}
