package com.aib.aib_backend.dto.request;


import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrderRequest {

    @NotEmpty(message = "Cart items are required")
    private List<CartItemDto> items;

    // Just the address ID!
    @NotNull(message = "Shipping address ID is required")
    private Long shippingAddressId;
}

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class CartItemDto {
    private Long productId;
    private Integer quantity;
}