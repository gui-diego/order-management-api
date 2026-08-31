package com.api.category.controller;

import com.api.category.dto.CategoryRequest;
import com.api.category.dto.CategoryResponse;
import com.api.category.service.CategoryService;
import com.api.product.dto.ProductRequest;
import com.api.product.dto.ProductResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/category")
public class CategoryController {

    private final CategoryService service;

    public CategoryController(CategoryService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> save(@Valid @RequestBody CategoryRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(service.save(request));
    }

    @PutMapping
    public ResponseEntity<CategoryResponse> update (@Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.ok(service.update(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable("id") Integer id) {
        CategoryResponse response = service.getById(id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
