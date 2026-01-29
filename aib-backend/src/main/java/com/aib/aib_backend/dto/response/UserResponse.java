package com.aib.aib_backend.dto.response;
import lombok.*;
import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private String provider; // Ajout du champ provider
    private Boolean emailVerified;
    private Set<String> roles;
    private LocalDateTime createdAt;
    
}