package com.aib.aib_backend.controller;

import com.aib.aib_backend.dto.request.AddressRequest;
import com.aib.aib_backend.dto.response.AddressResponse;
import com.aib.aib_backend.model.Address;
import com.aib.aib_backend.model.User;
import com.aib.aib_backend.service.AddressService;
import com.aib.aib_backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class AddressController {

    private final AddressService addressService;
    private final UserService userService;

    /**
     * Get all addresses for current user
     */
    @GetMapping
    public ResponseEntity<List<AddressResponse>> getUserAddresses() {
        User user = userService.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("User not authenticated"));

        List<AddressResponse> addresses = addressService.getUserAddresses(user.getId())
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(addresses);
    }

    /**
     * Get a specific address by ID
     */
    @GetMapping("/{addressId}")
    public ResponseEntity<AddressResponse> getAddress(@PathVariable Long addressId) {
        User user = userService.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("User not authenticated"));

        Address address = addressService.getAddressById(addressId);

        // Verify ownership
        if (!address.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(toResponse(address));
    }

    /**
     * Create a new address
     */
    @PostMapping
    public ResponseEntity<AddressResponse> createAddress(@Valid @RequestBody AddressRequest request) {
        User user = userService.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("User not authenticated"));

        Address address = Address.builder()
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .addressLine(request.getAddressLine())
                .city(request.getCity())
                .postalCode(request.getPostalCode())
                .country(request.getCountry() != null ? request.getCountry() : "Tunisia")
                .isDefault(request.getIsDefault() != null ? request.getIsDefault() : false)
                .label(request.getLabel())
                .build();

        Address savedAddress = addressService.createAddress(user.getId(), address);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(savedAddress));
    }

    /**
     * Update an existing address
     */
    @PutMapping("/{addressId}")
    public ResponseEntity<AddressResponse> updateAddress(
            @PathVariable Long addressId,
            @Valid @RequestBody AddressRequest request) {

        User user = userService.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("User not authenticated"));

        Address address = Address.builder()
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .addressLine(request.getAddressLine())
                .city(request.getCity())
                .postalCode(request.getPostalCode())
                .country(request.getCountry() != null ? request.getCountry() : "Tunisia")
                .isDefault(request.getIsDefault() != null ? request.getIsDefault() : false)
                .label(request.getLabel())
                .build();

        Address updatedAddress = addressService.updateAddress(user.getId(), addressId, address);
        return ResponseEntity.ok(toResponse(updatedAddress));
    }

    /**
     * Delete an address
     */
    @DeleteMapping("/{addressId}")
    public ResponseEntity<Void> deleteAddress(@PathVariable Long addressId) {
        User user = userService.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("User not authenticated"));

        addressService.deleteAddress(user.getId(), addressId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Set an address as default
     */
    @PatchMapping("/{addressId}/set-default")
    public ResponseEntity<AddressResponse> setDefaultAddress(@PathVariable Long addressId) {
        User user = userService.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("User not authenticated"));

        // Get the address and verify ownership
        Address address = addressService.getAddressById(addressId);
        if (!address.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Reset all other addresses to non-default
        List<Address> userAddresses = addressService.getUserAddresses(user.getId());
        for (Address addr : userAddresses) {
            if (addr.getIsDefault() && !addr.getId().equals(addressId)) {
                addr.setIsDefault(false);
                addressService.updateAddress(user.getId(), addr.getId(), addr);
            }
        }

        // Set this address as default
        address.setIsDefault(true);
        Address updatedAddress = addressService.updateAddress(user.getId(), addressId, address);

        return ResponseEntity.ok(toResponse(updatedAddress));
    }

    /**
     * Convert Address entity to AddressResponse DTO
     */
    private AddressResponse toResponse(Address address) {
        return AddressResponse.builder()
                .id(address.getId())
                .fullName(address.getFullName())
                .phone(address.getPhone())
                .addressLine(address.getAddressLine())
                .city(address.getCity())
                .postalCode(address.getPostalCode())
                .country(address.getCountry())
                .isDefault(address.getIsDefault())
                .label(address.getLabel())
                .build();
    }
}