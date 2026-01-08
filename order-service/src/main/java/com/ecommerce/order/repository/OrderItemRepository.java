package com.ecommerce.order.repository;

import com.ecommerce.order.domain.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByProductId(String productId);

    @Query("SELECT SUM(oi.quantity) FROM OrderItem WHERE oi.productId = :productId")
    Long getTotalQuantitySold(String productId);
}
