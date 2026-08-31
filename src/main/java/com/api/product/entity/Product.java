package com.api.product.entity;

import com.api.category.entity.Category;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "product")
@Getter
@Setter
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    private String description;

    private Integer stock;

    private BigDecimal price;

    private boolean active;
}
