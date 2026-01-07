package com.ecommerce.order.domain;

import com.google.common.base.Preconditions;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Address {
    private String street;
    private String city;
    private String state;
    private String zipCode;
    private String country;

    public void validate() {
        Preconditions.checkArgument(street != null && !street.isBlank(), "Street is mandatory");
        Preconditions.checkArgument(city != null && !city.isBlank(), "City is mandatory");
        Preconditions.checkArgument(zipCode != null && !zipCode.isBlank(), "ZipCode is mandatory");
        Preconditions.checkArgument(country != null && !country.isBlank(), "country is mandatory");
    }
}
