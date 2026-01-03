package com.aib.aib_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GoogleAuthRequest {
    @NotBlank(message = "Google token is required")
    private String token;  // ID token from Google
}