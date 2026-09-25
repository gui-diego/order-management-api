package com.api.order.service;

import com.api.exception.BadRequestException;
import com.api.order.entity.Order;
import com.api.order.enums.StatusOrder;
import com.api.order.repository.OrderRepository;
import com.api.orderItem.entity.OrderItem;
import com.api.payment.dto.PaymentResult;
import com.api.product.entity.Product;
import com.api.product.service.ProductService;
import com.api.payment.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderPaymentProcessorTest {

    @Mock
    private OrderRepository repository;

    @Mock
    private ProductService productService;

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private OrderPaymentProcessor processor;

    @Test
    void shouldNotClaimOrderThatIsNoLongerPending() {
        when(repository.markAsProcessing(1, StatusOrder.PROCESSING, StatusOrder.PENDING)).thenReturn(0);

        boolean claimed = processor.claim(1);

        assertFalse(claimed);
        verify(paymentService, never()).processPayment(any());
    }

    @Test
    void shouldClaimPendingOrder() {
        when(repository.markAsProcessing(1, StatusOrder.PROCESSING, StatusOrder.PENDING)).thenReturn(1);

        boolean claimed = processor.claim(1);

        assertTrue(claimed);
    }

    @Test
    void shouldApproveProcessingOrderWithoutRestoringStock() {
        Order order = processingOrder();
        when(repository.findById(1)).thenReturn(Optional.of(order));
        when(paymentService.processPayment(order.getTotal()))
                .thenReturn(new PaymentResult(true, "Pagamento aprovado pelo gateway simulado"));

        boolean finished = processor.finish(1);

        assertTrue(finished);
        assertEquals(StatusOrder.PAYMENT_APPROVED, order.getStatus());
        assertEquals("Pagamento aprovado pelo gateway simulado", order.getPaymentReason());
        verify(productService, never()).incrementStock(any(), any());
    }

    @Test
    void shouldRestoreStockWhenPaymentIsRejected() {
        Order order = processingOrder();
        when(repository.findById(1)).thenReturn(Optional.of(order));
        when(paymentService.processPayment(order.getTotal()))
                .thenReturn(new PaymentResult(false, "Pagamento recusado: valor acima do limite do gateway simulado"));
        when(productService.incrementStock(3, 2)).thenReturn(1);

        boolean finished = processor.finish(1);

        assertTrue(finished);
        assertEquals(StatusOrder.PAYMENT_FAILED, order.getStatus());
        assertEquals("Pagamento recusado: valor acima do limite do gateway simulado", order.getPaymentReason());
        verify(productService).incrementStock(3, 2);
    }

    @Test
    void shouldKeepProcessingStatusWhenStockCannotBeRestored() {
        Order order = processingOrder();
        when(repository.findById(1)).thenReturn(Optional.of(order));
        when(paymentService.processPayment(order.getTotal()))
                .thenReturn(new PaymentResult(false, "Pagamento recusado: valor acima do limite do gateway simulado"));
        when(productService.incrementStock(3, 2)).thenReturn(0);

        assertThrows(BadRequestException.class, () -> processor.finish(1));
        assertEquals(StatusOrder.PROCESSING, order.getStatus());
    }

    private Order processingOrder() {
        Product product = new Product();
        product.setId(3);
        product.setDescription("TV");

        OrderItem item = new OrderItem();
        item.setProduct(product);
        item.setQuantity(2);

        Order order = new Order();
        order.setId(1);
        order.setStatus(StatusOrder.PROCESSING);
        order.setTotal(new BigDecimal("15000.00"));
        order.setItems(List.of(item));
        return order;
    }
}
