package com.aib.aib_backend.dto.response;

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
public class ProductListResponse {

    private List<ProductResponse> products;
    private Long totalElements;
    private Integer totalPages;
    private Integer currentPage;
    private Integer pageSize;
    private Boolean hasNext;
    private Boolean hasPrevious;

    // Filter summary (helpful for UI)
    private FilterSummary filterSummary;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FilterSummary {
        private List<String> availableBrands;
        private BigDecimal minPrice;
        private BigDecimal maxPrice;
        private Integer totalProducts;
    }
}