package com.aib.aib_backend.controller;

import com.aib.aib_backend.dto.request.ChangePasswordRequest;
import com.aib.aib_backend.dto.request.UpdateProfileRequest;
import com.aib.aib_backend.model.User;
import com.aib.aib_backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser() {
        return userService.getCurrentUser()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/profile")
    public ResponseEntity<User> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        User currentUser = userService.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("User not authenticated"));
        
        User updatedUser = userService.updateProfile(currentUser.getId(), request);
        return ResponseEntity.ok(updatedUser);
    }

    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        User currentUser = userService.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("User not authenticated"));
        
        userService.changePassword(currentUser.getId(), request);
        return ResponseEntity.ok().build();
    }

    // Admin endpoints
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/admin/customers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getCustomersOnly() {
        return ResponseEntity.ok(userService.getCustomersOnly());
    }

    @GetMapping("/admin/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> getUserById(@PathVariable Long userId) {
        return userService.getUserById(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/admin/{userId}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> updateUserRoles(@PathVariable Long userId, @RequestBody List<String> roles) {
        return ResponseEntity.ok(userService.updateUserRoles(userId, roles));
    }

    @PatchMapping("/admin/{userId}/toggle-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> toggleUserStatus(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.toggleUserStatus(userId));
    }

    @DeleteMapping("/admin/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}
