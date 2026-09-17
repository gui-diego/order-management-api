package com.api.order.service;

import com.api.category.entity.Category;
import com.api.exception.BadRequestException;
import com.api.exception.ResourceNotFoundException;
import com.api.order.dto.OrderRequest;
import com.api.order.dto.OrderResponse;
import com.api.order.entity.Order;
import com.api.order.repository.OrderRepository;
import com.api.orderItem.dto.OrderItemDTO;
import com.api.product.entity.Product;
import com.api.product.service.ProductService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository repository;

    @Mock
    private ProductService productService;

    @InjectMocks
    private OrderService orderService;

    @Test
    void shouldCreateOrderWithCorrectTotal() {
        Category category = new Category();
        category.setId(1);
        category.setName("ELETRÔNICO");

        Product product = new Product();
        product.setId(1);
        product.setCategory(category);
        product.setDescription("TV DA MARCA XYZ");
        product.setStock(10);
        product.setPrice(BigDecimal.valueOf(1500));
        product.setActive(true);

        when(productService.getById(product.getId()))
                .thenReturn(product);

        List<OrderItemDTO> items = new ArrayList<>();
        items.add(new OrderItemDTO(product.getId(), 2));
        OrderRequest request = new OrderRequest(items);

        Mockito.when(productService.decrementStock(product.getId(), 2)).thenReturn(1);

        when(repository.save(any(Order.class))).thenAnswer(invocation -> {
            return invocation.<Order>getArgument(0);
        });

        OrderResponse orderResponse = orderService.createOrder(request);

        assertEquals(BigDecimal.valueOf(3000), orderResponse.total());
    }

    @Test
    void shouldNotCreateOrderWhenProductDoesNotExist() {
        when(productService.getById(999))
                .thenThrow(new ResourceNotFoundException("Produto não encontrado"));

        OrderRequest request = new OrderRequest(
                List.of(new OrderItemDTO(999, 2))
        );

        assertThrows(
                ResourceNotFoundException.class,
                () -> orderService.createOrder(request)
        );

        verify(repository, never()).save(any(Order.class));
    }

    @Test
    void shouldNotCreateOrderWhenProductIsInactive() {
        when(productService.getById(1))
                .thenThrow(new BadRequestException(
                        "Não foi possível retornar pois o produto informado está inativo"));

        OrderRequest request = new OrderRequest(
                List.of(new OrderItemDTO(1, 2))
        );

        assertThrows(
                BadRequestException.class,
                () -> orderService.createOrder(request)
        );

        verify(repository, never()).save(any(Order.class));
        verify(productService, never()).decrementStock(any(), any());
    }

    @Test
    void shouldNotCreateOrderWhenStockIsInsufficient() {
        Product product = new Product();
        product.setId(1);
        product.setDescription("TV DA MARCA XYZ");
        product.setStock(3);
        product.setPrice(BigDecimal.valueOf(1500));
        product.setActive(true);

        when(productService.getById(product.getId())).thenReturn(product);

        OrderRequest request = new OrderRequest(
                List.of(new OrderItemDTO(product.getId(), 5))
        );

        assertThrows(
                BadRequestException.class,
                () -> orderService.createOrder(request)
        );

        verify(repository, never()).save(any(Order.class));
    }

    @Test
    void shouldDecrementStockWhenCreatingOrder() {
        Category category = new Category();
        category.setId(1);
        category.setName("ELETRÔNICO");

        Product product = new Product();
        product.setId(1);
        product.setCategory(category);
        product.setDescription("TV DA MARCA XYZ");
        product.setStock(10);
        product.setPrice(BigDecimal.valueOf(1500));
        product.setActive(true);

        when(productService.getById(product.getId()))
                .thenReturn(product);

        List<OrderItemDTO> items = new ArrayList<>();
        items.add(new OrderItemDTO(product.getId(), 2));
        OrderRequest request = new OrderRequest(items);

        Mockito.when(productService.decrementStock(product.getId(), 2)).thenReturn(1);

        when(repository.save(any(Order.class))).thenAnswer(invocation -> {
            return invocation.<Order>getArgument(0);
        });

        orderService.createOrder(request);

        verify(productService).decrementStock(product.getId(), 2);
    }

    @Test
    void shouldCreateOrderWithMultipleProducts() {
        Category category = new Category();
        category.setId(1);
        category.setName("ELETRÔNICO");

        Product productOne = new Product();
        productOne.setId(1);
        productOne.setCategory(category);
        productOne.setDescription("TV DA MARCA XYZ");
        productOne.setStock(10);
        productOne.setPrice(BigDecimal.valueOf(1500));
        productOne.setActive(true);

        Product productTwo = new Product();
        productTwo.setId(2);
        productTwo.setCategory(category);
        productTwo.setDescription("TV DA MARCA ABC");
        productTwo.setStock(10);
        productTwo.setPrice(BigDecimal.valueOf(500));
        productTwo.setActive(true);

        when(productService.getById(productOne.getId()))
                .thenReturn(productOne);

        when(productService.getById(productTwo.getId()))
                .thenReturn(productTwo);

        List<OrderItemDTO> items = new ArrayList<>();
        items.add(new OrderItemDTO(productOne.getId(), 2));
        items.add(new OrderItemDTO(productTwo.getId(), 3));
        OrderRequest request = new OrderRequest(items);

        Mockito.when(productService.decrementStock(productOne.getId(), 2)).thenReturn(1);
        Mockito.when(productService.decrementStock(productTwo.getId(), 3)).thenReturn(1);


        when(repository.save(any(Order.class))).thenAnswer(invocation -> {
            return invocation.<Order>getArgument(0);
        });

        OrderResponse orderResponse = orderService.createOrder(request);

        assertEquals(BigDecimal.valueOf(4500), orderResponse.total());
    }
}
