package com.ecommerce.order.repository;

import com.ecommerce.order.domain.Address;
import com.ecommerce.order.domain.Order;
import com.ecommerce.order.domain.OrderItem;
import com.ecommerce.order.domain.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static com.ecommerce.order.utils.OrderUtils.createOrderItem;
import static com.ecommerce.order.utils.OrderUtils.createTestOrder;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
@ActiveProfiles("default")
public class OrderRepositoryTest {
    @Autowired
    OrderRepository orderRepository;

    @BeforeEach
    void setUp(){
        orderRepository.deleteAll();
    }

    @Test
    void shouldSaveAndRetrieveOrder(){
        Order order = createTestOrder("customer123",
                List.of(createOrderItem("prod-1", 1, "100.00")));
        Order saved = orderRepository.save(order);

        assertThat(saved.getId()).isNotNull();
        assertThat(orderRepository.findById(saved.getId())).isPresent();
        assertThat(saved.getItemCount()).isEqualTo(1);
        assertThat(saved.getStatus()).isEqualTo(OrderStatus.PENDING);

    }

    @Test
    void shouldRecalculateTotalBeforeSaving(){
        Order order = createTestOrder("customer123",
                List.of(createOrderItem("prod-1", 1, "100.00")));
        order.getItems().get(0).setQuantity(3);
        Order savedOrder = orderRepository.save(order);

        assertThat(savedOrder.getId()).isNotNull();
        assertThat(savedOrder.getTotalAmount())
                .isEqualByComparingTo(new BigDecimal("300.00"));

    }

    @Test
    void shouldPersistTimestampOnSave(){

    }

    @Test
    void shouldPersistTimestampOnUpdate(){

    }

    @Test
    void shouldCascadeDeleteOrderItems(){

    }

    @Test
    void shouldFindByCustomerId(){

    }

    @Test
    void shouldFindByStatus(){

    }

    @Test
    void findByCustomerIdAndStatus(){

    }

    @Test
    void shouldFindOrdersBasedOnDateRange(){

    }

    @Test
    void shouldFindPendingOrdersByCustomer(){

    }

    @Test
    void shouldFindHighValueOrders(){

    }

    @Test
    void shouldCountByCustomers(){

    }


}
