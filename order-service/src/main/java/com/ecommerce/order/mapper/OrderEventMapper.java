package com.ecommerce.order.mapper;

import com.ecommerce.common.event.OrderCreatedEvent;
import com.ecommerce.order.domain.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring",
uses = OrderItemEventMapper.class)
public interface OrderEventMapper {

    @Mapping(target = "orderId", source = "id")
    @Mapping(target = "timestamp", expression = "java(java.time.Instant.now())")
    public OrderCreatedEvent toOrderCreatedEvent(Order order);
}
