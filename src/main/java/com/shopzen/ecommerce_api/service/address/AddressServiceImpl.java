// com.shopzen.ecommerce_api.service.address.AddressServiceImpl.java
package com.shopzen.ecommerce_api.service.address;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopzen.ecommerce_api.dto.address.AddressDTO;
import com.shopzen.ecommerce_api.dto.address.AddressRequestDTO;
import com.shopzen.ecommerce_api.entity.Address;
import com.shopzen.ecommerce_api.exception.ResourceNotFoundException;
import com.shopzen.ecommerce_api.repository.AddressRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;

    @Override
    public List<AddressDTO> getAddressesByUserId(String userId) {
        return addressRepository.findByUserIdOrderByIsDefaultDesc(userId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public AddressDTO getAddressByIdAndUserId(String id, String userId) {
        Address address = addressRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));
        return toDTO(address);
    }

    @Override
    public AddressDTO createAddress(String userId, AddressRequestDTO request) {
        if (request.getIsDefault() != null && request.getIsDefault()) {
            addressRepository.updateAllNonDefault(userId);
        } else {
            List<Address> existingAddresses = addressRepository.findByUserIdOrderByIsDefaultDesc(userId);
            if (existingAddresses.isEmpty()) {
                request.setIsDefault(true);
            }
        }

        Address address = Address.builder()
                .userId(userId)
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .addressLine1(request.getAddressLine1())
                .addressLine2(request.getAddressLine2())
                .city(request.getCity())
                .state(request.getState())
                .pincode(request.getPincode())
                .country(request.getCountry() != null ? request.getCountry() : "India")
                .isDefault(request.getIsDefault() != null ? request.getIsDefault() : false)
                .addressType(request.getAddressType() != null ? request.getAddressType() : "home")
                .build();

        address = addressRepository.save(address);
        return toDTO(address);
    }

    @Override
    public AddressDTO updateAddress(String id, String userId, AddressRequestDTO request) {
        Address address = addressRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));
        if (request.getIsDefault() != null && request.getIsDefault() && !address.getIsDefault()) {
            addressRepository.updateAllNonDefault(userId);
        }

        address.setFullName(request.getFullName());
        address.setPhone(request.getPhone());
        address.setAddressLine1(request.getAddressLine1());
        address.setAddressLine2(request.getAddressLine2());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setPincode(request.getPincode());
        address.setCountry(request.getCountry() != null ? request.getCountry() : address.getCountry());
        address.setIsDefault(request.getIsDefault() != null ? request.getIsDefault() : address.getIsDefault());
        address.setAddressType(request.getAddressType() != null ? request.getAddressType() : address.getAddressType());

        address = addressRepository.save(address);
        return toDTO(address);
    }

    @Override
    public void deleteAddress(String id, String userId) {
        Address address = addressRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));
        if (address.getIsDefault()) {
            List<Address> otherAddresses = addressRepository.findByUserIdOrderByIsDefaultDesc(userId)
                    .stream()
                    .filter(a -> !a.getId().equals(id))
                    .collect(Collectors.toList());
            
            if (!otherAddresses.isEmpty()) {
                Address newDefault = otherAddresses.get(0);
                newDefault.setIsDefault(true);
                addressRepository.save(newDefault);
            }
        }
        
        addressRepository.delete(address);
    }

    @Override
    public AddressDTO setDefaultAddress(String id, String userId) {
        addressRepository.updateAllNonDefault(userId);
        Address address = addressRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));
        address.setIsDefault(true);
        address = addressRepository.save(address);
        return toDTO(address);
    }

    private AddressDTO toDTO(Address address) {
        return AddressDTO.builder()
                .id(address.getId().toString())
                .userId(address.getUserId().toString())
                .fullName(address.getFullName())
                .phone(address.getPhone())
                .addressLine1(address.getAddressLine1())
                .addressLine2(address.getAddressLine2())
                .city(address.getCity())
                .state(address.getState())
                .pincode(address.getPincode())
                .country(address.getCountry())
                .isDefault(address.getIsDefault())
                .addressType(address.getAddressType())
                .createdAt(address.getCreatedAt() != null ? address.getCreatedAt().toString() : null)
                .updatedAt(address.getUpdatedAt() != null ? address.getUpdatedAt().toString() : null)
                .build();
    }
}