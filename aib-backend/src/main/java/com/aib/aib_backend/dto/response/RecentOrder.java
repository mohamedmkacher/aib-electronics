package com.aib.aib_backend.dto.response;

import java.math.BigDecimal;

public record RecentOrder(
        Long orderId,
        String orderNumber,
        String customerName,
        BigDecimal total,
        String status,
        String createdAt
) {}