package com.api.order.service;

import com.api.exception.BadRequestException;
import com.api.exception.ConflictException;
import com.api.exception.ForbiddenException;
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
import com.api.user.entity.UserAccount;
import com.api.user.enums.Role;
import com.api.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);
    private static final Set<StatusOrder> CANCELABLE = EnumSet.of(
            StatusOrder.PENDING,
            StatusOrder.PAYMENT_APPROVED
    );

    private final OrderRepository repository;
    private final ProductService productService;
    private final OrderPaymentProcessor paymentProcessor;
    private final UserRepository userRepository;

    public OrderService(OrderRepository repository,
                        ProductService productService,
                        OrderPaymentProcessor paymentProcessor,
                        UserRepository userRepository) {
        this.repository = repository;
        this.productService = productService;
        this.paymentProcessor = paymentProcessor;
        this.userRepository = userRepository;
    }

    @Transactional
    public OrderResponse createOrder(OrderRequest request, String customerEmail) {
        UserAccount customer = userRepository.findByEmail(customerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário autenticado não encontrado"));
        List<OrderItemDTO> itemsDTO = validateOrderItems(request);
        Order orderSaved = processOrder(itemsDTO, customer);
        return toResponse(orderSaved);
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

    private Order processOrder(List<OrderItemDTO> itemsDTO, UserAccount customer) {
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
        order.setStatus(StatusOrder.PENDING);
        order.setCustomer(customer);
        items.forEach( item -> item.setOrder(order));
        order.setItems(items);
        order.setTotal(totalPrice);
        try {
            return repository.save(order);
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException("Não é permitido adicionar o mesmo produto mais de uma vez ao pedido");
        }
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

    @Transactional
    public OrderResponse getById(Integer id, String email) {
        UserAccount currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário autenticado não encontrado"));
        Order order = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar um pedido associado ao id informado: " + id
                ));

        if (currentUser.getRole() == Role.CUSTOMER) {
            UserAccount owner = order.getCustomer();
            boolean ownsOrder = owner != null && owner.getId().equals(currentUser.getId());
            if (!ownsOrder) {
                throw new ForbiddenException("Você só pode consultar o próprio pedido");
            }
        }

        return toResponse(order);
    }

    @Transactional
    public Page<OrderResponse> list(StatusOrder status, Pageable pageable) {
        Page<Order> orders = status == null
                ? repository.findAll(pageable)
                : repository.findByStatus(status, pageable);
        return orders.map(this::toResponse);
    }

    public int processOrdersWithStatusPending() {
        List<Integer> pendingOrderIds = repository.findIdsByStatus(StatusOrder.PENDING);
        int processedCount = 0;

        for (Integer orderId : pendingOrderIds) {
            try {
                boolean claimed = paymentProcessor.claim(orderId);
                if (!claimed) {
                    continue;
                }

                boolean finished = paymentProcessor.finish(orderId);
                if (finished) {
                    processedCount++;
                }
            } catch (RuntimeException ex) {
                log.error("Falha ao processar pagamento do pedido {}", orderId, ex);
            }
        }

        List<Integer> processingOrderIds = repository.findIdsByStatus(StatusOrder.PROCESSING);
        for (Integer orderId : processingOrderIds) {
            try {
                boolean finished = paymentProcessor.finish(orderId);
                if (finished) {
                    processedCount++;
                }
            } catch (RuntimeException ex) {
                log.error("Falha ao retomar pagamento do pedido {}", orderId, ex);
            }
        }

        return processedCount;
    }

    @Transactional
    public OrderResponse cancelOrder(Integer id) {
        Order order = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar um pedido associado ao id informado: " + id
                ));

        if (!CANCELABLE.contains(order.getStatus())) {
            throw new BadRequestException("Não é possível cancelar o pedido no status atual: " + order.getStatus());
        }

        for (OrderItem item : order.getItems()) {
            int updatedRows = productService.incrementStock(item.getProduct().getId(), item.getQuantity());
            if (updatedRows == 0) {
                throw new BadRequestException("Não foi possível restaurar o estoque do produto: " + item.getProduct().getId());
            }
        }

        order.setStatus(StatusOrder.CANCELED);
        return toResponse(order);
    }

    @Transactional
    public OrderResponse shipOrder(Integer id) {
        Order order = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar um pedido associado ao id informado: " + id
                ));

        if (order.getStatus() != StatusOrder.PAYMENT_APPROVED) {
            throw new BadRequestException("Não é possível enviar um pedido no status atual: " + order.getStatus());
        }

        order.setStatus(StatusOrder.SHIPPED);
        return toResponse(order);
    }

    @Transactional
    public OrderResponse deliverOrder(Integer id) {
        Order order = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar um pedido associado ao id informado: " + id
                ));

        if (order.getStatus() != StatusOrder.SHIPPED) {
            throw new BadRequestException("O pedido não pode ser entregue no status atual: " + order.getStatus());
        }

        order.setStatus(StatusOrder.DELIVERED);
        return toResponse(order);
    }

    private OrderResponse toResponse(Order order) {
        Integer customerId = order.getCustomer() == null ? null : order.getCustomer().getId();
        return new OrderResponse(
                order.getId(),
                customerId,
                order.getCreatedAt(),
                order.getStatus(),
                order.getTotal(),
                order.getPaymentReason(),
                getOrderItemResponses(order)
        );
    }
}
