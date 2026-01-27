package com.aib.aib_backend.service;

import com.aib.aib_backend.dto.request.ChangePasswordRequest;
import com.aib.aib_backend.dto.request.UpdateProfileRequest;
import com.aib.aib_backend.model.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    Optional<User> getCurrentUser();
    User updateProfile(Long userId, UpdateProfileRequest request);
    void changePassword(Long userId, ChangePasswordRequest request);

    // Admin methods
    List<User> getAllUsers();
    List<User> getCustomersOnly(); // Added method
    Optional<User> getUserById(Long userId);
    User updateUserRoles(Long userId, List<String> newRoles);
    User toggleUserStatus(Long userId);
    void deleteUser(Long userId);
}
