package com.ecommerce.inventory.controller;

import com.ecommerce.inventory.dto.request.CreateProductRequest;
import com.ecommerce.inventory.dto.request.ReserveStockRequest;
import com.ecommerce.inventory.dto.response.ProductResponse;
import com.ecommerce.inventory.exception.InsufficientStockException;
import com.ecommerce.inventory.exception.ProductNotFoundException;
import com.ecommerce.inventory.service.InventoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;


import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InventoryController.class)
public class InventoryControllerTest {
    public static final String TEST_PRODUCT = "Test Product";
    public static final String TEST_SKU_001 = "TEST-SKU-001";
    public static final String TEST_DESCRIPTION = "Test description";
    public static final String PRICE = "99.99";
    public static final int INITIAL_QUANTITY = 100;
    public static final String PRODUCT_ID = "PROD-123";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InventoryService inventoryService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldCreateProductSuccessfully() throws Exception {
        // Given
        CreateProductRequest request = CreateProductRequest.builder()
                .productName(TEST_PRODUCT)
                .sku(TEST_SKU_001)
                .description(TEST_DESCRIPTION)
                .price(new BigDecimal(PRICE))
                .initialQuantity(INITIAL_QUANTITY)
                .build();

        ProductResponse mockResponse = ProductResponse.builder()
                .productId(PRODUCT_ID)
                .productName(TEST_PRODUCT)
                .sku(TEST_SKU_001)
                .price(new BigDecimal(PRICE))
                .availableQuantity(100)
                .reservedQuantity(0)
                .totalQuantity(100)
                .inStock(true)
                .createdAt(Instant.now())
                .build();

        when(inventoryService.createProduct(any(CreateProductRequest.class)))
                .thenReturn(mockResponse);

        mockMvc.perform(post("/api/inventory/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/inventory/products/PROD-123"))
                .andExpect(jsonPath("$.productId").value(PRODUCT_ID))
                .andExpect(jsonPath("$.productName").value(TEST_PRODUCT))
                .andExpect(jsonPath("$.availableQuantity").value(100))
                .andExpect(jsonPath("$.inStock").value(true));
    }

    @Test
    void shouldReturnBadRequestWhenValidationFails() throws Exception {
        CreateProductRequest invalidRequest = CreateProductRequest.builder()
                .productName("")  // Invalid - blank
                .sku("SKU")
                .price(new BigDecimal("-10"))  // Invalid - negative
                .initialQuantity(-5)  // Invalid - negative
                .build();

        mockMvc.perform(post("/api/inventory/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation failed"));
    }

    @Test
    void shouldGetProductWhenExists() throws Exception {

        ProductResponse mockResponse = ProductResponse.builder()
                .productId(PRODUCT_ID)
                .productName(TEST_PRODUCT)
                .sku("SKU-001")
                .price(new BigDecimal("50.00"))
                .availableQuantity(100)
                .build();

        when(inventoryService.getProduct(PRODUCT_ID)).thenReturn(mockResponse);

        // When/Then
        mockMvc.perform(get("/api/inventory/products/{id}", PRODUCT_ID))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(PRODUCT_ID))
                .andExpect(jsonPath("$.productName").value(TEST_PRODUCT));
    }

    @Test
    void shouldReturnNotFoundWhenProductNotFound() throws Exception {
        // Given
        String productId = "NON-EXISTENT";
        when(inventoryService.getProduct(productId))
                .thenThrow(new ProductNotFoundException(productId));


        mockMvc.perform(get("/api/inventory/products/{id}", productId))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not found"))
                .andExpect(jsonPath("$.message").value("Product not found with id: " + productId));
    }

    @Test
    void shouldReserveStock_Successfully() throws Exception {
        // Given
        ReserveStockRequest request = ReserveStockRequest.builder()
                .quantity(10)
                .orderId("ORDER-456")
                .build();

        doNothing().when(inventoryService).reserveStock(PRODUCT_ID, 10);

        // When/Then
        mockMvc.perform(post("/api/inventory/products/{id}/reserve", PRODUCT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk());

        verify(inventoryService, times(1)).reserveStock(PRODUCT_ID, 10);
    }

    @Test
    void shouldReturnBadRequestWhenInsufficientStock() throws Exception {
        // Given
        String productId = "PROD-123";
        int quantity = 100;
        ReserveStockRequest request = ReserveStockRequest.builder()
                .quantity(quantity)
                .build();

        int available = 5;
        doThrow(new InsufficientStockException(productId, quantity, available))
                .when(inventoryService).reserveStock(productId, quantity);

        // When/Then
        mockMvc.perform(post("/api/inventory/products/{id}/reserve", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(
                        String.format("Insufficient stock for product %s: requested %d, available %d",
                                productId, quantity, available)));
    }

    @Test
    void shouldGetAllProducts() throws Exception {
        // Given
        List<ProductResponse> products = List.of(
                ProductResponse.builder()
                        .productId("PROD-1")
                        .productName("Product 1")
                        .sku("SKU-1")
                        .price(new BigDecimal("10.00"))
                        .availableQuantity(50)
                        .build(),
                ProductResponse.builder()
                        .productId("PROD-2")
                        .productName("Product 2")
                        .sku("SKU-2")
                        .price(new BigDecimal("20.00"))
                        .availableQuantity(30)
                        .build()
        );

        when(inventoryService.getAllProducts()).thenReturn(products);

        // When/Then
        mockMvc.perform(get("/api/inventory/products"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].productId").value("PROD-1"))
                .andExpect(jsonPath("$[1].productId").value("PROD-2"));
    }

    @Test
    void shouldCheckStockAvailability() throws Exception {

        String productId = "PROD-123";
        when(inventoryService.isStockAvailable(productId, 50)).thenReturn(true);
        when(inventoryService.isStockAvailable(productId, 200)).thenReturn(false);
        //available
        mockMvc.perform(get("/api/inventory/products/{id}/check", productId)
                        .param("quantity", "50"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
        //Not available
        mockMvc.perform(get("/api/inventory/products/{id}/check", productId)
                        .param("quantity", "200"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }
}
