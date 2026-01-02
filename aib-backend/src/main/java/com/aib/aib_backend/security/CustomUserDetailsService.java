package com.aib.aib_backend.security;

import com.aib.aib_backend.model.User;
import com.aib.aib_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        Set<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toSet());

        // CRITICAL FIX: Handle null passwords for OAuth users
        String password = user.getPassword();
        if (password == null || password.isEmpty()) {
            // OAuth users don't have passwords - use a placeholder
            // This password will never be used for authentication since OAuth users
            // are authenticated via JWT tokens, not password
            password = "{noop}"; // or use a random UUID
        }

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(password)  // Now guaranteed to be non-null
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(!user.getEnabled())
                .credentialsExpired(false)
                .disabled(!user.getEnabled())
                .build();
    }
}