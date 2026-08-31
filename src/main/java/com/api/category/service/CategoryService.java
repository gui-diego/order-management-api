package com.api.category.service;

import com.api.category.dto.CategoryRequest;
import com.api.category.dto.CategoryResponse;
import com.api.category.entity.Category;
import com.api.category.repository.CategoryRepository;
import com.api.exception.BadRequestException;
import com.api.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CategoryService {
    private final CategoryRepository repository;

    public CategoryService(CategoryRepository repository) {
        this.repository = repository;
    }

    public CategoryResponse save(CategoryRequest request) {
        Category category = new Category();
        category.setName(request.name());
        Category saved = repository.save(category);

        return new CategoryResponse(
                saved.getId(),
                saved.getName()
        );
    }

    public CategoryResponse getById(Integer id) {
        Category category = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada com o ID: " + id));

        return new CategoryResponse(
                category.getId(),
                category.getName()
        );
    }

    public void delete(Integer id) {
        CategoryResponse category = getById(id);
        repository.deleteById(category.id());
    }

    public CategoryResponse update(CategoryRequest request) {
        if (request.id() == null) {
            throw new BadRequestException("ID não pode ser nulo");
        }

        Category category = repository.findById(request.id())
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada com o ID: " + request.id()));

        category.setName(request.name());
        Category updated = repository.save(category);

        return new CategoryResponse(
                updated.getId(),
                updated.getName()
        );
    }
}
