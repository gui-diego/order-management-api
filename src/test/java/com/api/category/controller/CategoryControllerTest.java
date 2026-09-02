package com.api.category.controller;


import com.api.category.dto.CategoryRequest;
import com.api.category.service.CategoryService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoryService categoryService;

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
                        post("/category")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isCreated());
    }

    @Test
    void shouldGetCategoryById() throws Exception {

        int id = createCategory();

        mockMvc.perform(get("/category/" + id))
                .andExpect(status().isOk());
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
                        put("/category")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isOk());
    }

    @Test
    void shouldDeleteCategory() throws Exception {

        int id = createCategory();

        mockMvc.perform(delete("/categodsry/" + id))
                .andExpect(status().isNoContent());
    }
}
