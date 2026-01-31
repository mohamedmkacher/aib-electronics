// TopProduct.java
package com.aib.aib_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TopProduct {
    private Long id;
    private String name;
    private String imageUrl;
    private Integer totalSold;
    private BigDecimal revenue;
}