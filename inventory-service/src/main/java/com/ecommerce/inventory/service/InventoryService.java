package com.ecommerce.inventory.service;

import com.ecommerce.events.inventory.FailedItemEvent;
import com.ecommerce.events.inventory.ReservedItemEvent;
import com.ecommerce.events.order.OrderItemEvent;
import com.ecommerce.inventory.domain.Product;
import com.ecommerce.inventory.domain.Reservation;
import com.ecommerce.inventory.domain.ReservationStatus;
import com.ecommerce.inventory.dto.ReservationResult;
import com.ecommerce.inventory.dto.request.CreateProductRequest;
import com.ecommerce.inventory.dto.response.ProductResponse;
import com.ecommerce.inventory.exception.ProductNotFoundException;
import com.ecommerce.inventory.mapper.ProductMapper;
import com.ecommerce.inventory.repository.ProductRepository;
import com.ecommerce.inventory.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InventoryService {

    private final ProductRepository productRepository;
    private final ReservationRepository reservationRepository;
    private final ProductMapper productMapper;

    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {
        log.info("Creating product with SKU: {}", request.getSku());

        if (productRepository.existsBySku(request.getSku())) {
            throw new IllegalArgumentException("Product with SKU " + request.getSku() + " already exists");
        }
        Product product = productMapper.toEntity(request);
        Product saved = productRepository.save(product);
        log.info("Created product with ID: {}", saved.getProductId());
        return productMapper.toResponse(saved);
    }

    public ProductResponse getProduct(String productId) {
        log.debug("Fetching product with ID: {}", productId);
        return productRepository.findById(productId)
                .map(productMapper::toResponse)
                .orElseThrow(() -> new ProductNotFoundException(productId));
    }

    public List<ProductResponse> getAllProducts() {
        log.debug("Fetching all products");
        return productMapper.toResponseList(productRepository.findAll());
    }

    public boolean isStockAvailable(String productId, Integer quantity) {
        log.debug("Checking stock availability for product {}: {} units", productId, quantity);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        return product.isStockAvailable(quantity);
    }

    /**
     * Reserve stock for an entire order, all-or-nothing.
     * Phase 1 validates every item without mutating anything; if any item is missing
     * or short on stock, nothing is reserved and no reservation rows are written.
     * Phase 2 (only when every item passes) reserves all items and persists a RESERVED
     * reservation row per item, atomically within this transaction. Reservation rows
     * therefore only ever exist for fully-fulfilled orders, which is what the cancel/
     * release path reconciles against.
     */
    @Transactional
    public ReservationResult reserveOrder(String orderId, List<OrderItemEvent> items) {
        log.info("Reserving stock for order: {} ({} items)", orderId, items.size());

        // Idempotency: a redelivered orders.created for an already-reserved order is a no-op.
        if (reservationRepository.existsByOrderId(orderId)) {
            log.info("Order {} already has reservations; skipping re-reservation", orderId);
            List<ReservedItemEvent> alreadyReserved = items.stream()
                    .map(item -> new ReservedItemEvent(
                            item.productId(), item.productSku(), item.unitPrice(), item.quantity()))
                    .toList();
            return new ReservationResult(alreadyReserved, List.of());
        }

        // Phase 1: validate all items, no mutation.
        List<FailedItemEvent> failedItems = new ArrayList<>();
        for (OrderItemEvent item : items) {
            Optional<Product> productOpt = productRepository.findById(item.productId());
            if (productOpt.isEmpty()) {
                failedItems.add(new FailedItemEvent(
                        item.productId(), item.productSku(), item.quantity(), 0, "Product not found"));
                continue;
            }
            Product product = productOpt.get();
            if (!product.isStockAvailable(item.quantity())) {
                failedItems.add(new FailedItemEvent(
                        item.productId(), item.productSku(), item.quantity(), product.getAvailableQuantity(),
                        String.format("Insufficient stock. Available: %d, Requested: %d",
                                product.getAvailableQuantity(), item.quantity())));
            }
        }

        // All-or-nothing: any failure means we mutate nothing and persist nothing.
        if (!failedItems.isEmpty()) {
            log.warn("Reservation for order {} failed: {} of {} items unavailable",
                    orderId, failedItems.size(), items.size());
            return new ReservationResult(List.of(), failedItems);
        }

        // Phase 2: reserve all items and record reservation rows.
        List<ReservedItemEvent> reservedItems = new ArrayList<>();
        for (OrderItemEvent item : items) {
            // Within this transaction findById returns the managed instance, so repeated
            // products in the same order accumulate correctly.
            Product product = productRepository.findById(item.productId())
                    .orElseThrow(() -> new ProductNotFoundException(item.productId()));
            product.reserveStock(item.quantity());
            productRepository.save(product);

            reservationRepository.save(Reservation.builder()
                    .orderId(orderId)
                    .productId(item.productId())
                    .productSku(item.productSku())
                    .quantity(item.quantity())
                    .status(ReservationStatus.RESERVED)
                    .build());

            reservedItems.add(new ReservedItemEvent(
                    item.productId(), item.productSku(), item.unitPrice(), item.quantity()));
            log.info("Reserved {} units of product {} for order {}",
                    item.quantity(), item.productId(), orderId);
        }

        log.info("Reserved all {} items for order {}", items.size(), orderId);
        return new ReservationResult(reservedItems, List.of());
    }

    /**
     * Release the stock held for a cancelled order by reconciling against the
     * reservation rows. Idempotent: only RESERVED rows are released and then flipped
     * to RELEASED, so a redelivered orders.cancelled event releases nothing twice.
     * An order that never reserved (e.g. cancelled because it was out of stock) has
     * no RESERVED rows and is a safe no-op.
     */
    @Transactional
    public void releaseReservation(String orderId) {
        List<Reservation> activeReservations =
                reservationRepository.findByOrderIdAndStatus(orderId, ReservationStatus.RESERVED);

        if (activeReservations.isEmpty()) {
            log.info("No active reservations to release for order {} (already released or never reserved)", orderId);
            return;
        }

        for (Reservation reservation : activeReservations) {
            Product product = productRepository.findById(reservation.getProductId())
                    .orElseThrow(() -> new ProductNotFoundException(reservation.getProductId()));
            product.releaseStock(reservation.getQuantity());
            productRepository.save(product);

            reservation.setStatus(ReservationStatus.RELEASED);
            reservationRepository.save(reservation);
            log.info("Released {} units of product {} for cancelled order {}",
                    reservation.getQuantity(), reservation.getProductId(), orderId);
        }

        log.info("Released {} reservation(s) for cancelled order {}", activeReservations.size(), orderId);
    }

    /**
     * Confirm the reservations for a fulfilled order, drawing the held stock down from
     * reserved quantity (it does NOT return to available — the stock is sold). Reservation
     * rows are flipped to CONFIRMED as a terminal audit record. Idempotent: only RESERVED
     * rows are confirmed, so a redelivered orders.confirmed event confirms nothing twice.
     * An order with no RESERVED rows is a safe no-op.
     */
    @Transactional
    public void confirmOrderReservations(String orderId) {
        List<Reservation> activeReservations =
                reservationRepository.findByOrderIdAndStatus(orderId, ReservationStatus.RESERVED);

        if (activeReservations.isEmpty()) {
            log.info("No active reservations to confirm for order {} (already confirmed or never reserved)", orderId);
            return;
        }

        for (Reservation reservation : activeReservations) {
            Product product = productRepository.findById(reservation.getProductId())
                    .orElseThrow(() -> new ProductNotFoundException(reservation.getProductId()));
            product.confirmReservation(reservation.getQuantity());
            productRepository.save(product);

            reservation.setStatus(ReservationStatus.CONFIRMED);
            reservationRepository.save(reservation);
            log.info("Confirmed {} units of product {} for fulfilled order {}",
                    reservation.getQuantity(), reservation.getProductId(), orderId);
        }

        log.info("Confirmed {} reservation(s) for fulfilled order {}", activeReservations.size(), orderId);
    }

}
