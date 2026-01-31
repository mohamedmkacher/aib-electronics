package com.aib.aib_backend.dto.response;

public record ProductLowStock(
        Long id,
        String name,
        String imageUrl,
        int stock,
        String category
) {}