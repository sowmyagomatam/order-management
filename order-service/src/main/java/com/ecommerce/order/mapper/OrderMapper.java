package com.ecommerce.order.mapper;

import com.ecommerce.order.domain.Order;
import com.ecommerce.order.dto.request.OrderRequest;
import com.ecommerce.order.dto.response.OrderResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring",
        uses = {OrderItemMapper.class, AddressMapper.class})
public interface OrderMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)  // Will be PENDING by default via @Builder.Default
    @Mapping(target = "items", ignore = true)  // Service will call addItem() for each
    @Mapping(target = "totalAmount", ignore = true)  // Calculated by addItem() → recalculateTotal()
    @Mapping(target = "createdAt", ignore = true)  // Set by @PrePersist
    @Mapping(target = "updatedAt", ignore = true)  // Set by @PrePersist
    @Mapping(target = "cancellationReason", ignore = true)
    @Mapping(target = "cancelledAt", ignore = true)
    @Mapping(target = "cancelledBy", ignore = true)
    Order toEntity(OrderRequest orderRequest);

    @Mapping(target = "itemCount", expression = "java(order.getItemCount())")
    @Mapping(target = "totalItemQuantity", expression = "java(order.getTotalItemQuantity())")
    OrderResponse toOrderResponse(Order order);

    List<OrderResponse> toResponseList(List<Order> orders);
}
