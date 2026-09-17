package com.api.product.service;

import com.api.category.entity.Category;
import com.api.category.service.CategoryService;
import com.api.exception.BadRequestException;
import com.api.exception.ConflictException;
import com.api.exception.ResourceNotFoundException;
import com.api.product.dto.ProductCreateRequest;
import com.api.product.dto.ProductResponse;
import com.api.product.dto.ProductUpdateRequest;
import com.api.product.entity.Product;
import com.api.product.repository.ProductRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import java.math.RoundingMode;

@Service
public class ProductService {

    private final ProductRepository repository;
    private final CategoryService categoryService;

    public ProductService(ProductRepository repository,
                          CategoryService categoryService) {
        this.repository = repository;
        this.categoryService = categoryService;
    }

    public ProductResponse save(ProductCreateRequest request) {
        Product product = new Product();
        product.setDescription(request.description());
        product.setStock(request.stock());
        product.setPrice(request.price().setScale(2, RoundingMode.UNNECESSARY));
        product.setActive(true);
        categoryService.getById(request.category());
        Category category = new Category();
        category.setId(request.category());
        product.setCategory(category);
        Product saved = repository.save(product);

        return getProductResponse(saved);
    }

    public Product getById(Integer id) {
        Product product = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado com o ID: " + id));

        if (!product.isActive()) {
            throw new BadRequestException("Não foi possível retornar pois o produto informado está inativo");
        }

        return product;
    }

    public ProductResponse getResponseById(Integer id) {
        return getProductResponse(getById(id));
    }

    public void delete(Integer id) {
        Product product = getById(id);
        try {
            repository.deleteById(product.getId());
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException("Não é possível excluir o produto porque ele possui itens de pedido associados");
        }
    }

    public ProductResponse update(Integer id, ProductUpdateRequest request) {
        Product product = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado com o ID: " + id));

        product.setDescription(request.description());
        product.setPrice(request.price().setScale(2, RoundingMode.UNNECESSARY));
        product.setActive(request.active());
        product.setStock(request.stock());

        Product updated = repository.save(product);

        return getProductResponse(updated);
    }

    private static ProductResponse getProductResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getCategory().getId(),
                product.getDescription(),
                product.getStock(),
                product.getPrice(),
                product.isActive()
        );
    }

    public int decrementStock(Integer productId, Integer quantity) {
        return repository.decrementStock(productId, quantity);
    }
}
