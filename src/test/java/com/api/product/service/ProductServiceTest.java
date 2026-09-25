package com.api.product.service;

import com.api.category.dto.CategoryResponse;
import com.api.category.service.CategoryService;
import com.api.exception.BadRequestException;
import com.api.product.dto.ProductResponse;
import com.api.product.entity.Product;
import com.api.product.repository.ProductRepository;
import com.api.category.entity.Category;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository repository;

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private ProductService productService;

    @Test
    void shouldListOnlyActiveProductsByDefault() {
        Pageable pageable = PageRequest.of(0, 20);
        when(repository.findByActiveTrue(pageable)).thenReturn(new PageImpl<>(List.of(activeProduct())));

        var page = productService.list(null, false, pageable);

        assertEquals(1, page.getTotalElements());
        verify(repository).findByActiveTrue(pageable);
        verify(repository, never()).findAll(pageable);
    }

    @Test
    void shouldListInactiveProductsWhenRequested() {
        Pageable pageable = PageRequest.of(0, 20);
        Product inactive = activeProduct();
        inactive.setActive(false);
        when(repository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(inactive)));

        var page = productService.list(null, true, pageable);

        assertEquals(1, page.getTotalElements());
        assertFalse(page.getContent().getFirst().active());
    }

    @Test
    void shouldFilterActiveProductsByCategory() {
        Pageable pageable = PageRequest.of(0, 20);
        when(categoryService.getById(4)).thenReturn(new CategoryResponse(4, "ELETRÔNICO"));
        when(repository.findByActiveTrueAndCategory_Id(4, pageable))
                .thenReturn(new PageImpl<>(List.of(activeProduct())));

        var page = productService.list(4, false, pageable);

        assertEquals(1, page.getTotalElements());
        verify(repository).findByActiveTrueAndCategory_Id(4, pageable);
    }

    @Test
    void shouldReturnInactiveProductById() {
        Product inactive = activeProduct();
        inactive.setActive(false);
        when(repository.findById(1)).thenReturn(java.util.Optional.of(inactive));

        ProductResponse response = productService.getResponseById(1);

        assertEquals(false, response.active());
    }

    @Test
    void shouldRejectInactiveProductWhenItIsUsedInAnOrder() {
        Product inactive = activeProduct();
        inactive.setActive(false);
        when(repository.findById(1)).thenReturn(java.util.Optional.of(inactive));

        assertThrows(BadRequestException.class, () -> productService.getById(1));
    }

    @Test
    void shouldDeleteInactiveProduct() {
        Product inactive = activeProduct();
        inactive.setActive(false);
        when(repository.findById(1)).thenReturn(java.util.Optional.of(inactive));

        productService.delete(1);

        verify(repository).deleteById(1);
    }

    private Product activeProduct() {
        Category category = new Category();
        category.setId(4);

        Product product = new Product();
        product.setId(1);
        product.setCategory(category);
        product.setDescription("TV");
        product.setStock(10);
        product.setPrice(new BigDecimal("10.00"));
        product.setActive(true);
        return product;
    }
}
