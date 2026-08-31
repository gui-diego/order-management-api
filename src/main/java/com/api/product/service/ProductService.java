package com.api.product.service;

import com.api.category.entity.Category;
import com.api.exception.BadRequestException;
import com.api.exception.ResourceNotFoundException;
import com.api.product.dto.ProductRequest;
import com.api.product.dto.ProductResponse;
import com.api.product.entity.Product;
import com.api.product.repository.ProductRepository;
import org.springframework.stereotype.Service;

@Service
public class ProductService {

    private final ProductRepository repository;

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    public ProductResponse save(ProductRequest request) {
        Product product = new Product();
        product.setDescription(request.description());
        product.setStock(request.stock());
        product.setPrice(request.price());
        product.setActive(true);
        Category category = new Category();
        category.setId(request.category());
        product.setCategory(category);
        Product saved = repository.save(product);

        return new ProductResponse(
                saved.getId(),
                saved.getDescription()
        );
    }

    public ProductResponse getById(Integer id) {
        Product product = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado com o ID: " + id));

        return new ProductResponse(
                product.getId(),
                product.getDescription()
        );
    }

    public void delete(Integer id) {
        ProductResponse product = getById(id);
        repository.deleteById(product.id());
    }

    public ProductResponse update(ProductRequest request) {
        if (request.id() == null) {
            throw new BadRequestException("ID não pode ser nulo");
        }
        Product product = repository.findById(request.id())
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado com o ID: " + request.id()));

        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setActive(request.active());
        product.setStock(request.stock());

        Product updated = repository.save(product);

        return new ProductResponse(
                updated.getId(),
                updated.getDescription()
        );
    }
}
