package com.aib.aib_backend.repository;

import com.aib.aib_backend.dto.response.OrderStatusCount;
import com.aib.aib_backend.dto.response.TopProduct;
import com.aib.aib_backend.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    @Procedure(procedureName = "sp_get_orders_by_user_ordered")
    List<Order> findByUserIdOrderByCreatedAtDesc(@Param("p_user_id") Long userId);

    @Procedure(procedureName = "sp_get_order_by_payment_intent")
    Optional<Order> findByStripePaymentIntentId(@Param("p_payment_intent_id") String stripePaymentIntentId);

    @Procedure(procedureName = "sp_sum_total_by_status", outputParameterName = "p_total")
    BigDecimal sumTotalByStatusRaw(@Param("p_status") String status);

    default Optional<BigDecimal> sumTotalByStatus(String status) {
        BigDecimal result = sumTotalByStatusRaw(status);
        return Optional.ofNullable(result);
    }

    @Procedure(procedureName = "sp_sum_total_by_status_and_date_after", outputParameterName = "p_total")
    BigDecimal sumTotalByStatusAndDateAfterRaw(@Param("p_status") String status, @Param("p_date") LocalDateTime date);

    default Optional<BigDecimal> sumTotalByStatusAndDateAfter(String status, LocalDateTime date) {
        BigDecimal result = sumTotalByStatusAndDateAfterRaw(status, date);
        return Optional.ofNullable(result);
    }

    @Procedure(procedureName = "sp_sum_total_by_status_and_date_between", outputParameterName = "p_total")
    BigDecimal sumTotalByStatusAndDateBetweenRaw(
            @Param("p_status") String status,
            @Param("p_start") LocalDateTime start,
            @Param("p_end") LocalDateTime end
    );

    default Optional<BigDecimal> sumTotalByStatusAndDateBetween(String status, LocalDateTime start, LocalDateTime end) {
        BigDecimal result = sumTotalByStatusAndDateBetweenRaw(status, start, end);
        return Optional.ofNullable(result);
    }

    @Procedure(procedureName = "sp_count_orders_after_date", outputParameterName = "p_count")
    Long countByCreatedAtAfterRaw(@Param("p_date") LocalDateTime date);

    default long countByCreatedAtAfter(LocalDateTime date) {
        Long result = countByCreatedAtAfterRaw(date);
        return result != null ? result : 0L;
    }

    @Procedure(procedureName = "sp_count_orders_between_dates", outputParameterName = "p_count")
    Long countByCreatedAtBetweenRaw(@Param("p_start") LocalDateTime start, @Param("p_end") LocalDateTime end);

    default long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end) {
        Long result = countByCreatedAtBetweenRaw(start, end);
        return result != null ? result : 0L;
    }

    @Procedure(procedureName = "sp_count_non_cancelled_orders", outputParameterName = "p_count")
    Long countNonCancelledOrdersRaw();

    default long countNonCancelledOrders() {
        Long result = countNonCancelledOrdersRaw();
        return result != null ? result : 0L;
    }

    @Procedure(procedureName = "sp_count_non_cancelled_orders_after", outputParameterName = "p_count")
    Long countNonCancelledByCreatedAtAfterRaw(@Param("p_date") LocalDateTime date);

    default long countNonCancelledByCreatedAtAfter(LocalDateTime date) {
        Long result = countNonCancelledByCreatedAtAfterRaw(date);
        return result != null ? result : 0L;
    }

    @Procedure(procedureName = "sp_count_non_cancelled_orders_between", outputParameterName = "p_count")
    Long countNonCancelledByCreatedAtBetweenRaw(@Param("p_start") LocalDateTime start, @Param("p_end") LocalDateTime end);

    default long countNonCancelledByCreatedAtBetween(LocalDateTime start, LocalDateTime end) {
        Long result = countNonCancelledByCreatedAtBetweenRaw(start, end);
        return result != null ? result : 0L;
    }

    @Procedure(procedureName = "sp_sum_non_cancelled_orders_between", outputParameterName = "p_total")
    BigDecimal sumTotalNonCancelledByCreatedAtBetween(@Param("p_start") LocalDateTime start, @Param("p_end") LocalDateTime end);

    @Procedure(procedureName = "sp_count_by_status_grouped")
    List<Object[]> countByStatusGroupedRaw();

    default List<OrderStatusCount> countByStatusGrouped() {
        return countByStatusGroupedRaw().stream()
                .map(row -> new OrderStatusCount(
                        (String) row[0],
                        ((Number) row[1]).longValue()
                ))
                .toList();
    }

    @Procedure(procedureName = "sp_find_top_products")
    List<Object[]> findTopProductsRaw(@Param("p_limit") Integer limit, @Param("p_offset") Integer offset);

    default List<TopProduct> findTopProductsWithLimit(Integer limit, Integer offset) {
        return findTopProductsRaw(limit, offset).stream()
                .map(row -> new TopProduct(
                        ((Number) row[0]).longValue(),
                        (String) row[1],
                        (String) row[2],
                        ((Number) row[3]).intValue(),
                        (BigDecimal) row[4]
                ))
                .toList();
    }

    @Procedure(procedureName = "sp_get_recent_orders")
    List<Order> findRecentOrders(@Param("p_limit") Integer limit);

    default List<Order> findTop10ByOrderByCreatedAtDesc() {
        return findRecentOrders(10);
    }

    @Procedure(procedureName = "sp_search_orders")
    List<Order> searchOrders(
            @Param("p_order_number") String orderNumber,
            @Param("p_customer_name") String customerName,
            @Param("p_status") String status,
            @Param("p_start_date") LocalDateTime startDate,
            @Param("p_end_date") LocalDateTime endDate,
            @Param("p_min_total") BigDecimal minTotal,
            @Param("p_max_total") BigDecimal maxTotal,
            @Param("p_city") String city,
            @Param("p_product_name") String productName,
            @Param("p_sort_by") String sortBy,
            @Param("p_sort_direction") String sortDirection,
            @Param("p_limit") Integer limit,
            @Param("p_offset") Integer offset
    );

    @Procedure(procedureName = "sp_count_search_orders", outputParameterName = "p_count")
    Long countSearchOrders(
            @Param("p_order_number") String orderNumber,
            @Param("p_customer_name") String customerName,
            @Param("p_status") String status,
            @Param("p_start_date") LocalDateTime startDate,
            @Param("p_end_date") LocalDateTime endDate,
            @Param("p_min_total") BigDecimal minTotal,
            @Param("p_max_total") BigDecimal maxTotal,
            @Param("p_city") String city,
            @Param("p_product_name") String productName
    );
}
