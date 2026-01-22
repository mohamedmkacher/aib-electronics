package com.aib.aib_backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonIgnore
    private Order order;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price; // Prix de base du produit au moment de la commande

    @Column(name = "discount")
    @Builder.Default
    private Integer discount = 0; // Pourcentage de remise au moment de la commande

    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Transient
    public BigDecimal getUnitPrice() {
        if (price != null && discount != null && discount > 0) {
            BigDecimal discountMultiplier = BigDecimal.ONE.subtract(BigDecimal.valueOf(discount).divide(BigDecimal.valueOf(100)));
            return price.multiply(discountMultiplier).setScale(2, RoundingMode.HALF_UP);
        }
        return price;
    }

    @PrePersist
    @PreUpdate
    public void calculateTotalPrice() {
        if (quantity != null) {
            this.totalPrice = getUnitPrice().multiply(BigDecimal.valueOf(quantity));
        }
    }
}