package com.aib.aib_backend.service;


import com.aib.aib_backend.dto.request.LoginRequest;
import com.aib.aib_backend.dto.request.RegisterRequest;
import com.aib.aib_backend.dto.response.AuthResponse;
import com.aib.aib_backend.dto.response.UserResponse;
public interface AuthService {


    AuthResponse register(RegisterRequest request);

    String verifyEmail(String token);

    AuthResponse login(LoginRequest request);

    String resendVerificationEmail(String email);

    public UserResponse getCurrentUser(String email);

    public AuthResponse authenticateWithGoogle(String idToken);
}
