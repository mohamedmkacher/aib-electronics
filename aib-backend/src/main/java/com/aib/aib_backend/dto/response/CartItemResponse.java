// src/main/java/com/aib/aib_backend/dto/response/CartItemResponse.java
package com.aib.aib_backend.dto.response;

import java.math.BigDecimal;

public record CartItemResponse(
        Long productId,
        String name,
        String slug,
        String imageUrl,
        BigDecimal price,
        int discount,
        BigDecimal unitPrice,
        int quantity,
        BigDecimal subtotal,
        int stockQuantity,
        boolean active // Added active status
) {}