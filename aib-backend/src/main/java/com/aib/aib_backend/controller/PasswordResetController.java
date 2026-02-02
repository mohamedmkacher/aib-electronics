package com.aib.aib_backend.controller;

import com.aib.aib_backend.dto.request.ForgotPasswordRequest;
import com.aib.aib_backend.dto.request.ResetPasswordRequest;
import com.aib.aib_backend.service.PasswordResetService;
import com.aib.aib_backend.service.impl.PasswordResetServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/password-reset")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        try {
            String message = passwordResetService.forgotPassword(request.getEmail());
            return ResponseEntity.ok(Map.of("message", message));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/validate-token")
    public ResponseEntity<?> validateToken(@RequestParam String token) {
        try {
            String message = passwordResetService.validateResetToken(token);
            return ResponseEntity.ok(Map.of("message", message));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            String message = passwordResetService.resetPassword(
                    request.getToken(),
                    request.getPassword(),
                    request.getConfirmPassword()
            );
            return ResponseEntity.ok(Map.of("message", message));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }
}