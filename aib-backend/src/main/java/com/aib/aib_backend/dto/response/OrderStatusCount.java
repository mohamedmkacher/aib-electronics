// OrderStatusCount.java
package com.aib.aib_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderStatusCount {
    private String status;
    private Long count;
}