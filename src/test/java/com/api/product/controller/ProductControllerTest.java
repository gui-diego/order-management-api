package com.api.product.controller;

import com.api.category.dto.CategoryRequest;
import com.api.category.service.CategoryService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CategoryService categoryService;

    private record ProductTestData(int productId, int categoryId) {}

    private int createCategory() {
        CategoryRequest request = new CategoryRequest(null, "Eletrônicos");
        return categoryService.save(request).id();
    }

    @Test
    void shouldCreateProduct() throws Exception {

        int categoryId = createCategory();

        String json = """
        {
          "price": 700,
          "description": "TV DA MARCA XYZ",
          "category": %d,
          "stock": 10,
          "active": true
        }
        """.formatted(categoryId);

        mockMvc.perform(
                        post("/product")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isCreated());
    }

    private ProductTestData createProduct() throws Exception {

        int categoryId = createCategory();

        String json = """
        {
          "price": 700,
          "description": "TV DA MARCA XYZ",
          "category": %d,
          "stock": 10,
          "active": true
        }
        """.formatted(categoryId);

        MvcResult result = mockMvc.perform(
                        post("/product")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode response =
                objectMapper.readTree(result.getResponse().getContentAsString());

        int productId = response.get("id").asInt();

        return new ProductTestData(productId, categoryId);
    }

    @Test
    void shouldGetProductById() throws Exception {

        ProductTestData data = createProduct();

        mockMvc.perform(get("/product/" + data.productId()))
                .andExpect(status().isOk());
    }

    @Test
    void shouldUpdateProduct() throws Exception {

        ProductTestData data = createProduct();

        String json = """
        {
          "id": %d,
          "price": 800,
          "description": "TV ATUALIZADA",
          "category": %d,
          "stock": 20,
          "active": true
        }
        """.formatted(
                data.productId(),
                data.categoryId()
        );

        mockMvc.perform(
                        put("/product")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isOk());
    }

    @Test
    void shouldDeleteProduct() throws Exception {

        ProductTestData data = createProduct();

        mockMvc.perform(delete("/product/" + data.productId()))
                .andExpect(status().isNoContent());
    }
}
