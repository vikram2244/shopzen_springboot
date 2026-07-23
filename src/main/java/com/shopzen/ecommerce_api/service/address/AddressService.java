// com.shopzen.ecommerce_api.service.address.AddressService.java
package com.shopzen.ecommerce_api.service.address;

import com.shopzen.ecommerce_api.dto.address.AddressDTO;
import com.shopzen.ecommerce_api.dto.address.AddressRequestDTO;

import java.util.List;
import java.util.UUID;

public interface AddressService {
    List<AddressDTO> getAddressesByUserId(String userId);
    AddressDTO getAddressByIdAndUserId(String id, String userId);
    AddressDTO createAddress(String userId, AddressRequestDTO request);
    AddressDTO updateAddress(String id, String userId, AddressRequestDTO request);
    void deleteAddress(String id, String userId);
    AddressDTO setDefaultAddress(String id, String userId);
}