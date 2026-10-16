package com.api.order.repository;

import com.api.order.entity.Order;
import com.api.order.enums.StatusOrder;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface OrderRepository extends JpaRepository<Order, Integer> {
    Optional<Order> findById(Integer id);
    List<Order> findByStatus(StatusOrder status);

    Page<Order> findByStatus(StatusOrder status, Pageable pageable);

    @Query("select o.id from Order o where o.status = :status")
    List<Integer> findIdsByStatus(@Param("status") StatusOrder status);

    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            update Order o
            set o.status = :processing
            where o.id = :id
              and o.status = :pending
            """)
    int markAsProcessing(@Param("id") Integer id,
                         @Param("processing") StatusOrder processing,
                         @Param("pending") StatusOrder pending);

    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            update Order o
            set o.status = :canceled
            where o.id = :id
              and o.status in :cancelable
            """)
    int markAsCanceled(@Param("id") Integer id,
                       @Param("canceled") StatusOrder canceled,
                       @Param("cancelable") Set<StatusOrder> cancelable);

    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            update Order o
            set o.status = :shipped
            where o.id = :id
              and o.status = :paymentApproved
            """)
    int markAsShipped(@Param("id") Integer id,
                      @Param("shipped") StatusOrder shipped,
                      @Param("paymentApproved") StatusOrder paymentApproved);

    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            update Order o
            set o.status = :delivered
            where o.id = :id
              and o.status = :shipped
            """)
    int markAsDelivered(@Param("id") Integer id,
                        @Param("delivered") StatusOrder delivered,
                        @Param("shipped") StatusOrder shipped);
}
