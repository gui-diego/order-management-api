package com.api.product.controller;

import com.api.auth.AuthAccess;
import com.api.exception.ErrorResponse;
import com.api.product.dto.ProductCreateRequest;
import com.api.product.dto.ProductResponse;
import com.api.product.dto.ProductStockRequest;
import com.api.product.dto.ProductUpdateRequest;
import com.api.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    @Operation(summary = "Create a product")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Product successfully created",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProductResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid product data",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Category not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @PostMapping
    public ResponseEntity<ProductResponse> save(@Valid @RequestBody ProductCreateRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(service.save(request));
    }

    @Operation(summary = "Update a product")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Product successfully updated",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProductResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid product data",
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
            )
    })
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> update(@PathVariable("id") Integer id,
                                                  @Valid @RequestBody ProductUpdateRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @Operation(summary = "Adjust product stock")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Stock successfully adjusted",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProductResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Zero quantity or insufficient stock to remove",
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
            )
    })
    @PatchMapping("/{id}/stock")
    public ResponseEntity<ProductResponse> adjustStock(@PathVariable("id") Integer id,
                                                       @Valid @RequestBody ProductStockRequest request) {
        return ResponseEntity.ok(service.adjustStock(id, request));
    }

    @Operation(summary = "List products")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Products successfully retrieved"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Category not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @SecurityRequirements
    @GetMapping
    public ResponseEntity<Page<ProductResponse>> list(
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(defaultValue = "false") boolean includeInactive,
            @PageableDefault(size = 20, sort = "id") Pageable pageable,
            Authentication authentication) {
        return ResponseEntity.ok(service.list(categoryId, includeInactive, AuthAccess.isAdmin(authentication), pageable));
    }

    @Operation(summary = "Get a product by ID")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Product successfully retrieved",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProductResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Product not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @SecurityRequirements
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable("id") Integer id,
                                                          Authentication authentication) {
        return ResponseEntity.ok(service.getResponseById(id, AuthAccess.isAdmin(authentication)));
    }

    @Operation(summary = "Delete a product")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Product successfully deleted"
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
                    description = "Product cannot be deleted because it has associated order items",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
