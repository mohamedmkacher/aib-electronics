package com.aib.aib_backend.service.impl;


import com.aib.aib_backend.dto.request.LoginRequest;
import com.aib.aib_backend.dto.request.RegisterRequest;
import com.aib.aib_backend.dto.response.AuthResponse;
import com.aib.aib_backend.dto.response.UserResponse;
import com.aib.aib_backend.model.Role;
import com.aib.aib_backend.model.User;
import com.aib.aib_backend.model.VerificationToken;
import com.aib.aib_backend.repository.RoleRepository;
import com.aib.aib_backend.repository.UserRepository;
import com.aib.aib_backend.repository.VerificationTokenRepository;
import com.aib.aib_backend.security.GoogleTokenVerifier;
import com.aib.aib_backend.security.JwtUtil;
import com.aib.aib_backend.service.AuthService;
import com.aib.aib_backend.service.EmailService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final GoogleTokenVerifier googleTokenVerifier;

    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final VerificationTokenRepository tokenRepository;


    public AuthResponse register(RegisterRequest request) {
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        // Check if phone already exists
        if (request.getPhone() != null && userRepository.existsByPhone(request.getPhone())) {
            throw new RuntimeException("Phone number already registered");
        }

        // Get USER role
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Role not found"));

        // Create user
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .emailVerified(false)  // Will be verified via email
                .enabled(true)
                .provider("LOCAL")
                .build();

        user.setRole(userRole);

        User savedUser = userRepository.save(user);

        // Create and send verification token
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = VerificationToken.builder()
                .token(token)
                .user(savedUser)
                .expiryDate(LocalDateTime.now().plusHours(24))
                .build();

        tokenRepository.save(verificationToken);

        // Send verification email
        emailService.sendVerificationEmail(savedUser.getEmail(),

                token);

        // Return response without JWT (user must verify email first)
        return AuthResponse.builder()
                .message("Registration successful! Please check your email to verify your account.")
                .build();
    }
    public String verifyEmail(String token) {
        VerificationToken verificationToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid verification token"));

        if (verificationToken.isExpired()) {
            throw new RuntimeException("Verification token has expired");
        }

        if (verificationToken.getVerifiedAt() != null) {
            throw new RuntimeException("Email already verified");
        }

        User user = verificationToken.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);

        verificationToken.setVerifiedAt(LocalDateTime.now());
        tokenRepository.save(verificationToken);

        // Send welcome email
        try {
            emailService.sendWelcomeEmail(user.getEmail(), user.getFirstName());
        } catch (Exception e) {
            System.err.println("Failed to send welcome email: " + e.getMessage());
        }

        return "Email verified successfully! You can now login.";
    }

    public AuthResponse login(LoginRequest request) {
        // Get user first to check email verification
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        // Check if email is verified
        if (!user.getEmailVerified()) {
            throw new RuntimeException("Please verify your email before logging in. Check your inbox.");
        }

        // Authenticate
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            // Update last login
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);

            // Generate token
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtUtil.generateToken(userDetails);

            return AuthResponse.builder()
                    .token(token)
                    .userId(user.getId())
                    .email(user.getEmail())
                    .fullName(user.getFullName())
                    .phone(user.getPhone())
                    .provider(user.getProvider())
                    .roles(Set.of(user.getRole().getName()))
                    .build();

        } catch (DisabledException e) {
            throw new RuntimeException("Account is disabled");
        } catch (Exception e) {
            throw new RuntimeException("Invalid email or password");
        }
    }

    public String resendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getEmailVerified()) {
            throw new RuntimeException("Email is already verified");
        }

        // Delete old token
        tokenRepository.findByUserId(user.getId())
                .ifPresent(tokenRepository::delete);

        // Generate new token
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = VerificationToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusHours(24))
                .build();

        tokenRepository.save(verificationToken);

        // Send email
        emailService.sendVerificationEmail(user.getEmail(), token);

        return "Verification email sent! Please check your inbox.";
    }

    public UserResponse getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .provider(user.getProvider())
                .emailVerified(user.getEmailVerified())
                .roles(Set.of(user.getRole().getName()))
                .createdAt(user.getCreatedAt())
                .build();
    }
    public AuthResponse authenticateWithGoogle(String idToken) {
        try {
            // Verify Google token
            GoogleIdToken.Payload payload = googleTokenVerifier.verify(idToken);

            String email = payload.getEmail();
            String firstName = (String) payload.get("given_name");
            String lastName = (String) payload.get("family_name");
            String googleId = payload.getSubject();

            // Check if user exists
            User user = userRepository.findByEmail(email).orElse(null);

            if (user == null) {
                // Create new user from Google account
                Role userRole = roleRepository.findByName("ROLE_USER")
                        .orElseThrow(() -> new RuntimeException("Role not found"));

                user = User.builder()
                        .email(email)
                        .firstName(firstName)
                        .lastName(lastName)
                        .emailVerified(true)  // Google emails are verified
                        .enabled(true)
                        .provider("GOOGLE")
                        .providerId(googleId)
                        .build();

                user.setRole(userRole);
                user = userRepository.save(user);

                // Send welcome email
                try {
                    emailService.sendWelcomeEmail(user.getEmail(), user.getFirstName());
                } catch (Exception e) {
                    System.err.println("Failed to send welcome email: " + e.getMessage());
                }
            } else {
                // Update existing user
                if (user.getProvider() == null || user.getProvider().equals("LOCAL")) {
                    user.setProvider("GOOGLE");
                    user.setProviderId(googleId);
                    user.setEmailVerified(true);
                    userRepository.save(user);
                }
            }

            // Update last login
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);

            // Generate JWT token
            UserDetails userDetails = org.springframework.security.core.userdetails.User
                    .withUsername(user.getEmail())
                    .password(user.getPassword() != null ? user.getPassword() : "")
                    .authorities(user.getRole().getName())
                    .build();

            String token = jwtUtil.generateToken(userDetails);

            return AuthResponse.builder()
                    .token(token)
                    .userId(user.getId())
                    .email(user.getEmail())
                    .fullName(user.getFullName())
                    .phone(user.getPhone())
                    .provider(user.getProvider())
                    .roles(Set.of(user.getRole().getName()))
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Google authentication failed: " + e.getMessage());
        }
    }
}
