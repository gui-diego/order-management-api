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

        return getProductResponse(saved, product);
    }

    public Product getById(Integer id) {
        Product product = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado com o ID: " + id));

        if (!product.isActive()) {
            throw new BadRequestException("Não foi possível retornar pois o produto informado está inativo");
        }

        return product;
    }

    public void delete(Integer id) {
        Product product = getById(id);
        repository.deleteById(product.getId());
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

        return getProductResponse(updated, product);
    }

    private static ProductResponse getProductResponse(Product saved, Product product) {
        return new ProductResponse(
                saved.getId(),
                saved.getDescription(),
                product.getStock(),
                product.getPrice()
        );
    }

    public void decrementStock(Integer productId, Integer quantity) {
        repository.decrementStock(productId, quantity);
    }
}
