package com.aib.aib_backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

@ToString(exclude = {"user", "shippingAddress", "items"})
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_number", unique = true, nullable = false)
    @Builder.Default
    private String orderNumber = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    // Shipping address (Foreign Key to addresses table!)
    // REMOVED cascade = CascadeType.ALL to prevent duplicate address creation
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipping_address_id", nullable = false)
    @JsonIgnore
    private Address shippingAddress;

    // Pricing
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal tax = BigDecimal.ZERO;

    @Column(name = "shipping_fee", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal shippingFee = BigDecimal.ZERO;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    // Status: PLACED -> PROCESSING -> SHIPPED -> DELIVERED (or CANCELLED)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "PLACED";

    // Payment status: PENDING, PAID, REFUNDED, REFUND_PENDING, FAILED
    @Column(name = "payment_status", nullable = false, length = 20)
    @Builder.Default
    private String paymentStatus = "PENDING";

    // Stripe Payment
    @Column(name = "stripe_payment_intent_id", length = 255)
    private String stripePaymentIntentId;

    // Stripe Refund ID (set when order is cancelled and refunded)
    @Column(name = "stripe_refund_id", length = 255)
    private String stripeRefundId;

    // Items
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<OrderItem> items = new HashSet<>();

    // Timestamps
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "refunded_at")
    private LocalDateTime refundedAt;

    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.orderNumber == null || this.orderNumber.isEmpty()) {
            this.orderNumber = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
    }

    // Helper methods
    public void calculateTotal() {
        this.total = subtotal.add(tax).add(shippingFee);
    }

    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }

    public void removeItem(OrderItem item) {
        items.remove(item);
        item.setOrder(null);
    }

    public void markAsPaid() {
        this.paymentStatus = "PAID";
        this.paidAt = LocalDateTime.now();
    }

    public void markAsRefunded(String refundId) {
        this.paymentStatus = "REFUNDED";
        this.stripeRefundId = refundId;
        this.refundedAt = LocalDateTime.now();
    }

    /**
     * Check if order can be cancelled (within 24 hours of creation)
     */
    public boolean canBeCancelled() {
        if (this.status.equals("CANCELLED") ||
                this.status.equals("SHIPPED") ||
                this.status.equals("DELIVERED")) {
            return false;
        }
        // Can only cancel within 24 hours
        LocalDateTime cancellationDeadline = this.createdAt.plusHours(24);
        return LocalDateTime.now().isBefore(cancellationDeadline);
    }

    /**
     * Cancel the order with a reason
     */
    public void cancel(String reason) {
        if (!canBeCancelled()) {
            throw new IllegalStateException("Order cannot be cancelled after 24 hours or if already shipped/delivered");
        }
        this.status = "CANCELLED";
        this.cancelledAt = LocalDateTime.now();
        this.cancellationReason = reason;
    }

    /**
     * Check if order was refunded
     */
    public boolean isRefunded() {
        return "REFUNDED".equals(this.paymentStatus) && this.stripeRefundId != null;
    }

    // Convenient methods to get shipping details
    public String getShippingName() {
        return shippingAddress != null ? shippingAddress.getFullName() : null;
    }

    public String getShippingPhone() {
        return shippingAddress != null ? shippingAddress.getPhone() : null;
    }

    public String getShippingFullAddress() {
        return shippingAddress != null ? shippingAddress.getFullAddress() : null;
    }
}