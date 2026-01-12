package com.ecommerce.order.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.ecommerce.order.utils.OrderUtils.createOrderItem;
import static com.ecommerce.order.utils.OrderUtils.createTestOrder;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class OrderTest {

    @Test
    void shouldInitializeWithPendingStatus(){

    }

    @Test
    void shouldValidateOrderWithValidData(){

    }

    @Test
    void shouldRecalculateTotalAfterAddingItems(){
        Order order = createTestOrder("customer123", new ArrayList<>());
        order.addItem(createOrderItem("prod-1", 1, "10.00"));
        order.addItem(createOrderItem("prod-2", 2, "20.00"));
        assertThat(order.getTotalAmount()).isEqualByComparingTo(new BigDecimal("50.00"));

    }

    @Test
    void shouldNotAllowAddingItemsWhenNotPending(){

    }

    @Test
    void shouldRecalculateTotalAfterRemovingItems() {

    }

    @Test
    void shouldNotAllowRemovingItemsWhenNotPending(){

    }

    @Test
    void shouldCalculateZeroTotalWhenAllItemsRemoved(){

    }

    @Test
    void shouldAllowStatusUpdateForValidTransitions(){

    }

    @Test
    void shouldNotAllowStatusUpdateForIllegalTransitions(){

    }

    @Test
    void shouldUpdateTimestampWhenStatusUpdated(){

    }

    @Test
    void shouldAllowCancellationForValidTransitions(){

    }

    @Test
    void shouldNotAllowCancellationForInValidTransitions(){

    }

    @Test
    void shouldCheckOrderHasSpecificProduct(){

    }

    @Test
    void shouldGetCorrectItemCount(){

    }

    @Test
    void shouldGetCorrectItemQuantity(){

    }

    @Test
    void shouldCountByCustomers(){

    }
}
