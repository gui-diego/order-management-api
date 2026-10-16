package com.api.order.service;

import com.api.category.entity.Category;
import com.api.exception.BadRequestException;
import com.api.exception.ConflictException;
import com.api.exception.ResourceNotFoundException;
import com.api.order.dto.OrderRequest;
import com.api.order.dto.OrderResponse;
import com.api.order.entity.Order;
import com.api.order.enums.StatusOrder;
import com.api.order.repository.OrderRepository;
import com.api.orderItem.dto.OrderItemDTO;
import com.api.orderItem.entity.OrderItem;
import com.api.product.entity.Product;
import com.api.product.service.ProductService;
import com.api.user.entity.UserAccount;
import com.api.user.enums.Role;
import com.api.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository repository;

    @Mock
    private ProductService productService;

    @Mock
    private OrderPaymentProcessor paymentProcessor;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OrderService orderService;

    private void stubCustomer() {
        UserAccount customer = new UserAccount();
        customer.setId(7);
        customer.setEmail("customer@order.local");
        when(userRepository.findByEmail("customer@order.local")).thenReturn(Optional.of(customer));
    }

    private void stubAdmin() {
        UserAccount admin = new UserAccount();
        admin.setId(1);
        admin.setEmail("admin@order.local");
        admin.setRole(Role.ADMIN);
        when(userRepository.findByEmail("admin@order.local")).thenReturn(Optional.of(admin));
    }

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

        stubCustomer();
        OrderResponse orderResponse = orderService.createOrder(request, "customer@order.local");

        assertEquals(BigDecimal.valueOf(3000), orderResponse.total());
    }

    @Test
    void shouldNotCreateOrderWhenProductDoesNotExist() {
        when(productService.getById(999))
                .thenThrow(new ResourceNotFoundException("Produto não encontrado"));

        OrderRequest request = new OrderRequest(
                List.of(new OrderItemDTO(999, 2))
        );

        stubCustomer();
        assertThrows(
                ResourceNotFoundException.class,
                () -> orderService.createOrder(request, "customer@order.local")
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

        stubCustomer();
        assertThrows(
                BadRequestException.class,
                () -> orderService.createOrder(request, "customer@order.local")
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

        stubCustomer();
        assertThrows(
                BadRequestException.class,
                () -> orderService.createOrder(request, "customer@order.local")
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

        stubCustomer();
        orderService.createOrder(request, "customer@order.local");

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

        stubCustomer();
        OrderResponse orderResponse = orderService.createOrder(request, "customer@order.local");

        assertEquals(BigDecimal.valueOf(4500), orderResponse.total());
    }

    @Test
    void shouldKeepProcessingRemainingOrdersWhenOnePaymentFails() {
        when(repository.findIdsByStatus(StatusOrder.PENDING)).thenReturn(List.of(1, 2));
        when(repository.findIdsByStatus(StatusOrder.PROCESSING)).thenReturn(List.of());
        when(paymentProcessor.claim(1)).thenReturn(true);
        when(paymentProcessor.finish(1)).thenThrow(new RuntimeException("falha inesperada"));
        when(paymentProcessor.claim(2)).thenReturn(true);
        when(paymentProcessor.finish(2)).thenReturn(true);

        int processed = orderService.processOrdersWithStatusPending();

        assertEquals(1, processed);
        verify(paymentProcessor).finish(2);
    }

    @Test
    void shouldFinishProcessingOrderWithoutClaimingAgain() {
        when(repository.findIdsByStatus(StatusOrder.PENDING)).thenReturn(List.of());
        when(repository.findIdsByStatus(StatusOrder.PROCESSING)).thenReturn(List.of(9));
        when(paymentProcessor.finish(9)).thenReturn(true);

        int processed = orderService.processOrdersWithStatusPending();

        assertEquals(1, processed);
        verify(paymentProcessor, never()).claim(any());
        verify(paymentProcessor).finish(9);
    }

    @Test
    void shouldCancelPendingOrderAndRestoreStock() {
        Order order = orderWithStatus(StatusOrder.CANCELED);
        stubAdmin();
        when(repository.markAsCanceled(eq(10), eq(StatusOrder.CANCELED), any())).thenReturn(1);
        when(repository.findById(10)).thenReturn(Optional.of(order));
        when(productService.incrementStock(5, 2)).thenReturn(1);

        OrderResponse response = orderService.cancelOrder(10, "admin@order.local");

        assertEquals(StatusOrder.CANCELED, response.status());
        verify(productService).incrementStock(5, 2);
    }

    @Test
    void shouldNotCancelOrderWhenPaymentAlreadyFailed() {
        Order order = orderWithStatus(StatusOrder.PAYMENT_FAILED);
        stubAdmin();
        when(repository.markAsCanceled(eq(10), eq(StatusOrder.CANCELED), any())).thenReturn(0);
        when(repository.findById(10)).thenReturn(Optional.of(order));

        assertThrows(BadRequestException.class, () -> orderService.cancelOrder(10, "admin@order.local"));
        verify(productService, never()).incrementStock(any(), any());
    }

    @Test
    void shouldNotCancelOrderWhilePaymentIsProcessing() {
        Order order = orderWithStatus(StatusOrder.PROCESSING);
        stubAdmin();
        when(repository.markAsCanceled(eq(10), eq(StatusOrder.CANCELED), any())).thenReturn(0);
        when(repository.findById(10)).thenReturn(Optional.of(order));

        assertThrows(BadRequestException.class, () -> orderService.cancelOrder(10, "admin@order.local"));
        verify(productService, never()).incrementStock(any(), any());
    }

    @Test
    void shouldShipOnlyAfterPaymentApproval() {
        Order pending = orderWithStatus(StatusOrder.PENDING);
        when(repository.findById(10)).thenReturn(Optional.of(pending));
        assertThrows(BadRequestException.class, () -> orderService.shipOrder(10));

        Order approved = orderWithStatus(StatusOrder.PAYMENT_APPROVED);
        when(repository.findById(10)).thenReturn(Optional.of(approved));
        OrderResponse response = orderService.shipOrder(10);

        assertEquals(StatusOrder.SHIPPED, response.status());
    }

    @Test
    void shouldDeliverOnlyAfterShipment() {
        Order approved = orderWithStatus(StatusOrder.PAYMENT_APPROVED);
        when(repository.findById(10)).thenReturn(Optional.of(approved));
        assertThrows(BadRequestException.class, () -> orderService.deliverOrder(10));

        Order shipped = orderWithStatus(StatusOrder.SHIPPED);
        when(repository.findById(10)).thenReturn(Optional.of(shipped));
        OrderResponse response = orderService.deliverOrder(10);

        assertEquals(StatusOrder.DELIVERED, response.status());
    }

    @Test
    void shouldRejectOrderWhenTheSameProductIsPersistedTwice() {
        Product product = new Product();
        product.setId(1);
        product.setDescription("TV");
        product.setStock(10);
        product.setPrice(BigDecimal.valueOf(10));
        product.setActive(true);

        when(productService.getById(1)).thenReturn(product);
        when(productService.decrementStock(1, 1)).thenReturn(1);
        when(repository.save(any(Order.class))).thenThrow(new DataIntegrityViolationException("duplicate"));

        OrderRequest request = new OrderRequest(List.of(new OrderItemDTO(1, 1)));

        stubCustomer();
        assertThrows(ConflictException.class, () -> orderService.createOrder(request, "customer@order.local"));
    }

    private Order orderWithStatus(StatusOrder status) {
        Product product = new Product();
        product.setId(5);
        product.setDescription("TV");
        product.setPrice(new BigDecimal("10.00"));

        OrderItem item = new OrderItem();
        item.setProduct(product);
        item.setQuantity(2);
        item.setUnitPrice(new BigDecimal("10.00"));
        item.setSubtotal(new BigDecimal("20.00"));

        Order order = new Order();
        order.setId(10);
        order.setCreatedAt(LocalDateTime.now());
        order.setStatus(status);
        order.setTotal(new BigDecimal("20.00"));
        order.setItems(List.of(item));
        return order;
    }
}
