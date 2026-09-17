package com.api.category.repository;

import com.api.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CategoryRepository extends JpaRepository<Category, Integer> {

    @Query("""
    SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END
    FROM Category c
    WHERE LOWER(TRIM(c.name)) = LOWER(TRIM(:name))
      AND (:id IS NULL OR c.id <> :id)
    """)
    boolean existsByNameIgnoringId(@Param("name") String name, @Param("id") Integer id);
}
