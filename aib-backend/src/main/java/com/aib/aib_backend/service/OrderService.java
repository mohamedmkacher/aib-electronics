package com.aib.aib_backend.service;

import com.aib.aib_backend.dto.request.OrderSearchRequest;
import com.aib.aib_backend.dto.response.OrderListResponse;
import com.aib.aib_backend.model.*;
import java.math.BigDecimal;

import java.util.List;



public interface OrderService {

    Order createOrderFromCart(Cart cart, String paymentIntentId, Address shippingAddress,
                              BigDecimal subtotal, BigDecimal tax, BigDecimal shippingFee);

    Order createOrderFromCart(Cart cart, String paymentIntentId, Address shippingAddress);

    List<Order> getUserOrders(Long userId);

    List<Order> getUserOrdersForAdmin(Long userId);

    Order getOrderDetails(Long orderId, Long userId);

    Order getOrderDetailsForAdmin(Long orderId);

    Order cancelOrder(Long orderId, Long userId, String reason);

    boolean canCancelOrder(Long orderId, Long userId);

    OrderListResponse searchOrders(OrderSearchRequest request);

    Order updateOrderStatus(Long orderId, String newStatus);
}
