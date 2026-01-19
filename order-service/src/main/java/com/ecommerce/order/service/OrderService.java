package com.ecommerce.order.service;

import com.ecommerce.order.domain.Order;
import com.ecommerce.order.domain.OrderItem;
import com.ecommerce.order.domain.OrderStatus;
import com.ecommerce.order.dto.request.OrderRequest;
import com.ecommerce.order.dto.response.OrderResponse;
import com.ecommerce.order.exception.OrderNotFoundException;
import com.ecommerce.order.mapper.OrderItemMapper;
import com.ecommerce.order.mapper.OrderMapper;
import com.ecommerce.order.repository.OrderRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;

    @Transactional
    public OrderResponse createOrder(OrderRequest orderRequest){
        log.info("Creating order for customer {}", orderRequest.getCustomerId());
        Order order = orderMapper.toEntity(orderRequest);
        // add items to order
        List<OrderItem> orderItems = orderRequest.getItems().stream()
                        .map(orderItemMapper::toEntity)
                                .toList();
        orderItems.forEach(order::addItem);
        order.validate();
        Order savedOrder = orderRepository.save(order);
        log.info("Saved order successfully");
        return orderMapper.toOrderResponse(savedOrder);
    }

    public OrderResponse getOrder(String orderId) throws OrderNotFoundException {
        log.debug("Fetching order with ID: {}", orderId);
        return orderMapper.toOrderResponse(
                orderRepository.findById(orderId)
                        .orElseThrow(() -> new OrderNotFoundException(orderId)));
    }

    public List<OrderResponse> getAllOrders(){
        log.debug("Fetching all orders");
        return orderMapper.toResponseList(orderRepository.findAll());
    }

    public List<OrderResponse> getOrdersByCustomer(String customerId){
        log.debug("Fetching orders for customer: {}", customerId);
        return orderMapper.toResponseList(orderRepository.findByCustomerId(customerId));
    }

    public OrderResponse updateOrderStatus(String orderId, OrderStatus orderStatus){
        return orderRepository.findById(orderId)
                .map(order -> {
                    order.updateStatus(orderStatus);
                return orderMapper.toOrderResponse(order);})
                .orElseThrow(() -> new OrderNotFoundException(orderId));


    }
}
