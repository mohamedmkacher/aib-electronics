package com.aib.aib_backend.service.impl;

import com.aib.aib_backend.dto.response.*;
import com.aib.aib_backend.repository.*;
import com.aib.aib_backend.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class DashboardServiceImpl implements DashboardService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public DashboardStatsResponse getDashboardStats() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        LocalDateTime startOfMonth = now.withDayOfMonth(1).toLocalDate().atStartOfDay();
        LocalDateTime last30Days = now.minusDays(30);

        // 1. KPIs
        KpiStats kpis = getKpiStats(startOfDay, startOfMonth);

        // 2. Sales chart (last 30 days)
        List<SalesDataPoint> salesChart = getSalesChartData(last30Days);

        // 3. Orders by status distribution
        List<OrderStatusCount> ordersByStatus = getOrdersByStatus();

        // 4. Top 5 products
        List<TopProduct> topProducts = getTopProducts();

        // 5. Recent orders
        List<RecentOrder> recentOrders = getRecentOrders();

        // 6. Low stock products
        List<ProductLowStock> lowStockProducts = getLowStockProducts();

        return new DashboardStatsResponse(
                kpis,
                salesChart,
                ordersByStatus,
                topProducts,
                recentOrders,
                lowStockProducts
        );
    }

    private KpiStats getKpiStats(LocalDateTime startOfDay, LocalDateTime startOfMonth) {
        // Revenue (via stored procedures) - only DELIVERED orders
        BigDecimal totalRevenue     = orderRepository.sumTotalByStatus("DELIVERED")
                .orElse(BigDecimal.ZERO);

        BigDecimal revenueToday     = orderRepository.sumTotalByStatusAndDateAfter("DELIVERED", startOfDay)
                .orElse(BigDecimal.ZERO);

        BigDecimal revenueThisMonth = orderRepository.sumTotalByStatusAndDateAfter("DELIVERED", startOfMonth)
                .orElse(BigDecimal.ZERO);

        // Orders - count non-cancelled orders only
        long totalOrders       = orderRepository.countNonCancelledOrders();
        long ordersToday       = orderRepository.countNonCancelledByCreatedAtAfter(startOfDay);
        long ordersThisMonth   = orderRepository.countNonCancelledByCreatedAtAfter(startOfMonth);

        // Products
        long totalProducts     = productRepository.count();
        long lowStockProducts  = productRepository.countByStockQuantityLessThan(10);

        // Users - count only non-admin users (customers)
        long totalUsers        = userRepository.countNonAdminUsers();
        long newUsersThisMonth = userRepository.countNonAdminUsersByCreatedAtAfter(startOfMonth);

        return new KpiStats(
                totalRevenue,
                revenueToday,
                revenueThisMonth,
                totalOrders,
                ordersToday,
                ordersThisMonth,
                totalProducts,
                lowStockProducts,
                totalUsers,
                newUsersThisMonth
        );
    }

    private List<SalesDataPoint> getSalesChartData(LocalDateTime startDate) {
        LocalDate start = startDate.toLocalDate();
        LocalDate end = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");

        return start.datesUntil(end.plusDays(1))
                .map(date -> {
                    LocalDateTime dayStart = date.atStartOfDay();
                    LocalDateTime dayEnd   = date.plusDays(1).atStartOfDay();

                    // Get total amount for non-cancelled orders
                    BigDecimal amount = orderRepository
                            .sumTotalNonCancelledByCreatedAtBetween(dayStart, dayEnd);
                    if (amount == null) {
                        amount = BigDecimal.ZERO;
                    }

                    // Get count of non-cancelled orders
                    long count = orderRepository.countNonCancelledByCreatedAtBetween(dayStart, dayEnd);

                    return new SalesDataPoint(
                            date.format(formatter),
                            amount,
                            count
                    );
                })
                .collect(Collectors.toList());
    }

    private List<OrderStatusCount> getOrdersByStatus() {
        // Call to stored procedure sp_count_by_status_grouped
        return orderRepository.countByStatusGrouped();
    }

    private List<TopProduct> getTopProducts() {
        int limit = 5;
        int offset = 0;

        return orderRepository.findTopProductsWithLimit(limit, offset);
    }

    private List<RecentOrder> getRecentOrders() {
        return orderRepository.findTop10ByOrderByCreatedAtDesc()
                .stream()
                .filter(order -> order.getCreatedAt() != null)
                .map(order -> new RecentOrder(
                        order.getId(),
                        order.getOrderNumber(),
                        order.getUser() != null ? order.getUser().getFullName() : "Unknown Customer",
                        order.getTotal(),
                        order.getStatus(),
                        order.getCreatedAt().toString()
                ))
                .toList();
    }

    private List<ProductLowStock> getLowStockProducts() {
        return productRepository.findByStockQuantityLessThanOrderByStockQuantityAsc(10)
                .stream()
                .limit(10)
                .map(p -> new ProductLowStock(
                        p.getId(),
                        p.getName(),
                        p.getMainImageUrl(),
                        p.getStockQuantity(),
                        p.getCategory() != null ? p.getCategory().getName() : "Uncategorized"
                ))
                .toList();
    }
}