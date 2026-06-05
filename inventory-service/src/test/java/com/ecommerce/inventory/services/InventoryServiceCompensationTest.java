package com.ecommerce.inventory.services;

import com.ecommerce.events.order.OrderItemEvent;
import com.ecommerce.inventory.domain.Product;
import com.ecommerce.inventory.domain.Reservation;
import com.ecommerce.inventory.domain.ReservationStatus;
import com.ecommerce.inventory.dto.ReservationResult;
import com.ecommerce.inventory.mapper.ProductMapper;
import com.ecommerce.inventory.repository.ProductRepository;
import com.ecommerce.inventory.repository.ReservationRepository;
import com.ecommerce.inventory.service.InventoryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the order-level reservation ledger operations:
 * {@link InventoryService#reserveOrder}, {@link InventoryService#releaseReservation}
 * and {@link InventoryService#confirmOrderReservations}.
 */
@ExtendWith(MockitoExtension.class)
class InventoryServiceCompensationTest {

    private static final String ORDER_ID = "ORDER-1";
    private static final String PRODUCT_1 = "PROD-1";
    private static final String PRODUCT_2 = "PROD-2";

    @Mock
    ProductRepository productRepository;
    @Mock
    ReservationRepository reservationRepository;
    @Mock
    ProductMapper productMapper;

    @InjectMocks
    InventoryService inventoryService;

    private static Product product(String id, int available, int reserved) {
        return Product.builder()
                .productId(id)
                .productName("Product " + id)
                .sku(id + "-SKU")
                .price(new BigDecimal("10.00"))
                .availableQuantity(available)
                .reservedQuantity(reserved)
                .build();
    }

    private static OrderItemEvent item(String productId, int quantity) {
        return OrderItemEvent.builder()
                .productId(productId)
                .productSku(productId + "-SKU")
                .productName("Product " + productId)
                .quantity(quantity)
                .unitPrice(new BigDecimal("10.00"))
                .build();
    }

    private static Reservation reservation(String productId, int quantity) {
        return Reservation.builder()
                .id("RES-" + productId)
                .orderId(ORDER_ID)
                .productId(productId)
                .productSku(productId + "-SKU")
                .quantity(quantity)
                .status(ReservationStatus.RESERVED)
                .build();
    }

    // ---------- reserveOrder ----------

    @Test
    void allItemsAvailableShouldReservesStockAndPersistsReservedRows() {
        Product p1 = product(PRODUCT_1, 100, 0);
        Product p2 = product(PRODUCT_2, 50, 0);
        when(reservationRepository.existsByOrderId(ORDER_ID)).thenReturn(false);
        when(productRepository.findById(PRODUCT_1)).thenReturn(Optional.of(p1));
        when(productRepository.findById(PRODUCT_2)).thenReturn(Optional.of(p2));

        ReservationResult result = inventoryService.reserveOrder(
                ORDER_ID, List.of(item(PRODUCT_1, 10), item(PRODUCT_2, 5)));

        assertThat(result.failedItems()).isEmpty();
        assertThat(result.reservedItems()).hasSize(2);

        // stock moved from available to reserved
        assertThat(p1.getAvailableQuantity()).isEqualTo(90);
        assertThat(p1.getReservedQuantity()).isEqualTo(10);
        assertThat(p2.getAvailableQuantity()).isEqualTo(45);
        assertThat(p2.getReservedQuantity()).isEqualTo(5);

        ArgumentCaptor<Reservation> captor = ArgumentCaptor.forClass(Reservation.class);
        verify(reservationRepository, times(2)).save(captor.capture());
        assertThat(captor.getAllValues())
                .allSatisfy(r -> {
                    assertThat(r.getOrderId()).isEqualTo(ORDER_ID);
                    assertThat(r.getStatus()).isEqualTo(ReservationStatus.RESERVED);
                });
        verify(productRepository, times(2)).save(any(Product.class));
    }

    @Test
    void insufficientStockOnAnyItemShouldReserveNothingAndPersistNothing() {
        Product p1 = product(PRODUCT_1, 100, 0);
        Product p2 = product(PRODUCT_2, 3, 0); // not enough for qty 5
        when(reservationRepository.existsByOrderId(ORDER_ID)).thenReturn(false);
        when(productRepository.findById(PRODUCT_1)).thenReturn(Optional.of(p1));
        when(productRepository.findById(PRODUCT_2)).thenReturn(Optional.of(p2));

        ReservationResult result = inventoryService.reserveOrder(
                ORDER_ID, List.of(item(PRODUCT_1, 10), item(PRODUCT_2, 5)));

        assertThat(result.reservedItems()).isEmpty();
        assertThat(result.failedItems()).hasSize(1);
        assertThat(result.failedItems().get(0).productId()).isEqualTo(PRODUCT_2);
        assertThat(result.failedItems().get(0).availableQuantity()).isEqualTo(3);

        // all-or-nothing: the available item was only validated, never mutated
        assertThat(p1.getAvailableQuantity()).isEqualTo(100);
        assertThat(p1.getReservedQuantity()).isEqualTo(0);
        verify(productRepository, never()).save(any(Product.class));
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    void productNotFoundShouldFailWithoutMutation() {
        when(reservationRepository.existsByOrderId(ORDER_ID)).thenReturn(false);
        when(productRepository.findById(PRODUCT_1)).thenReturn(Optional.empty());

        ReservationResult result = inventoryService.reserveOrder(ORDER_ID, List.of(item(PRODUCT_1, 10)));

        assertThat(result.reservedItems()).isEmpty();
        assertThat(result.failedItems()).hasSize(1);
        assertThat(result.failedItems().get(0).reason()).isEqualTo("Product not found");
        verify(productRepository, never()).save(any(Product.class));
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    void orderAlreadyReservedShouldResultInIdempotentNoOp() {
        when(reservationRepository.existsByOrderId(ORDER_ID)).thenReturn(true);

        ReservationResult result = inventoryService.reserveOrder(
                ORDER_ID, List.of(item(PRODUCT_1, 10), item(PRODUCT_2, 5)));

        assertThat(result.failedItems()).isEmpty();
        assertThat(result.reservedItems()).hasSize(2);
        // no re-reservation: products untouched, no new rows
        verify(productRepository, never()).findById(any());
        verify(productRepository, never()).save(any(Product.class));
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    // ---------- releaseReservation ----------

    @Test
    void reservedRowsShouldReleaseStockAndMarkReleased() {
        Reservation r1 = reservation(PRODUCT_1, 10);
        Reservation r2 = reservation(PRODUCT_2, 5);
        Product p1 = product(PRODUCT_1, 90, 10);
        Product p2 = product(PRODUCT_2, 45, 5);
        when(reservationRepository.findByOrderIdAndStatus(ORDER_ID, ReservationStatus.RESERVED))
                .thenReturn(List.of(r1, r2));
        when(productRepository.findById(PRODUCT_1)).thenReturn(Optional.of(p1));
        when(productRepository.findById(PRODUCT_2)).thenReturn(Optional.of(p2));

        inventoryService.releaseReservation(ORDER_ID);

        // stock returned to available
        assertThat(p1.getAvailableQuantity()).isEqualTo(100);
        assertThat(p1.getReservedQuantity()).isEqualTo(0);
        assertThat(p2.getAvailableQuantity()).isEqualTo(50);
        assertThat(p2.getReservedQuantity()).isEqualTo(0);

        assertThat(r1.getStatus()).isEqualTo(ReservationStatus.RELEASED);
        assertThat(r2.getStatus()).isEqualTo(ReservationStatus.RELEASED);
        verify(productRepository, times(2)).save(any(Product.class));
        verify(reservationRepository, times(2)).save(any(Reservation.class));
    }

    @Test
    void releaseStockWhennoActiveRowsShouldResultInNoOp() {
        when(reservationRepository.findByOrderIdAndStatus(ORDER_ID, ReservationStatus.RESERVED))
                .thenReturn(List.of());

        inventoryService.releaseReservation(ORDER_ID);

        verify(productRepository, never()).findById(any());
        verify(productRepository, never()).save(any(Product.class));
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    // ---------- confirmOrderReservations ----------

    @Test
    void reservedRowsShouldDrawDownReservedAndMarkConfirmed() {
        Reservation r1 = reservation(PRODUCT_1, 10);
        Product p1 = product(PRODUCT_1, 90, 10);
        when(reservationRepository.findByOrderIdAndStatus(ORDER_ID, ReservationStatus.RESERVED))
                .thenReturn(List.of(r1));
        when(productRepository.findById(PRODUCT_1)).thenReturn(Optional.of(p1));

        inventoryService.confirmOrderReservations(ORDER_ID);

        // reserved drawn down, available NOT returned (stock is sold)
        assertThat(p1.getReservedQuantity()).isEqualTo(0);
        assertThat(p1.getAvailableQuantity()).isEqualTo(90);
        assertThat(r1.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
        verify(productRepository, times(1)).save(any(Product.class));
        verify(reservationRepository, times(1)).save(any(Reservation.class));
    }

    @Test
    void reservationsWithnoActiveRowsShouldBeNoOp() {
        when(reservationRepository.findByOrderIdAndStatus(ORDER_ID, ReservationStatus.RESERVED))
                .thenReturn(List.of());

        inventoryService.confirmOrderReservations(ORDER_ID);

        verify(productRepository, never()).findById(any());
        verify(productRepository, never()).save(any(Product.class));
        verify(reservationRepository, never()).save(any(Reservation.class));
    }
}
