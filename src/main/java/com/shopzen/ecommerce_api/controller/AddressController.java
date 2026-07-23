package com.shopzen.ecommerce_api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.shopzen.ecommerce_api.dto.address.AddressDTO;
import com.shopzen.ecommerce_api.dto.address.AddressRequestDTO;
import com.shopzen.ecommerce_api.dto.common.ApiResponseDTO;
import com.shopzen.ecommerce_api.service.address.AddressService;
import com.shopzen.ecommerce_api.util.SecurityUtils;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
@Tag(name = "Address API", description = "Address management endpoints")
public class AddressController {

    private final AddressService addressService;

    @GetMapping
    @Operation(summary = "Get current user's addresses")
    public ResponseEntity<ApiResponseDTO<List<AddressDTO>>> getAddresses() {
        String userId = SecurityUtils.getCurrentUserId();  // Changed from UUID to String
        List<AddressDTO> addresses = addressService.getAddressesByUserId(userId);
        return ResponseEntity.ok(ApiResponseDTO.success(addresses));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get address by ID")
    public ResponseEntity<ApiResponseDTO<AddressDTO>> getAddress(@PathVariable String id) {  // Changed from UUID to String
        String userId = SecurityUtils.getCurrentUserId();  // Changed from UUID to String
        AddressDTO address = addressService.getAddressByIdAndUserId(id, userId);
        return ResponseEntity.ok(ApiResponseDTO.success(address));
    }

    @PostMapping
    @Operation(summary = "Create new address")
    public ResponseEntity<ApiResponseDTO<AddressDTO>> createAddress(@Valid @RequestBody AddressRequestDTO request) {
        String userId = SecurityUtils.getCurrentUserId();  // Changed from UUID to String
        AddressDTO address = addressService.createAddress(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success(address, "Address created successfully"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update address")
    public ResponseEntity<ApiResponseDTO<AddressDTO>> updateAddress(
            @PathVariable String id,  // Changed from UUID to String
            @Valid @RequestBody AddressRequestDTO request) {
        String userId = SecurityUtils.getCurrentUserId();  // Changed from UUID to String
        AddressDTO address = addressService.updateAddress(id, userId, request);
        return ResponseEntity.ok(ApiResponseDTO.success(address, "Address updated successfully"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete address")
    public ResponseEntity<ApiResponseDTO<Void>> deleteAddress(@PathVariable String id) {  // Changed from UUID to String
        String userId = SecurityUtils.getCurrentUserId();  // Changed from UUID to String
        addressService.deleteAddress(id, userId);
        return ResponseEntity.ok(ApiResponseDTO.success(null, "Address deleted successfully"));
    }

    @PatchMapping("/{id}/default")
    @Operation(summary = "Set address as default")
    public ResponseEntity<ApiResponseDTO<AddressDTO>> setDefaultAddress(@PathVariable String id) {  // Changed from UUID to String
        String userId = SecurityUtils.getCurrentUserId();  // Changed from UUID to String
        AddressDTO address = addressService.setDefaultAddress(id, userId);
        return ResponseEntity.ok(ApiResponseDTO.success(address, "Default address updated"));
    }
}