package com.ecommerce.inventory.controller;

import com.ecommerce.inventory.dto.request.CreateProductRequest;
import com.ecommerce.inventory.dto.request.ReserveStockRequest;
import com.ecommerce.inventory.dto.response.ProductResponse;
import com.ecommerce.inventory.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@RestController
@RequestMapping("/api/inventory/products")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody CreateProductRequest productRequest) throws URISyntaxException {
       ProductResponse response =  inventoryService.createProduct(productRequest);
       return ResponseEntity.created(new URI("/api/inventory/products/" + response.getProductId()))
               .body(response);
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponse> getProduct(@PathVariable String productId){
        ProductResponse productResponse = inventoryService.getProduct(productId);
        return ResponseEntity.ok(productResponse);
    }

    @PostMapping("/{productId}/reserve")
    public ResponseEntity<Void> reserveStock(
            @PathVariable String productId,
            @Valid @RequestBody ReserveStockRequest request
    ) {
        inventoryService.reserveStock(productId, request.getQuantity());
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts(){
        List<ProductResponse> productResponses = inventoryService.getAllProducts();
        return ResponseEntity.ok(productResponses);
    }

    @PostMapping("/{productId}/release")
    public ResponseEntity<Void> releaseStock(
            @PathVariable String productId,
            @Valid @RequestBody ReserveStockRequest request
    ) {
        inventoryService.releaseStock(productId, request.getQuantity());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{productId}/confirm")
    public ResponseEntity<Void> confirmReservation(
            @PathVariable String productId,
            @Valid @RequestBody ReserveStockRequest request
    ) {
        inventoryService.confirmReservation(productId, request.getQuantity());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{productId}/check")
    public ResponseEntity<Boolean> checkStockAvailability(
            @PathVariable String productId,
            @RequestParam Integer quantity
    ) {
        boolean available = inventoryService.isStockAvailable(productId, quantity);
        return ResponseEntity.ok(available);
    }
}
