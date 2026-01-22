// src/main/java/com/aib/aib_backend/dto/request/CheckoutRequest.java
package com.aib.aib_backend.dto.request;

public record CheckoutRequest(
        Long addressId,
        String paymentMethodId
) {}