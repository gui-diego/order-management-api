package com.api.orderItem.entity;

import com.api.product.dto.ProductResponse;

import java.math.BigDecimal;

public record OrderItemResponse(Integer id, ProductResponse product, Integer quantity, BigDecimal unitPrice, BigDecimal subtotal) {
}
