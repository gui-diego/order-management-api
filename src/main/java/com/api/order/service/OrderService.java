package com.api.order.service;

import com.api.exception.BadRequestException;
import com.api.order.dto.OrderRequest;
import com.api.order.dto.OrderResponse;
import com.api.order.entity.Order;
import com.api.order.enums.StatusOrder;
import com.api.order.repository.OrderRepository;
import com.api.orderItem.dto.OrderItemDTO;
import com.api.orderItem.entity.OrderItem;
import com.api.orderItem.entity.OrderItemResponse;
import com.api.product.dto.ProductResponse;
import com.api.product.entity.Product;
import com.api.product.service.ProductService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class OrderService {

    private final OrderRepository repository;
    private final ProductService productService;

    public OrderService(OrderRepository repository,
                        ProductService productService) {
        this.repository = repository;
        this.productService = productService;
    }

    public OrderResponse createOrder(OrderRequest request) {

        List<OrderItemDTO> itemsDTO = request.items();
        if (itemsDTO == null || itemsDTO.isEmpty()) {
            throw new BadRequestException("Lista de pedidos não pode ser nula ou vazia");
        }

        BigDecimal totalPrice = BigDecimal.ZERO;
        List<OrderItem> items = new ArrayList<>();

        for (OrderItemDTO item : itemsDTO) {
            Product product = productService.getById(item.productId());

            Integer quantity = item.quantity();

            if (quantity == 0 || quantity < 0) {
                throw new BadRequestException("Não é possível seguir com o pedido, a quantidade dos itens deve ser maior que zero");
            }

            if (quantity > product.getStock()) {
                throw new BadRequestException("Estoque insuficiente para o produto: " + product.getDescription());
            }

            BigDecimal subTotal = product.getPrice().multiply(BigDecimal.valueOf(quantity));
            totalPrice = totalPrice.add(subTotal);

            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setSubtotal(subTotal);
            orderItem.setQuantity(quantity);
            orderItem.setUnitPrice(product.getPrice());
            items.add(orderItem);
        }

        Order order = new Order();
        order.setCreatedAt(LocalDateTime.now());
        order.setStatus(StatusOrder.PROCESSING);
        items.forEach( item -> item.setOrder(order));
        order.setItems(items);
        order.setTotal(totalPrice);

        Order orderSaved = repository.save(order);

        for (OrderItem item : orderSaved.getItems()) {
            productService.decrementStock(item.getProduct().getId(), item.getQuantity());
        }

        orderSaved.setStatus(StatusOrder.COMPLETED);

        List<OrderItemResponse> itemsResponse = orderSaved.getItems()
                .stream()
                .map(item -> new OrderItemResponse(
                   item.getId(),
                   new ProductResponse(item.getProduct().getId(), item.getProduct().getDescription(), null, item.getProduct().getPrice()),
                   item.getQuantity(),
                   item.getUnitPrice(),
                   item.getSubtotal()
                ))
                .toList();

        // dps de realizar todas as operacoes atomicas no banco vou createOrder.setStatus(StatusOrder.COMPLETED);
    return new OrderResponse(orderSaved.getId(), orderSaved.getCreatedAt(), orderSaved.getStatus(), orderSaved.getTotal(), itemsResponse);
    }

}
