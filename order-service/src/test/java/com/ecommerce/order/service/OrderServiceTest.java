package com.ecommerce.order.service;

import com.ecommerce.order.domain.Order;
import com.ecommerce.order.domain.OrderStatus;
import com.ecommerce.order.dto.request.AddressRequest;
import com.ecommerce.order.dto.request.OrderItemRequest;
import com.ecommerce.order.dto.request.OrderRequest;
import com.ecommerce.order.dto.response.OrderResponse;
import com.ecommerce.order.exception.OrderNotFoundException;
import com.ecommerce.order.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static com.ecommerce.order.utils.OrderUtils.createOrderItem;
import static com.ecommerce.order.utils.OrderUtils.createTestOrder;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {
    @MockBean
    OrderRepository orderRepository;

    @Autowired
    private OrderService orderService;

    @Test
    void shouldCreateOrderSuccessfully(){
        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> {
                    Order order = invocation.getArgument(0);
                    order.setId("ORDER-123");
                    return order;
                });

       OrderResponse response =  orderService.createOrder(createTestOrderRequest("customer-123",
               List.of(createTestOrderItemRequest("prod-1",
                       "product1", 1,
                       "100.00")) ));

       assertThat(response.getId()).isNotNull();
       assertThat(response.getItemCount()).isEqualTo(1);
       assertThat(response.getStatus()).isEqualTo(OrderStatus.PENDING);
       assertThat(response.getTotalAmount()).isEqualByComparingTo(new BigDecimal("100.00"));
       verify(orderRepository).save(any());
    }

    @Test
    void shouldCreateOrderWithMultipleItems(){
        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> {
                    Order order = invocation.getArgument(0);
                    order.setId("ORDER-123");
                    return order;
                });

        OrderResponse response =  orderService.createOrder(createTestOrderRequest(
                "customer-123",
                List.of(createTestOrderItemRequest("prod-1",
                        "product1", 1,
                        "100.00"),
                        createTestOrderItemRequest("prod-2",
                                "product2", 2,
                                "100.00")) ));

        assertThat(response.getId()).isNotNull();
        assertThat(response.getItemCount()).isEqualTo(2);
        assertThat(response.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(response.getTotalAmount()).isEqualByComparingTo(new BigDecimal("300.00"));
        verify(orderRepository).save(any());
    }

    @Test
    void shouldReturnOrdersForGivenCustomer(){
        Order order1 = createTestOrder("customer123",
                List.of(createOrderItem("prod-1", 1, "100.00"),
                        createOrderItem("prod-2", 1, "100.00")));
        Order order2 = createTestOrder("customer456",
                List.of(createOrderItem("prod-3", 1, "20.00"),
                        createOrderItem("prod-4", 1, "20.00")));

        when(orderRepository.findByCustomerId(eq("customer456"))
        ).thenReturn(List.of(order1));

        List<OrderResponse> responses = orderService.getOrdersByCustomer("customer456");

        assertThat(responses.size()).isEqualTo(1);
    }

    @Test
    void shouldReturnOrdersForGivenId(){
        Order order1 = createTestOrder("customer123",
                List.of(createOrderItem("prod-1", 1, "100.00"),
                        createOrderItem("prod-2", 1, "100.00")));

        when(orderRepository.findById(eq("order123"))
        ).thenReturn(Optional.of(order1));

        OrderResponse response = orderService.getOrder("order123");

        assertThat(response.getCustomerId()).isEqualTo("customer123");
        assertThat(response.getItemCount()).isEqualTo(2);
    }

    @Test
    void shouldThrowExceptionWhenOrderNotFound(){

        when(orderRepository.findById(eq("order123"))
        ).thenThrow(new OrderNotFoundException("order123"));


        assertThatThrownBy(() ->  orderService.getOrder("order123"))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessage("Order not found with Id order123");
    }

    @Test
    void shouldFindAllOrders(){
        Order order1 = createTestOrder("customer123",
                List.of(createOrderItem("prod-1", 1, "100.00"),
                        createOrderItem("prod-2", 1, "100.00")));
        Order order2 = createTestOrder("customer456",
                List.of(createOrderItem("prod-3", 1, "20.00"),
                        createOrderItem("prod-4", 1, "20.00")));

        when(orderRepository.findAll()).thenReturn(List.of(order1, order2));

        List<OrderResponse> response = orderService.getAllOrders();

        assertThat(response.size()).isEqualTo(2);


    }


    private static OrderRequest createTestOrderRequest( String customerId,List<OrderItemRequest> orderItemRequests) {
        return OrderRequest.builder()
                .customerId(customerId)
                .shippingAddress(createTestAddress())
                .items(orderItemRequests)
                .build();
    }

    private static OrderItemRequest createTestOrderItemRequest(String productId,
                                                               String productName, int quantity, String unitPrice) {
        return OrderItemRequest.builder()
                .productId(productId)
                .productSku(productId + "-sku")
                .productName(productName)
                .quantity(quantity)
                .unitPrice(new BigDecimal(unitPrice))
                .build();
    }

    private static AddressRequest createTestAddress() {
        return AddressRequest.builder()
                .street("5 Main St")
                .city("London")
                .state("London")
                .zipCode("E1 7DS")
                .country("GB")
                .build();
    }
}
