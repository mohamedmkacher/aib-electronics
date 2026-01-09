package com.aib.aib_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {

    private Long id;
    private String name;
    private String slug;
    private String sku;
    private String description;
    private String brand;
    private String model;

    // Category info
    private Long categoryId;
    private String categoryName;
    private String categorySlug;

    // Pricing
    private BigDecimal price;
    private BigDecimal discountPrice;
    private BigDecimal currentPrice;  // Calculated: discountPrice or price
    private Integer discountPercentage;  // Calculated
    private Boolean hasDiscount;

    // Stock
    private Integer stockQuantity;
    private Boolean inStock;

    // Image
    private String mainImageUrl;

    // Features
    private Boolean isFeatured;
    private Boolean active;
    private Boolean categoryActive;

    // Metadata
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}