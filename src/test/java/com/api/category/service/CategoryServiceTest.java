package com.api.category.service;

import com.api.category.entity.Category;
import com.api.category.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository repository;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    void shouldListCategories() {
        Category category = new Category();
        category.setId(1);
        category.setName("ELETRÔNICO");

        Pageable pageable = PageRequest.of(0, 20);
        when(repository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(category)));

        var page = categoryService.list(pageable);

        assertEquals(1, page.getTotalElements());
        assertEquals("ELETRÔNICO", page.getContent().getFirst().name());
    }
}
