package com.aib.aib_backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(unique = true)
    private String phone;

    // Account status
    @Column(name = "email_verified")
    @Builder.Default
    private Boolean emailVerified = false;

    @Builder.Default
    private Boolean enabled = true;

    // OAuth
    @Builder.Default
    private String provider = "LOCAL";

    @Column(name = "provider_id")
    private String providerId;

    // Relationships
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id")
    private Role role;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Address> addresses = new HashSet<>();

    // Timestamps
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    // Helper methods
    public String getFullName() {
        return firstName + " " + lastName;
    }

    // Compatibility method for existing code that expects a Set
    public Set<Role> getRoles() {
        return role != null ? Set.of(role) : Collections.emptySet();
    }

    public void setRole(Role role) {
        this.role = role;
    }

    // Compatibility method
    public void addRole(Role role) {
        this.role = role;
    }

    public void addAddress(Address address) {
        addresses.add(address);
        address.setUser(this);
    }

    public void removeAddress(Address address) {
        addresses.remove(address);
        address.setUser(null);
    }

    public Address getDefaultAddress() {
        return addresses.stream()
                .filter(Address::getIsDefault)
                .findFirst()
                .orElse(null);
    }
}
