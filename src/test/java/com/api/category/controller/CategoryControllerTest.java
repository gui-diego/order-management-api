package com.api.category.controller;


import com.api.category.dto.CategoryRequest;
import com.api.category.repository.CategoryRepository;
import com.api.category.service.CategoryService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CategoryRepository categoryRepository;

    private int createCategory() {
        CategoryRequest request = new CategoryRequest(null, "Eletrônicos");
        return categoryService.save(request).id();
    }

    @Test
    void shouldCreateCategory() throws Exception {

        String json = """
        {
         "name": "ELETRÔNICOS"
        }
        """;

        mockMvc.perform(
                        post("/categories")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("ELETRÔNICOS"));
    }

    @Test
    void shouldGetCategoryById() throws Exception {

        int id = createCategory();

        mockMvc.perform(get("/categories/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value("Eletrônicos"));
    }

    @Test
    void shouldUpdateCategory() throws Exception {

        int id = createCategory();

        String json = """
        {
            "id": %d,
            "name": "MODA"
        }
        """.formatted(id);

        mockMvc.perform(
                        put("/categories")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value("MODA"));
    }

    @Test
    void shouldDeleteCategory() throws Exception {

        int id = createCategory();

        mockMvc.perform(delete("/categories/" + id))
                .andExpect(status().isNoContent());

        assertTrue(categoryRepository.findById(id).isEmpty());
    }
}
