package com.ecommerce.order.mapper;

import com.ecommerce.order.domain.Address;
import com.ecommerce.order.dto.request.AddressRequest;
import com.ecommerce.order.dto.response.AddressResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AddressMapper {

    Address toEntity(AddressRequest addressRequest);

    AddressResponse toAddressResponse(Address address);
}
