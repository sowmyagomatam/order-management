package com.ecommerce.inventory.domain;

import com.ecommerce.inventory.exception.InsufficientStockException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

public class ProductTest {
    @Test
    void shouldReserveStock_Successfully() {
        Product product = Product.builder()
                .productId("PROD-1")
                .availableQuantity(100)
                .reservedQuantity(0)
                .build();

        product.reserveStock(10);

        assertThat(product.getAvailableQuantity()).isEqualTo(90);
        assertThat(product.getReservedQuantity()).isEqualTo(10);
    }

    @Test
    void shouldThrowExceptionWhenInsufficientStock() {
        Product product = Product.builder()
                .availableQuantity(5)
                .build();

        assertThatThrownBy(() -> product.reserveStock(10))
                .isInstanceOf(InsufficientStockException.class);
    }

    @Test
    void shouldReleaseStock_Successfully() {
        Product product = Product.builder()
                .productId("PROD-1")
                .availableQuantity(90)
                .reservedQuantity(10)
                .build();

        product.releaseStock(10);

        assertThat(product.getAvailableQuantity()).isEqualTo(100);
        assertThat(product.getReservedQuantity()).isEqualTo(0);
    }

    @Test
    void shouldThrowExceptionWhenQuantityReleasedExceedsReserved() {
        Product product = Product.builder()
                .availableQuantity(5)
                .reservedQuantity(5)
                .build();

        assertThatThrownBy(() -> product.releaseStock(10))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldConfirmReservationSuccessfully() {
        Product product = Product.builder()
                .productId("PROD-1")
                .availableQuantity(100)
                .reservedQuantity(10)
                .build();

        product.confirmReservation(10);

        assertThat(product.getAvailableQuantity()).isEqualTo(100);
        assertThat(product.getReservedQuantity()).isEqualTo(0);
    }

    @Test
    void shouldThrowExceptionWhenConfirmedQuantityReleasedExceedsReserved() {
        Product product = Product.builder()
                .availableQuantity(5)
                .reservedQuantity(5)
                .build();

        assertThatThrownBy(() -> product.confirmReservation(10))
                .isInstanceOf(IllegalStateException.class);
    }
}
