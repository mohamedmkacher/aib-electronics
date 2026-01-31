package com.aib.aib_backend.dto.response;

import java.math.BigDecimal;

public record SalesDataPoint(
        String date,
        BigDecimal amount,
        long orderCount
) {}