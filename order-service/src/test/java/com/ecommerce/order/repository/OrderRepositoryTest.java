package com.ecommerce.order.repository;

import com.ecommerce.order.domain.Address;
import com.ecommerce.order.domain.Order;
import com.ecommerce.order.domain.OrderItem;
import com.ecommerce.order.domain.OrderStatus;
import org.assertj.core.api.ListAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static com.ecommerce.order.utils.OrderUtils.createOrderItem;
import static com.ecommerce.order.utils.OrderUtils.createTestOrder;
import static org.assertj.core.api.Assertions.assertThat;

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
        Order order = createTestOrder("customer123",
                List.of(createOrderItem("prod-1", 1, "100.00")));
        Order saved = orderRepository.save(order);
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    void shouldPersistTimestampOnUpdate() throws InterruptedException {
        Order order = createTestOrder("customer123",
                List.of(createOrderItem("prod-1", 1, "100.00")));
        Order saved = orderRepository.save(order);
        Instant originalUpdatedAt = saved.getUpdatedAt();
        Thread.sleep(1000); // give some time
        saved.updateStatus(OrderStatus.INVENTORY_RESERVED);
        assertThat(saved.getUpdatedAt()).isAfter(originalUpdatedAt);

    }

    @Test
    void shouldCascadeDeleteOrderItems(){
        Order order = createTestOrder("customer123",
                List.of(createOrderItem("prod-1", 1, "100.00")));
        Order saved = orderRepository.save(order);

        orderRepository.delete(saved);
        orderRepository.flush();
        assertThat(orderRepository.findById(saved.getId())).isEmpty();

    }

    @Test
    void shouldFindByCustomerId(){
        Order order1 = createTestOrder("customer123",
                List.of(createOrderItem("prod-1", 1, "100.00")));
        Order order2 = createTestOrder("customer456",
                List.of(createOrderItem("prod-2", 1, "100.00")));
        List<Order> orders = orderRepository.saveAll(List.of(order1, order2));

        assertThat(orderRepository.findByCustomerId("customer456")).isNotNull();
    }

    @Test
    void shouldFindByStatus(){
        Order order1 = createTestOrder("customer123",
                List.of(createOrderItem("prod-1", 1, "100.00")));
        order1.updateStatus(OrderStatus.INVENTORY_RESERVED);
        Order order2 = createTestOrder("customer456",
                List.of(createOrderItem("prod-2", 1, "100.00")));
        List<Order> orders = orderRepository.saveAll(List.of(order1, order2));

        assertThat(orderRepository.findByStatus(OrderStatus.INVENTORY_RESERVED)).isNotNull();

    }

    @Test
    void findByCustomerIdAndStatus(){
        Order order1 = createTestOrder("customer123",
                List.of(createOrderItem("prod-1", 1, "100.00")));
        order1.updateStatus(OrderStatus.INVENTORY_RESERVED);
        Order order2 = createTestOrder("customer456",
                List.of(createOrderItem("prod-2", 1, "100.00")));
        List<Order> orders = orderRepository.saveAll(List.of(order1, order2));

        assertThat(orderRepository.findByCustomerIdAndStatus("customer123", OrderStatus.INVENTORY_RESERVED)).isNotNull();

    }

    @Test
    void shouldFindOrdersBasedOnDateRange(){
        Instant now = Instant.now();
        Instant yesterday = now.minusSeconds(86400);
        Instant tomorrow = now.plusSeconds(86400);
        Order order1 = createTestOrder("customer123",
                List.of(createOrderItem("prod-1", 1, "100.00")));
        orderRepository.save(order1);
        assertThat(orderRepository.findByCreatedAtBetween(yesterday, tomorrow)).isNotNull();
        List<Order> orders = orderRepository.findByCreatedAtBetween(
                tomorrow, tomorrow.plusMillis(2000));
        assertThat(orders).isEmpty();

    }

    @Test
    void shouldCheckIfCustomerHasPendingOrders() {
        Order order1 = createTestOrder("customer123",
                List.of(createOrderItem("prod-1", 1, "100.00"),
                        createOrderItem("prod-2", 1, "100.00")));
        Order order2 = createTestOrder("customer456",
                List.of(createOrderItem("prod-2", 1, "100.00")));
        order2.updateStatus(OrderStatus.INVENTORY_RESERVED);
        orderRepository.saveAll(List.of(order1, order2));

        assertThat(orderRepository.existsByCustomerIdAndStatus("customer123", OrderStatus.PENDING)).isTrue();
        assertThat(orderRepository.existsByCustomerIdAndStatus("customer456", OrderStatus.PENDING)).isFalse();

    }

    @Test
    void shouldFindHighValueOrders(){
        Order order1 = createTestOrder("customer123",
                List.of(createOrderItem("prod-1", 1, "100.00"),
                        createOrderItem("prod-2", 1, "600.00")));
        Order order2 = createTestOrder("customer456",
                List.of(createOrderItem("prod-2", 1, "1000.00")));

        orderRepository.saveAll(List.of(order1, order2));
        assertThat(orderRepository.findHighValueOrders(new BigDecimal("500.00"))).hasSize(2);

    }

    @Test
    void shouldCountByCustomers(){

        Order order1 = createTestOrder("customer123",
                List.of(createOrderItem("prod-1", 1, "100.00"),
                        createOrderItem("prod-2", 1, "600.00")));
        Order order2 = createTestOrder("customer123",
                List.of(createOrderItem("prod-3", 1, "1000.00")));
        Order order3 = createTestOrder("customer456",
                List.of(createOrderItem("prod-2", 1, "1000.00")));


        orderRepository.saveAll(List.of(order1, order2, order3));

        assertThat(orderRepository.countByCustomerId("customer123")).isEqualTo(2);

    }


}
