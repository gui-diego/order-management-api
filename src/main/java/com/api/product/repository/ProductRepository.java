package com.api.product.repository;
import com.api.product.entity.Product;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Integer> {

    @Transactional
    @Modifying
    @Query("""
    UPDATE Product p
    SET p.stock = p.stock - :quantity
    WHERE p.id = :id
    """)
    void decrementStock(@Param("id") Integer id, @Param("quantity") Integer quantity);
}
