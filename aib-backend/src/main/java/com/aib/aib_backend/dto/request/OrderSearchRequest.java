package com.aib.aib_backend.dto.request;

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
public class OrderSearchRequest {
    private String orderNumber;
    private String customerName;
    private String status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private BigDecimal minTotal;
    private BigDecimal maxTotal;
    private String city;
    private String productName;
    
    private String sortBy;
    private String sortDirection;
    private Integer page;
    private Integer size;
}