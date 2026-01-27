package com.aib.aib_backend.service.impl;

import com.aib.aib_backend.exception.ResourceNotFoundException;
import com.aib.aib_backend.model.Address;
import com.aib.aib_backend.model.User;
import com.aib.aib_backend.repository.AddressRepository;
import com.aib.aib_backend.repository.UserRepository;
import com.aib.aib_backend.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    @Override
    public List<Address> getUserAddresses(Long userId) {
        return addressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId);
    }

    @Override
    public Address createAddress(Long userId, Address address) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // If user doesn't have a phone number, save the one from address
        if ((user.getPhone() == null || user.getPhone().isBlank()) &&
                address.getPhone() != null && !address.getPhone().isBlank()) {
            user.setPhone(address.getPhone());
            userRepository.save(user);
        }

        // If this is the first address or marked as default, handle default logic
        if (address.getIsDefault() != null && address.getIsDefault()) {
            // Reset other default addresses
            addressRepository.findByUserIdAndIsDefaultTrue(userId)
                    .ifPresent(existingDefault -> {
                        existingDefault.setIsDefault(false);
                        existingDefault.setUpdatedAt(LocalDateTime.now());
                        addressRepository.save(existingDefault);
                    });
        }

        // If this is the first address, make it default
        if (addressRepository.countByUserId(userId) == 0) {
            address.setIsDefault(true);
        }

        address.setUser(user);
        address.setCreatedAt(LocalDateTime.now());
        address.setUpdatedAt(LocalDateTime.now());

        return addressRepository.save(address);
    }

    @Override
    public Address updateAddress(Long userId, Long addressId, Address updatedAddress) {
        Address existingAddress = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + addressId));

        // Verify ownership
        if (!existingAddress.getUser().getId().equals(userId)) {
            throw new RuntimeException("Address does not belong to the user");
        }

        User user = existingAddress.getUser();

        // If user doesn't have a phone number, save the one from updated address
        if ((user.getPhone() == null || user.getPhone().isBlank()) &&
                updatedAddress.getPhone() != null && !updatedAddress.getPhone().isBlank()) {
            user.setPhone(updatedAddress.getPhone());
            userRepository.save(user);
        }

        // If setting as default, reset other default addresses
        if (updatedAddress.getIsDefault() != null && updatedAddress.getIsDefault() &&
                (existingAddress.getIsDefault() == null || !existingAddress.getIsDefault())) {
            addressRepository.findByUserIdAndIsDefaultTrue(userId)
                    .ifPresent(existingDefault -> {
                        if (!existingDefault.getId().equals(addressId)) {
                            existingDefault.setIsDefault(false);
                            existingDefault.setUpdatedAt(LocalDateTime.now());
                            addressRepository.save(existingDefault);
                        }
                    });
        }

        // Update fields
        existingAddress.setFullName(updatedAddress.getFullName());
        existingAddress.setPhone(updatedAddress.getPhone());
        existingAddress.setAddressLine(updatedAddress.getAddressLine());
        existingAddress.setCity(updatedAddress.getCity());
        existingAddress.setPostalCode(updatedAddress.getPostalCode());
        existingAddress.setCountry(updatedAddress.getCountry());
        existingAddress.setIsDefault(updatedAddress.getIsDefault());
        existingAddress.setLabel(updatedAddress.getLabel());
        existingAddress.setUpdatedAt(LocalDateTime.now());

        return addressRepository.save(existingAddress);
    }

    @Override
    public void deleteAddress(Long userId, Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + addressId));

        // Verify ownership
        if (!address.getUser().getId().equals(userId)) {
            throw new RuntimeException("Address does not belong to the user");
        }

        boolean wasDefault = address.getIsDefault() != null && address.getIsDefault();

        addressRepository.delete(address);

        // If deleted address was default, set another as default
        if (wasDefault) {
            List<Address> remainingAddresses = addressRepository.findByUserId(userId);
            if (!remainingAddresses.isEmpty()) {
                Address newDefault = remainingAddresses.get(0);
                newDefault.setIsDefault(true);
                newDefault.setUpdatedAt(LocalDateTime.now());
                addressRepository.save(newDefault);
            }
        }
    }

    @Override
    public Address getAddressById(Long addressId) {
        return addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + addressId));
    }
}