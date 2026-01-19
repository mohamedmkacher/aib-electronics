package com.aib.aib_backend.controller;

import com.aib.aib_backend.dto.request.AddToCartRequest;
import com.aib.aib_backend.dto.response.CartResponse;
import com.aib.aib_backend.model.User;
import com.aib.aib_backend.service.CartService;
import com.aib.aib_backend.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.UUID;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class CartController {

    private final CartService cartService;
    private final UserService userService;

    @PostMapping("/add")
    public ResponseEntity<CartResponse> addToCart(
            @RequestBody AddToCartRequest req,
            HttpServletRequest request,
            HttpServletResponse response) {

        String sessionId = getOrCreateCartSession(request, response);
        CartResponse cart = cartService.addItem(sessionId, req.productId(), req.quantity());
        return ResponseEntity.ok(cart);
    }

    @GetMapping
    public ResponseEntity<CartResponse> getCart(
            HttpServletRequest request,
            HttpServletResponse response) {

        String sessionId = getOrCreateCartSession(request, response);
        CartResponse cart = cartService.getCart(sessionId);
        return ResponseEntity.ok(cart);
    }

    @GetMapping("/anonymous")
    public ResponseEntity<CartResponse> getAnonymousCart(
            HttpServletRequest request) {
        String sessionId = getSessionIdFromCookie(request);
        if (sessionId == null) {
            return ResponseEntity.notFound().build();
        }
        CartResponse cart = cartService.toResponse(cartService.getAnonymousCart(sessionId));
        return ResponseEntity.ok(cart);
    }

    @GetMapping("/user")
    public ResponseEntity<CartResponse> getUserCart() {
        User user = userService.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("User not authenticated"));
        CartResponse cart = cartService.toResponse(cartService.getUserCart(user));
        return ResponseEntity.ok(cart);
    }

    @GetMapping("/has-cart")
    public ResponseEntity<Boolean> hasUserCart() {
        User user = userService.getCurrentUser().orElse(null);
        if (user == null) {
            return ResponseEntity.ok(false);
        }
        return ResponseEntity.ok(cartService.hasUserCart(user));
    }

    @PostMapping("/merge")
    public ResponseEntity<CartResponse> mergeCart(
            HttpServletRequest request,
            HttpServletResponse response) {

        String sessionId = getSessionIdFromCookie(request);
        if (sessionId == null) {
            return ResponseEntity.badRequest().build();
        }
        CartResponse cart = cartService.toResponse(cartService.mergeCarts(sessionId));
        return ResponseEntity.ok(cart);
    }

    @PostMapping("/replace")
    public ResponseEntity<CartResponse> replaceCart(
            HttpServletRequest request,
            HttpServletResponse response) {

        String sessionId = getSessionIdFromCookie(request);
        if (sessionId == null) {
            return ResponseEntity.badRequest().build();
        }
        CartResponse cart = cartService.toResponse(cartService.replaceUserCart(sessionId));
        return ResponseEntity.ok(cart);
    }

    @PostMapping("/keep")
    public ResponseEntity<Void> keepCart(
            HttpServletRequest request,
            HttpServletResponse response) {

        String sessionId = getSessionIdFromCookie(request);
        if (sessionId != null) {
            cartService.keepUserCart(sessionId);
        }
        return ResponseEntity.ok().build();
    }

    /**
     * Update quantity of an item in cart
     */
    @PutMapping("/update")
    public ResponseEntity<CartResponse> updateQuantity(
            @RequestBody AddToCartRequest req,
            HttpServletRequest request,
            HttpServletResponse response) {

        String sessionId = getOrCreateCartSession(request, response);
        CartResponse cart = cartService.updateQuantity(sessionId, req.productId(), req.quantity());
        return ResponseEntity.ok(cart);
    }

    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<CartResponse> removeFromCart(
            @PathVariable Long productId,
            HttpServletRequest request,
            HttpServletResponse response) {

        String sessionId = getOrCreateCartSession(request, response);
        CartResponse cart = cartService.removeItem(sessionId, productId);
        return ResponseEntity.ok(cart);
    }

    @DeleteMapping("/clear")
    public ResponseEntity<Void> clearCart(
            HttpServletRequest request,
            HttpServletResponse response) {

        String sessionId = getSessionIdFromCookie(request);

        if (sessionId != null) {
            cartService.clearCart(sessionId);
        }

        Cookie cookie = new Cookie("cart_session", null);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setHttpOnly(true);
        response.addCookie(cookie);

        return ResponseEntity.ok().build();
    }

    // --- Cookie utilities ---
    private String getOrCreateCartSession(HttpServletRequest req, HttpServletResponse res) {
        if (req.getCookies() != null) {
            for (Cookie c : req.getCookies()) {
                if ("cart_session".equals(c.getName())) {
                    return c.getValue();
                }
            }
        }
        String newId = UUID.randomUUID().toString();
        Cookie cookie = new Cookie("cart_session", newId);
        cookie.setPath("/");
        cookie.setMaxAge(7 * 24 * 60 * 60);
        cookie.setHttpOnly(true);
        res.addCookie(cookie);
        return newId;
    }

    private String getSessionIdFromCookie(HttpServletRequest req) {
        if (req.getCookies() == null) return null;
        return Arrays.stream(req.getCookies())
                .filter(c -> "cart_session".equals(c.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElse(null);
    }
}
