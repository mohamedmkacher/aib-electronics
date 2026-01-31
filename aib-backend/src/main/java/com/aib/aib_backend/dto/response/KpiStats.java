// src/main/java/com/aib/aib_backend/dto/response/KpiStats.java
package com.aib.aib_backend.dto.response;

import java.math.BigDecimal;

public record KpiStats(
        BigDecimal totalRevenue,
        BigDecimal revenueToday,
        BigDecimal revenueThisMonth,
        long totalOrders,
        long ordersToday,
        long ordersThisMonth,
        long totalProducts,
        long lowStockProducts,
        long totalUsers,
        long newUsersThisMonth
) {}