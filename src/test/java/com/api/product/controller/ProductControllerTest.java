package com.api.product.controller;

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

    @Test
    void shouldCreateProduct() throws Exception {

        String json = """
            {
              "price": 700,
              "description": "TV DA MARCA XYZ",
              "category": 1,
              "stock": 10,
              "active": true
            }
            """;

        mockMvc.perform(
                        post("/product")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isCreated());
    }

    private int createProduct() throws Exception {

        String json = """
                {
                  "price": 700,
                  "description": "TV DA MARCA XYZ",
                  "category": 1,
                  "stock": 10,
                  "active": true
                }
                """;

        MvcResult result = mockMvc.perform(
                        post("/product")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode response =
                objectMapper.readTree(result.getResponse().getContentAsString());

        return response.get("id").asInt();
    }

    @Test
    void shouldGetProductById() throws Exception {

        int id = createProduct();

        mockMvc.perform(get("/product/" + id))
                .andExpect(status().isOk());
    }

    @Test
    void shouldUpdateProduct() throws Exception {

        int id = createProduct();

        String json = """
            {
              "id": %d,
              "price": 800,
              "description": "TV ATUALIZADA",
              "category": 1,
              "stock": 20,
              "active": true
            }
            """.formatted(id);

        mockMvc.perform(
                        put("/product")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isOk());
    }

    @Test
    void shouldDeleteProduct() throws Exception {

        int id = createProduct();

        mockMvc.perform(delete("/product/" + id))
                .andExpect(status().isNoContent());
    }
}
