package com.aib.aib_backend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSearchRequest {

    // Search
    private String keyword;  // Search in name, description, brand, model

    // Filters
    private Long categoryId;
    private String brand;
    private List<String> brands;  // Multiple brands
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Boolean inStock;  // true = only in stock products
    private Boolean hasDiscount;  // true = only discounted products
    private Boolean isFeatured;  // true = only featured products
    private Boolean active;  // Default true (only show active products)
    private String stockStatus; // Added stock status filter

    // Sorting
    private String sortBy;  // price, name, createdAt, rating
    private String sortDirection;  // ASC or DESC

    // Pagination
    private Integer page;  // Default 0
    private Integer size;  // Default 12
}