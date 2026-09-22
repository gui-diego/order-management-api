package com.api.order.repository;

import com.api.order.entity.Order;
import com.api.order.enums.StatusOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Integer> {
    Optional<Order> findById(Integer id);
    List<Order> findByStatus(StatusOrder status);
}
