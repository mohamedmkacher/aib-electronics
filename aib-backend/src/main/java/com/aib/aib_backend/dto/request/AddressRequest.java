package com.aib.aib_backend.dto.request;


import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressRequest {

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Phone is required")
    private String phone;

    @NotBlank(message = "Address is required")
    private String addressLine;

    @NotBlank(message = "City is required")
    private String city;

    private String postalCode;

    @Builder.Default
    private String country = "Tunisia";

    @Builder.Default
    private Boolean isDefault = false;

    private String label;  // 'Home', 'Work', 'Other'
}