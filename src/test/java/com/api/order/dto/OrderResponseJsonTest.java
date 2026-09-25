package com.api.order.dto;

import com.api.order.enums.StatusOrder;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderResponseJsonTest {

    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    @Test
    void shouldOmitPaymentReasonWhenItIsNull() {
        OrderResponse response = new OrderResponse(
                1,
                LocalDateTime.of(2026, 9, 24, 21, 0),
                StatusOrder.PENDING,
                new BigDecimal("10.00"),
                null,
                List.of()
        );

        JsonNode json = jsonMapper.readTree(jsonMapper.writeValueAsString(response));

        assertFalse(json.has("paymentReason"));
        assertEquals(1, json.get("id").intValue());
    }

    @Test
    void shouldIncludePaymentReasonWhenItHasAValue() {
        OrderResponse response = new OrderResponse(
                1,
                LocalDateTime.of(2026, 9, 24, 21, 0),
                StatusOrder.PAYMENT_APPROVED,
                new BigDecimal("10.00"),
                "Pagamento aprovado pelo gateway simulado",
                List.of()
        );

        JsonNode json = jsonMapper.readTree(jsonMapper.writeValueAsString(response));

        assertTrue(json.has("paymentReason"));
        assertEquals("Pagamento aprovado pelo gateway simulado", json.get("paymentReason").asString());
    }
}
