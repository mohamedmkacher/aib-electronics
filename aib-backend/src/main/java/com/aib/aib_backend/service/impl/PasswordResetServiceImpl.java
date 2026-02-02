package com.aib.aib_backend.service.impl;


import com.aib.aib_backend.model.PasswordResetToken;
import com.aib.aib_backend.model.User;
import com.aib.aib_backend.repository.PasswordResetTokenRepository;
import com.aib.aib_backend.repository.UserRepository;
import com.aib.aib_backend.service.EmailService;
import com.aib.aib_backend.service.PasswordResetService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PasswordResetServiceImpl implements PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    public String forgotPassword(String email) {
        // Find user by email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("No account found with this email address"));

        // Delete any existing tokens for this user
        tokenRepository.findByUserId(user.getId())
                .ifPresent(tokenRepository::delete);

        // Generate new token
        String token = UUID.randomUUID().toString();

        // Create reset token
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusHours(1))
                .build();

        tokenRepository.save(resetToken);

        // Send reset email
        try {
            emailService.sendPasswordResetEmail(user.getEmail(), token);
            System.out.println("✅ Password reset email sent to: " + user.getEmail());
        } catch (Exception e) {
            System.err.println("❌ Failed to send password reset email: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to send password reset email. Please try again later.");
        }

        return "Password reset email sent. Please check your inbox.";
    }

    public String validateResetToken(String token) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid password reset token"));

        if (resetToken.isExpired()) {
            throw new RuntimeException("Password reset token has expired. Please request a new one.");
        }

        if (resetToken.isUsed()) {
            throw new RuntimeException("This password reset link has already been used");
        }

        return "Token is valid";
    }

    public String resetPassword(String token, String newPassword, String confirmPassword) {
        // Validate passwords match
        if (!newPassword.equals(confirmPassword)) {
            throw new RuntimeException("Passwords do not match");
        }

        // Validate password strength
        if (newPassword.length() < 8) {
            throw new RuntimeException("Password must be at least 8 characters long");
        }

        // Find token
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid password reset token"));

        // Validate token
        if (resetToken.isExpired()) {
            throw new RuntimeException("Password reset token has expired");
        }

        if (resetToken.isUsed()) {
            throw new RuntimeException("This password reset link has already been used");
        }

        // Get user and update password
        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Mark token as used
        resetToken.setUsedAt(LocalDateTime.now());
        tokenRepository.save(resetToken);

        // Send confirmation email
        emailService.sendPasswordResetConfirmationEmail(user.getEmail(), user.getFirstName());

        return "Password has been reset successfully. You can now login with your new password.";
    }

    // Cleanup expired tokens (can be called by a scheduled task)
    public void cleanupExpiredTokens() {
        tokenRepository.deleteByExpiryDateBefore(LocalDateTime.now().minusDays(1));
    }
}