package com.ecommerce.order.repository;

import com.ecommerce.order.domain.Order;
import com.ecommerce.order.domain.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {

    List<Order> findByCustomerId(String customerId);

    List<Order> findByStatus(OrderStatus status);

    List<Order> findByCustomerIdAndStatus(String customerId, OrderStatus status);

    List<Order> findByCreatedAtBetween(Instant startDate, Instant endDate);

    long countByCustomerId(String customerId);

    /**
     * Check if customer has any pending orders
     */
    boolean existsByCustomerIdAndStatus(String customerId, OrderStatus status);

    /**
     * Custom query: Find orders with total above threshold
     */
    @Query("SELECT o FROM Order o WHERE o.totalAmount >= :minAmount ORDER BY o.totalAmount DESC")
    List<Order> findHighValueOrders(@Param("minAmount") BigDecimal minAmount);
}
