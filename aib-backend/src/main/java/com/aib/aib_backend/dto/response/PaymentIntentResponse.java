// src/main/java/com/aib/aib_backend/dto/response/PaymentIntentResponse.java
package com.aib.aib_backend.dto.response;

public record PaymentIntentResponse(
        String clientSecret,
        String paymentIntentId,
        Long amount
) {}