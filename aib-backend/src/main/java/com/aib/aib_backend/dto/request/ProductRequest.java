package com.aib.aib_backend.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {

    @NotBlank(message = "Product name is required")
    @Size(min = 2, max = 200, message = "Name must be between 2 and 200 characters")
    private String name;

    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description; // Description is now optional

    @NotNull(message = "Category is required")
    private Long categoryId;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private BigDecimal price;

    @Min(value = 0, message = "Discount must be 0 or greater")
    @Max(value = 100, message = "Discount cannot be more than 100")
    private Integer discount;

    @NotNull(message = "Stock quantity is required")
    @Min(value = 0, message = "Stock quantity cannot be negative")
    private Integer stockQuantity;

    // SKU will be generated automatically, so no @NotBlank here
    @Size(max = 50, message = "SKU must not exceed 50 characters")
    private String sku; // SKU is now optional in request

    @NotBlank(message = "Brand is required") // Brand is now required
    @Size(max = 100, message = "Brand must not exceed 100 characters")
    private String brand;

    @Size(max = 100, message = "Model must not exceed 100 characters")
    private String model;

    private String mainImageUrl;

    private Boolean isFeatured;

    private Boolean active;
}
