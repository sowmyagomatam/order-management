package com.ecommerce.order.mapper;

import com.ecommerce.events.order.OrderItemEvent;
import com.ecommerce.order.domain.OrderItem;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrderItemEventMapper {
    OrderItemEvent toOrderItemEvent(OrderItem item);
}
