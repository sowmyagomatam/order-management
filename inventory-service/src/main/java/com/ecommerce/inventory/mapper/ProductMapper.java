package com.ecommerce.inventory.mapper;

import com.ecommerce.inventory.domain.Product;
import com.ecommerce.inventory.dto.request.CreateProductRequest;
import com.ecommerce.inventory.dto.response.ProductResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(target = "productId", ignore = true)  // Set by @PrePersist
    @Mapping(target = "availableQuantity", source = "initialQuantity")
    @Mapping(target = "reservedQuantity", constant = "0")
    @Mapping(target = "createdAt", ignore = true)  // Set by @PrePersist
    @Mapping(target = "updatedAt", ignore = true)  // Set by @PrePersist
    Product toEntity(CreateProductRequest productRequest);

    @Mapping(target = "totalQuantity",
            expression = "java(product.getAvailableQuantity() + product.getReservedQuantity())")
    @Mapping(target = "inStock",
            expression = "java(product.getAvailableQuantity() > 0)")
    ProductResponse toResponse(Product product);

    List<ProductResponse> toResponseList(List<Product> products);
}
