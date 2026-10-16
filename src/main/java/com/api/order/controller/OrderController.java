package com.api.order.controller;

import com.api.exception.ErrorResponse;
import com.api.order.dto.OrderRequest;
import com.api.order.dto.OrderResponse;
import com.api.order.enums.StatusOrder;
import com.api.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    @Operation(summary = "Create an order")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Order successfully created",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = OrderResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid order data",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Product not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "The same product was added more than once to the order",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @PostMapping
    public ResponseEntity<OrderResponse> save(@Valid @RequestBody OrderRequest request,
                                              Authentication authentication) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(service.createOrder(request, authentication.getName()));
    }

    @Operation(summary = "List orders")
    @ApiResponse(responseCode = "200", description = "Orders successfully retrieved")
    @GetMapping
    public ResponseEntity<Page<OrderResponse>> list(
            @RequestParam(required = false) StatusOrder status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(service.list(status, pageable));
    }

    @Operation(summary = "Get an order by ID")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Order successfully retrieved",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = OrderResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Order not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getById(@PathVariable("id") Integer id,
                                                 Authentication authentication) {
        return ResponseEntity.ok(service.getById(id, authentication.getName()));
    }

    @Operation(summary = "Cancel an order by ID")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Order canceled successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = OrderResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Order cannot be canceled in the current status",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Customers can only cancel their own orders",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Order not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @PostMapping("/{id}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable("id") Integer id,
                                                     Authentication authentication) {
        return ResponseEntity.ok(service.cancelOrder(id, authentication.getName()));
    }

    @Operation(summary = "Ship an order by ID")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Order shipped successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = OrderResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Order cannot be shipped in the current status",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Order not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @PostMapping("/{id}/ship")
    public ResponseEntity<OrderResponse> shipOrder(@PathVariable("id") Integer id) {
        return ResponseEntity.ok(service.shipOrder(id));
    }

    @Operation(summary = "Deliver an order by ID")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Order delivered successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = OrderResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Order cannot be delivered in the current status",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Order not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @PostMapping("/{id}/deliver")
    public ResponseEntity<OrderResponse> deliverOrder(@PathVariable("id") Integer id) {
        return ResponseEntity.ok(service.deliverOrder(id));
    }
}
