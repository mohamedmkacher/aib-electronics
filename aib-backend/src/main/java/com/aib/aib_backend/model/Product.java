package com.aib.aib_backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String slug;

    @Column(unique = true, nullable = false)
    private String sku;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String brand;
    
    private String model;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "discount")
    @Builder.Default
    private Integer discount = 0; // Pourcentage de remise, par ex. 10 pour 10%

    @Column(name = "stock_quantity", nullable = false)
    @Builder.Default
    private Integer stockQuantity = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    @JsonIgnore
    private Category category;

    @Column(name = "main_image_url")
    private String mainImageUrl;

    @Column(name = "is_featured")
    @Builder.Default
    private Boolean isFeatured = false;

    @Builder.Default
    private Boolean active = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public boolean isInStock() {
        return stockQuantity != null && stockQuantity > 0;
    }

    public BigDecimal getCurrentPrice() {
        if (discount != null && discount > 0 && price != null) {
            BigDecimal discountMultiplier = BigDecimal.ONE.subtract(BigDecimal.valueOf(discount).divide(BigDecimal.valueOf(100)));
            return price.multiply(discountMultiplier).setScale(2, RoundingMode.HALF_UP);
        }
        return price;
    }

    public void decrementStock(Integer quantity) {
        if (this.stockQuantity >= quantity) {
            this.stockQuantity -= quantity;
        } else {
            throw new IllegalStateException("Insufficient stock");
        }
    }

    public void incrementStock(Integer quantity) {
        this.stockQuantity += quantity;

    }
}
