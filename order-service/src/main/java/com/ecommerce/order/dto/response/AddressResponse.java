package com.ecommerce.order.dto.response;

import jakarta.validation.constraints.NotBlank;

public class AddressResponse {
    private String street;
    private String city;
    private String state;
    private String zipCode;
    private String country;
}
