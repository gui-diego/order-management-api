package com.api.product.controller;

import com.api.category.dto.CategoryRequest;
import com.api.category.service.CategoryService;
import com.api.product.repository.ProductRepository;
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
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ProductRepository productRepository;

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
          "stock": 10
        }
        """.formatted(categoryId);

        mockMvc.perform(
                        post("/products")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.price").value(700))
                .andExpect(jsonPath("$.description").value("TV DA MARCA XYZ"))
                .andExpect(jsonPath("$.stock").value(10))
                .andExpect(jsonPath("$.categoryId").value(categoryId))
                .andExpect(jsonPath("$.active").value(true));
    }

    private ProductTestData createProduct() throws Exception {

        int categoryId = createCategory();

        String json = """
        {
          "price": 700,
          "description": "TV DA MARCA XYZ",
          "category": %d,
          "stock": 10
        }
        """.formatted(categoryId);

        MvcResult result = mockMvc.perform(
                        post("/products")
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

        mockMvc.perform(get("/products/" + data.productId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(data.productId()))
                .andExpect(jsonPath("$.description").value("TV DA MARCA XYZ"))
                .andExpect(jsonPath("$.price").value(700))
                .andExpect(jsonPath("$.categoryId").value(data.categoryId()))
                .andExpect(jsonPath("$.stock").value(10))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void shouldUpdateProduct() throws Exception {

        ProductTestData data = createProduct();

        String json = """
        {
          "id": %d,
          "price": 800,
          "description": "TV ATUALIZADA",
          "stock": 20,
          "active": true
        }
        """.formatted(data.productId());

        mockMvc.perform(
                        put("/products")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(data.productId()))
                .andExpect(jsonPath("$.price").value(800))
                .andExpect(jsonPath("$.description").value("TV ATUALIZADA"))
                .andExpect(jsonPath("$.stock").value(20))
                .andExpect(jsonPath("$.categoryId").value(data.categoryId()))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void shouldDeleteProduct() throws Exception {

        ProductTestData data = createProduct();

        mockMvc.perform(delete("/products/" + data.productId()))
                .andExpect(status().isNoContent());

        assertTrue(productRepository.findById(data.productId()).isEmpty());
    }
}
