package com.aib.aib_backend.service.impl;

import com.aib.aib_backend.dto.request.OrderSearchRequest;
import com.aib.aib_backend.dto.response.OrderListResponse;
import com.aib.aib_backend.model.*;
import com.aib.aib_backend.repository.OrderRepository;
import com.aib.aib_backend.repository.ProductRepository;
import com.aib.aib_backend.service.*;
import com.stripe.exception.StripeException;
import com.stripe.model.Refund;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.TreeSet;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class OrderServiceImpl implements OrderService {
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final StripeService stripeService;
    private final EmailService emailService;
    private final UserService userService;

    public Order createOrderFromCart(Cart cart, String paymentIntentId, Address shippingAddress,
                                     BigDecimal subtotal, BigDecimal tax, BigDecimal shippingFee) {
        if (cart == null || cart.getItems().isEmpty()) {
            throw new IllegalArgumentException("Panier vide");
        }

        cartService.validateCartForCheckout(cart);

        BigDecimal orderSubtotal = subtotal != null && subtotal.compareTo(BigDecimal.ZERO) > 0
                ? subtotal
                : cartService.toResponse(cart).total();

        BigDecimal orderTax = tax != null ? tax : BigDecimal.ZERO;
        BigDecimal orderShippingFee = shippingFee != null ? shippingFee : BigDecimal.ZERO;
        BigDecimal orderTotal = orderSubtotal.add(orderTax).add(orderShippingFee);

        LocalDateTime now = LocalDateTime.now();
        Order order = Order.builder()
                .user(cart.getUser())
                .shippingAddress(shippingAddress)
                .subtotal(orderSubtotal)
                .tax(orderTax)
                .shippingFee(orderShippingFee)
                .total(orderTotal)
                .status("PLACED")
                .paymentStatus("PAID")
                .stripePaymentIntentId(paymentIntentId)
                .createdAt(now)
                .paidAt(now)
                .build();

        cart.getItems().forEach(cartItem -> {
            Product product = cartItem.getProduct();
            if (product.getStockQuantity() < cartItem.getQuantity()) {
                throw new RuntimeException("Stock insuffisant pour le produit : " + product.getName());
            }
            product.decrementStock(cartItem.getQuantity());
            productRepository.save(product);

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(cartItem.getQuantity())
                    .price(product.getPrice())
                    .discount(product.getDiscount())
                    .build();
            orderItem.calculateTotalPrice();
            order.getItems().add(orderItem);
        });

        orderRepository.save(order);
        cartService.clearCart(cart);

        log.info("Order created: {} - Subtotal: {}, Tax: {}, Shipping: {}, Total: {}",
                order.getOrderNumber(), orderSubtotal, orderTax, orderShippingFee, orderTotal);

        try {
            emailService.sendOrderConfirmationEmail(
                    order.getUser().getEmail(),
                    order.getUser().getFirstName(),
                    order,
                    new TreeSet<>(order.getItems())
            );
        } catch (Exception e) {
            log.error("Failed to send order confirmation email", e);
        }

        return order;
    }

    public Order createOrderFromCart(Cart cart, String paymentIntentId, Address shippingAddress) {
        return createOrderFromCart(cart, paymentIntentId, shippingAddress, null, null, null);
    }

    public List<Order> getUserOrders(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<Order> getUserOrdersForAdmin(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public Order getOrderDetails(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Commande non trouvée"));
        User currentUser = userService.getCurrentUser().orElse(null);
        if (currentUser != null && !currentUser.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_ADMIN"))) {
            if (userId != null && !order.getUser().getId().equals(userId)) {
                throw new RuntimeException("Accès non autorisé à cette commande");
            }
        }
        return order;
    }

    public Order getOrderDetailsForAdmin(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Commande non trouvée"));
    }

    public Order cancelOrder(Long orderId, Long userId, String reason) {
        Order order = getOrderDetails(orderId, userId);
        if (!order.canBeCancelled()) {
            throw new RuntimeException("Cette commande ne peut plus être annulée.");
        }
        return processCancellation(order, reason);
    }

    private Order processCancellation(Order order, String reason) {
        if (order.getStripePaymentIntentId() != null && "PAID".equals(order.getPaymentStatus())) {
            try {
                Refund refund = stripeService.createFullRefund(order.getStripePaymentIntentId());
                log.info("Refund processed successfully for order {}: Refund ID = {}", order.getOrderNumber(), refund.getId());
                order.markAsRefunded(refund.getId());
                emailService.sendRefundProcessedEmail(order.getUser().getEmail(), order.getUser().getFirstName(), order, refund.getAmount() / 100.0);
            } catch (StripeException e) {
                log.error("Failed to process refund for order {}: {}", order.getOrderNumber(), e.getMessage());
                order.setPaymentStatus("REFUND_PENDING");
            }
        }

        order.getItems().forEach(orderItem -> {
            Product product = orderItem.getProduct();
            product.incrementStock(orderItem.getQuantity());
            productRepository.save(product);
        });

        order.cancel(reason);
        Order savedOrder = orderRepository.save(order);

        log.info("Order {} cancelled successfully.", order.getOrderNumber());
        return savedOrder;
    }

    public boolean canCancelOrder(Long orderId, Long userId) {
        Order order = getOrderDetails(orderId, userId);
        return order.canBeCancelled();
    }

    public OrderListResponse searchOrders(OrderSearchRequest request) {
        int page = Math.max(0, request.getPage() != null ? request.getPage() : 0);
        int size = Math.max(1, request.getSize() != null ? request.getSize() : 10);
        int offset = page * size;

        List<Order> orders = orderRepository.searchOrders(
                request.getOrderNumber(), request.getCustomerName(), request.getStatus(),
                request.getStartDate(), request.getEndDate(), request.getMinTotal(),
                request.getMaxTotal(), request.getCity(), request.getProductName(),
                request.getSortBy(), request.getSortDirection(), size, offset
        );

        long totalElements = orderRepository.countSearchOrders(
                request.getOrderNumber(), request.getCustomerName(), request.getStatus(),
                request.getStartDate(), request.getEndDate(), request.getMinTotal(),
                request.getMaxTotal(), request.getCity(), request.getProductName()
        );

        int totalPages = (int) Math.ceil((double) totalElements / size);

        return OrderListResponse.builder()
                .orders(orders)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .currentPage(page)
                .pageSize(size)
                .hasNext(page < totalPages - 1)
                .hasPrevious(page > 0)
                .build();
    }

    public Order updateOrderStatus(Long orderId, String newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Commande non trouvée"));
        String oldStatus = order.getStatus();
        validateStatusTransition(oldStatus, newStatus);

        order.setStatus(newStatus);
        Order savedOrder = orderRepository.save(order);

        if (!oldStatus.equals(newStatus)) {
            try {
                emailService.sendOrderStatusUpdateEmail(order.getUser().getEmail(), order.getUser().getFirstName(), savedOrder);
                log.info("Order status update email sent for order {}", order.getOrderNumber());
            } catch (Exception e) {
                log.error("Failed to send order status update email", e);
            }
        }
        return savedOrder;
    }

    private void validateStatusTransition(String currentStatus, String newStatus) {
        switch (currentStatus) {
            case "PLACED":
                if (!"PROCESSING".equals(newStatus)) {
                    throw new IllegalStateException("Invalid status transition from PLACED to " + newStatus);
                }
                break;
            case "PROCESSING":
                if (!"SHIPPED".equals(newStatus)) {
                    throw new IllegalStateException("Invalid status transition from PROCESSING to " + newStatus);
                }
                break;
            case "SHIPPED":
                if (!"DELIVERED".equals(newStatus)) {
                    throw new IllegalStateException("Invalid status transition from SHIPPED to " + newStatus);
                }
                break;
            case "DELIVERED":
            case "CANCELLED":
                throw new IllegalStateException("Cannot change status of " + currentStatus + " order");
            default:
                throw new IllegalStateException("Unknown order status: " + currentStatus);
        }
    }
}
