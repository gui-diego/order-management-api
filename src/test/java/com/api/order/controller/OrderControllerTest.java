package com.api.order.controller;

import com.api.category.entity.Category;
import com.api.category.repository.CategoryRepository;
import com.api.order.dto.OrderResponse;
import com.api.order.entity.Order;
import com.api.order.repository.OrderRepository;
import com.api.product.entity.Product;
import com.api.product.repository.ProductRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateOrderAndReturnCorrectId() throws Exception {

        Category category = new Category();
        category.setName("ELETRÔNICO");

        category = categoryRepository.save(category);

        Product product = new Product();
        product.setDescription("TV DA MARCA XYZ");
        product.setPrice(BigDecimal.valueOf(1500));
        product.setStock(10);
        product.setActive(true);
        product.setCategory(category);

        product = productRepository.save(product);

        String request = """
            {
                "items": [
                    {
                        "productId": %d,
                        "quantity": 2
                    }
                ]
            }
            """.formatted(product.getId());

        String response = mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.items[0].product.id").value(product.getId()))
                .andExpect(jsonPath("$.items[0].quantity").value(2))
                .andExpect(jsonPath("$.items[0].unitPrice").value(1500))
                .andExpect(jsonPath("$.items[0].subtotal").value(3000))
                .andExpect(jsonPath("$.total").value(3000))
                .andReturn()
                .getResponse()
                .getContentAsString();

        OrderResponse orderResponse =
                objectMapper.readValue(response, OrderResponse.class);

        Order order = orderRepository.findById(orderResponse.id())
                .orElseThrow();

        assertEquals(order.getId(), orderResponse.id());
    }
}
