package com.aib.aib_backend.service;

import com.aib.aib_backend.dto.response.CartResponse;

import com.aib.aib_backend.model.*;


public interface CartService {


    Cart getActiveCart(String sessionId);

    Cart getAnonymousCart(String sessionId);

    Cart getUserCart(User user);

    boolean hasUserCart(User user);

    Cart mergeCarts(String anonymousSessionId);

    Cart replaceUserCart(String anonymousSessionId);

    void keepUserCart(String anonymousSessionId);


    CartResponse addItem(String sessionId, Long productId, int qty);

    CartResponse getCart(String sessionId);

    CartResponse updateQuantity(String sessionId, Long productId, int quantity);

    CartResponse removeItem(String sessionId, Long productId);

    void clearCart(String sessionId);

    void clearCart(Cart cart);

    CartResponse toResponse(Cart cart);

    void validateCartForCheckout(Cart cart);

}
