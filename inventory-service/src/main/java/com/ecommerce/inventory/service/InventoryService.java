package com.ecommerce.inventory.service;

import com.ecommerce.inventory.domain.Product;
import com.ecommerce.inventory.dto.request.CreateProductRequest;
import com.ecommerce.inventory.dto.response.ProductResponse;
import com.ecommerce.inventory.exception.ProductNotFoundException;
import com.ecommerce.inventory.mapper.ProductMapper;
import com.ecommerce.inventory.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InventoryService {

    private final ProductRepository productRepository;
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

    @Transactional
    public void reserveStock(String productId, Integer quantity) {
        log.info("Reserving {} units of product {}", quantity, productId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
        product.reserveStock(quantity);
        productRepository.save(product);
        log.info("Reserved {} units of product {}", quantity, productId);
    }

    @Transactional
    public void releaseStock(String productId, Integer quantity) {
        log.info("Releasing {} units of product {}", quantity, productId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
        product.releaseStock(quantity);
        productRepository.save(product);
        log.info("Released {} units of product {}", quantity, productId);
    }

    @Transactional
    public void confirmReservation(String productId, Integer quantity) {
        log.info("Confirming reservation of {} units for product {}", quantity, productId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
        product.confirmReservation(quantity);
        productRepository.save(product);
        log.info("Confirmed reservation of {} units for product {}", quantity, productId);
    }

    public boolean isStockAvailable(String productId, Integer quantity) {
        log.debug("Checking stock availability for product {}: {} units", productId, quantity);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        return product.isStockAvailable(quantity);
    }

}
