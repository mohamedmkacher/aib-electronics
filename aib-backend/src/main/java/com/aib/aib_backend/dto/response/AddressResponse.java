package com.aib.aib_backend.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressResponse {
    private Long id;
    private String fullName;
    private String phone;
    private String addressLine;
    private String city;
    private String postalCode;
    private String country;
    private Boolean isDefault;
    private String label;
}