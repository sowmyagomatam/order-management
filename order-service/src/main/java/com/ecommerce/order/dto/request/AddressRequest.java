package com.ecommerce.order.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AddressRequest {
    @NotBlank(message =  "Street is required")
    private String street;
    @NotBlank(message =  "City is required")
    private String city;
    @NotBlank(message =  "State is required")
    private String state;
    @NotBlank(message =  "Zip code is required")
    private String zipCode;
    @NotBlank(message =  "Country is required")
    private String country;

}
