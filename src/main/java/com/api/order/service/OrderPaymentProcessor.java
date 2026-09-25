package com.api.order.service;

import com.api.exception.BadRequestException;
import com.api.exception.ResourceNotFoundException;
import com.api.order.entity.Order;
import com.api.order.enums.StatusOrder;
import com.api.order.repository.OrderRepository;
import com.api.orderItem.entity.OrderItem;
import com.api.payment.dto.PaymentResult;
import com.api.payment.service.PaymentService;
import com.api.product.service.ProductService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderPaymentProcessor {

    private final OrderRepository repository;
    private final ProductService productService;
    private final PaymentService paymentService;

    public OrderPaymentProcessor(OrderRepository repository,
                                 ProductService productService,
                                 PaymentService paymentService) {
        this.repository = repository;
        this.productService = productService;
        this.paymentService = paymentService;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean claim(Integer orderId) {
        int updatedRows = repository.markAsProcessing(
                orderId,
                StatusOrder.PROCESSING,
                StatusOrder.PENDING
        );

        return updatedRows == 1;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean finish(Integer orderId) {
        Order order = repository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar um pedido associado ao id informado: " + orderId
                ));

        if (order.getStatus() != StatusOrder.PROCESSING) {
            return false;
        }

        PaymentResult paymentResult = paymentService.processPayment(order.getTotal());
        boolean approved = paymentResult.approved();
        String reason = paymentResult.reason();

        if (approved) {
            order.setPaymentReason(reason);
            order.setStatus(StatusOrder.PAYMENT_APPROVED);
            return true;
        }

        restoreReservedStock(order);
        order.setPaymentReason(reason);
        order.setStatus(StatusOrder.PAYMENT_FAILED);
        return true;
    }

    private void restoreReservedStock(Order order) {
        for (OrderItem item : order.getItems()) {
            Integer productId = item.getProduct().getId();
            Integer quantity = item.getQuantity();

            int updatedRows = productService.incrementStock(productId, quantity);
            if (updatedRows == 0) {
                throw new BadRequestException(
                        "Não foi possível restaurar o estoque do produto: " + productId
                );
            }
        }
    }
}
