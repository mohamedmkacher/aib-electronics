// src/main/java/com/aib/aib_backend/dto/response/CartResponse.java
package com.aib.aib_backend.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record CartResponse(
        Long cartId,
        int itemCount,
        BigDecimal subtotal, // Total before discounts
        BigDecimal total,    // Total after discounts
        List<CartItemResponse> items
) {}