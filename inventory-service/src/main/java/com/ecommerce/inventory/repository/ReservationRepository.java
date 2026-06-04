package com.ecommerce.inventory.repository;

import com.ecommerce.inventory.domain.Reservation;
import com.ecommerce.inventory.domain.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, String> {

    List<Reservation> findByOrderId(String orderId);

    List<Reservation> findByOrderIdAndStatus(String orderId, ReservationStatus status);

    boolean existsByOrderId(String orderId);
}
