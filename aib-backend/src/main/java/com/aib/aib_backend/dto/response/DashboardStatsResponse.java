// src/main/java/com/aib/aib_backend/dto/response/DashboardStatsResponse.java
package com.aib.aib_backend.dto.response;

import java.util.List;

public record DashboardStatsResponse(
        KpiStats kpis,
        List<SalesDataPoint> salesChart,
        List<OrderStatusCount> ordersByStatus,
        List<TopProduct> topProducts,
        List<RecentOrder> recentOrders,
        List<ProductLowStock> lowStockProducts
) {}