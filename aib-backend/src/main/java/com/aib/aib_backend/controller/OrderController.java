// src/main/java/com/aib/aib_backend/controller/OrderController.java
package com.aib.aib_backend.controller;

import com.aib.aib_backend.dto.request.CancelOrderRequest;
import com.aib.aib_backend.dto.request.OrderSearchRequest;
import com.aib.aib_backend.dto.response.OrderListResponse;
import com.aib.aib_backend.model.Order;
import com.aib.aib_backend.service.OrderService;
import com.aib.aib_backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class OrderController {

    private final OrderService orderService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<Order>> getUserOrders() {
        Long userId = userService.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("Utilisateur non connecté"))
                .getId();

        List<Order> orders = orderService.getUserOrders(userId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getOrderDetails(@PathVariable Long orderId) {
        Long userId = userService.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("Utilisateur non connecté"))
                .getId();

        Order order = orderService.getOrderDetails(orderId, userId);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/admin/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Order>> getUserOrdersForAdmin(@PathVariable Long userId) {
        List<Order> orders = orderService.getUserOrdersForAdmin(userId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/admin/{orderId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Order> getOrderDetailsForAdmin(@PathVariable Long orderId) {
        Order order = orderService.getOrderDetailsForAdmin(orderId);
        return ResponseEntity.ok(order);
    }

    /**
     * Check if an order can be cancelled
     */
    @GetMapping("/{orderId}/can-cancel")
    public ResponseEntity<Boolean> canCancelOrder(@PathVariable Long orderId) {
        Long userId = userService.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("Utilisateur non connecté"))
                .getId();

        boolean canCancel = orderService.canCancelOrder(orderId, userId);
        return ResponseEntity.ok(canCancel);
    }

    /**
     * Cancel an order (only within 24 hours of placing)
     */
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<Order> cancelOrder(
            @PathVariable Long orderId,
            @Valid @RequestBody CancelOrderRequest request) {

        Long userId = userService.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("Utilisateur non connecté"))
                .getId();

        Order cancelledOrder = orderService.cancelOrder(orderId, userId, request.getReason());
        return ResponseEntity.ok(cancelledOrder);
    }

    /**
     * Search orders (Admin)
     */
    @PostMapping("/admin/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderListResponse> searchOrders(@RequestBody OrderSearchRequest request) {
        OrderListResponse response = orderService.searchOrders(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Update order status (Admin)
     */
    @PatchMapping("/{orderId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Order> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestBody Map<String, String> statusMap) {
        String status = statusMap.get("status");
        Order updatedOrder = orderService.updateOrderStatus(orderId, status);
        return ResponseEntity.ok(updatedOrder);
    }
}
