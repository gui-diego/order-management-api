package com.api.order.service;

import com.api.exception.BadRequestException;
import com.api.exception.ResourceNotFoundException;
import com.api.order.dto.OrderRequest;
import com.api.order.dto.OrderResponse;
import com.api.order.entity.Order;
import com.api.order.enums.StatusOrder;
import com.api.order.repository.OrderRepository;
import com.api.orderItem.dto.OrderItemDTO;
import com.api.orderItem.entity.OrderItem;
import com.api.orderItem.dto.OrderItemResponse;
import com.api.product.dto.ProductOrderResponse;
import com.api.product.entity.Product;
import com.api.product.service.ProductService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

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
        List<OrderItemDTO> itemsDTO = validateOrderItems(request);
        Order orderSaved = processOrder(itemsDTO);
        List<OrderItemResponse> itemsResponse = getOrderItemResponses(orderSaved);
        return new OrderResponse(orderSaved.getId(), orderSaved.getCreatedAt(), orderSaved.getStatus(), orderSaved.getTotal(), itemsResponse);
    }

    private List<OrderItemDTO> validateOrderItems(OrderRequest request) {
        List<OrderItemDTO> itemsDTO = request.items();
        if (itemsDTO == null || itemsDTO.isEmpty()) {
            throw new BadRequestException("Lista de pedidos não pode ser nula ou vazia");
        }

        Set<Integer> uniqueProductIds = new HashSet<>();
        for (OrderItemDTO orderItemDTO : itemsDTO) {
            if (!uniqueProductIds.add(orderItemDTO.productId())) {
                throw new BadRequestException(
                        "Não é permitido adicionar o mesmo produto mais de uma vez ao pedido: "
                                + orderItemDTO.productId()
                );
            }
        }
        return itemsDTO;
    }

    private Order processOrder(List<OrderItemDTO> itemsDTO) {
        BigDecimal totalPrice = BigDecimal.ZERO;
        List<OrderItem> items = new ArrayList<>();

        for (OrderItemDTO item : itemsDTO) {
            Product product = productService.getById(item.productId());
            Integer quantity = item.quantity();

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

        for (OrderItem item : items) {
            int updatedRows = productService.decrementStock(item.getProduct().getId(), item.getQuantity());

            if (updatedRows == 0) {
                throw new BadRequestException("Estoque insuficiente para o produto: " + item.getProduct().getDescription());
            }
        }

        Order order = new Order();
        order.setCreatedAt(LocalDateTime.now());
        order.setStatus(StatusOrder.PROCESSING);
        items.forEach( item -> item.setOrder(order));
        order.setItems(items);
        order.setTotal(totalPrice);
        Order orderSaved = repository.save(order);
        orderSaved.setStatus(StatusOrder.COMPLETED);
        return orderSaved;
    }

    private List<OrderItemResponse> getOrderItemResponses(Order orderSaved) {
        return orderSaved.getItems()
                .stream()
                .map(item -> new OrderItemResponse(
                        item.getId(),
                        new ProductOrderResponse(item.getProduct().getId(), item.getProduct().getDescription(), item.getProduct().getPrice()),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getSubtotal()
                ))
                .toList();
    }

    public OrderResponse getById(Integer id) {
        Optional<Order> orderOptional = repository.findById(id);

        if (orderOptional.isEmpty()) {
            throw new ResourceNotFoundException("Não foi possível encontrar um pedido associado ao id informado: " + id);
        }

        Order order = orderOptional.get();
        List<OrderItemResponse> orderItemResponse = getOrderItemResponses(order);

        return new OrderResponse(order.getId(), order.getCreatedAt(), order.getStatus(), order.getTotal(), orderItemResponse);
    }
}
