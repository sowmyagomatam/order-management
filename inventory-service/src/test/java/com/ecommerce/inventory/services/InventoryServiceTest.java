package com.ecommerce.inventory.services;

import com.ecommerce.inventory.domain.Product;
import com.ecommerce.inventory.dto.request.CreateProductRequest;
import com.ecommerce.inventory.dto.response.ProductResponse;
import com.ecommerce.inventory.exception.ProductNotFoundException;
import com.ecommerce.inventory.repository.ProductRepository;
import com.ecommerce.inventory.service.InventoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
public class InventoryServiceTest {
    @MockBean
    ProductRepository productRepository;

    @Autowired
    InventoryService inventoryService;

    @Test
    void shouldCreateProductSuccessfully() {
        // Given
        CreateProductRequest request = CreateProductRequest.builder()
                .productName("Test Product")
                .sku("TEST-SKU-001")
                .description("Test description")
                .price(new BigDecimal("99.99"))
                .initialQuantity(100)
                .build();

        when(productRepository.existsBySku("TEST-SKU-001")).thenReturn(false);
        when(productRepository.save(any(Product.class)))
                .thenAnswer(invocation -> {
                    Product product = invocation.getArgument(0);
                    product.setProductId("PROD-123");
                    return product;
                });

        // When
        ProductResponse response = inventoryService.createProduct(request);

        // Then
        assertThat(response.getProductId()).isEqualTo("PROD-123");
        assertThat(response.getProductName()).isEqualTo("Test Product");
        assertThat(response.getSku()).isEqualTo("TEST-SKU-001");
        assertThat(response.getAvailableQuantity()).isEqualTo(100);
        assertThat(response.getReservedQuantity()).isEqualTo(0);
        assertThat(response.isInStock()).isTrue();

        verify(productRepository).save(any(Product.class));
    }

    @Test
    void shouldThrowExceptionWhenSKUAlreadyExists() {
        CreateProductRequest request = CreateProductRequest.builder()
                .sku("DUPLICATE-SKU")
                .productName("Test")
                .price(new BigDecimal("10.00"))
                .initialQuantity(10)
                .build();

        when(productRepository.existsBySku("DUPLICATE-SKU")).thenReturn(true);

        // When/Then
        assertThatThrownBy(() -> inventoryService.createProduct(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void shouldThrowExceptionWhenProductNotFound() {
        // Given
        String productId = "NON-EXISTENT";
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> inventoryService.getProduct(productId))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("NON-EXISTENT");
    }

    @Test
    void shouldCheckStockAvailability() {
        // Given
        String productId = "PROD-123";
        Product product = Product.builder()
                .productId(productId)
                .availableQuantity(50)
                .build();

        when(productRepository.findById(productId))
                .thenReturn(Optional.of(product));

        // When/Then
        assertThat(inventoryService.isStockAvailable(productId, 30)).isTrue();
        assertThat(inventoryService.isStockAvailable(productId, 60)).isFalse();
    }
}

