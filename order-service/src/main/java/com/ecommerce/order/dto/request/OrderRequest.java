package com.ecommerce.order.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderRequest {
    @NotBlank(message = "Customer Id is required")
    private String customerId;

    @NotEmpty(message = "Order must have atleast one item ")
    @Valid
    private List<OrderItemRequest> items;

    @Valid
    @NotNull(message = "Shipping address is required")
    private AddressRequest shippingAddress;

    @Valid
    private AddressRequest billingAddress;

}
