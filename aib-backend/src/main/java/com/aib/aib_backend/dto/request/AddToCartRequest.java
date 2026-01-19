// src/main/java/com/aib/aib_backend/dto/request/AddToCartRequest.java
package com.aib.aib_backend.dto.request;

public record AddToCartRequest(
        Long productId,
        Integer quantity
) {}