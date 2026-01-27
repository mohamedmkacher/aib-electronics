package com.aib.aib_backend.service;

import com.aib.aib_backend.model.Address;
import org.springframework.stereotype.Service;
import java.util.List;

@Service

public interface AddressService {




     List<Address> getUserAddresses(Long userId);

     Address createAddress(Long userId, Address address);

     Address updateAddress(Long userId, Long addressId, Address updatedAddress);

     void deleteAddress(Long userId, Long addressId);

     Address getAddressById(Long addressId);
}