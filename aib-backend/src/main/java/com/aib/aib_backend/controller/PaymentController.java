// src/main/java/com/aib/aib_backend/controller/PaymentController.java
package com.aib.aib_backend.controller;

import com.aib.aib_backend.model.Address;
import com.aib.aib_backend.model.Cart;
import com.aib.aib_backend.model.Order;
import com.aib.aib_backend.model.User;
import com.aib.aib_backend.service.*;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class PaymentController {

    private final CartService cartService;
    private final OrderService orderService;
    private final UserService userService;
    private final AddressService addressService;


    @PostMapping("/create-payment-intent")
    public ResponseEntity<Map<String, String>> createPaymentIntent(
            @RequestBody Map<String, Object> payload,
            HttpServletRequest request) throws StripeException {

        User user = userService.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("Vous devez être connecté"));

        String sessionId = getCookie(request, "cart_session");

        if (sessionId == null) {
            throw new RuntimeException("Session de panier introuvable. Veuillez ajouter des articles au panier.");
        }

        Cart cart = cartService.getActiveCart(sessionId);

        if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new RuntimeException("Votre panier est vide");
        }

        Long amount = cartService.toResponse(cart).total()
                .multiply(BigDecimal.valueOf(100)).longValue();

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amount)
                .setCurrency("usd")
                .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                .setEnabled(true)
                                .build()
                )
                .putMetadata("user_id", user.getId().toString())
                .putMetadata("cart_id", cart.getId().toString())
                .build();

        PaymentIntent intent = PaymentIntent.create(params);

        return ResponseEntity.ok(Map.of("clientSecret", intent.getClientSecret()));
    }

    @PostMapping("/confirm-payment")
    public ResponseEntity<Map<String, Object>> confirmPayment(
            @RequestBody Map<String, Object> payload,
            HttpServletRequest request) {

        try {
            User user = userService.getCurrentUser()
                    .orElseThrow(() -> new RuntimeException("Vous devez être connecté"));

            String paymentIntentId = (String) payload.get("paymentIntentId");

            if (paymentIntentId == null || paymentIntentId.isEmpty()) {
                throw new RuntimeException("PaymentIntent ID manquant");
            }

            String sessionId = getCookie(request, "cart_session");
            if (sessionId == null) {
                throw new RuntimeException("Session de panier introuvable");
            }

            Cart cart = cartService.getActiveCart(sessionId);

            if (cart == null || cart.getItems().isEmpty()) {
                throw new RuntimeException("Votre panier est vide");
            }

            // Get or create shipping address
            Address shippingAddress = getOrCreateShippingAddress(payload, user);

            // Extract tax and shipping fee from frontend
            BigDecimal tax = extractBigDecimal(payload, "tax");
            BigDecimal shippingFee = extractBigDecimal(payload, "shippingFee");
            BigDecimal subtotal = extractBigDecimal(payload, "subtotal");

            // Create the order with tax and shipping
            Order order = orderService.createOrderFromCart(
                    cart,
                    paymentIntentId,
                    shippingAddress,
                    subtotal,
                    tax,
                    shippingFee
            );


            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "orderId", order.getId(),
                    "message", "Commande créée avec succès"
            ));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Erreur: " + e.getMessage()
            ));
        }
    }

    /**
     * Get existing address by ID or create new one from payload
     */
    private Address getOrCreateShippingAddress(Map<String, Object> payload, User user) {
        // Check if using existing address
        Object addressIdObj = payload.get("shippingAddressId");
        if (addressIdObj != null) {
            Long addressId = null;
            if (addressIdObj instanceof Number) {
                addressId = ((Number) addressIdObj).longValue();
            } else if (addressIdObj instanceof String && !((String) addressIdObj).isEmpty()) {
                try {
                    addressId = Long.parseLong((String) addressIdObj);
                } catch (NumberFormatException e) {
                    // Not a valid ID, will create new address
                }
            }

            if (addressId != null) {
                // Use existing address - no duplication!
                return addressService.getAddressById(addressId);
            }
        }

        // Create new address from payload
        return createAndSaveNewAddress(payload, user);
    }

    /**
     * Create and save a new address from payload data
     */
    private Address createAndSaveNewAddress(Map<String, Object> payload, User user) {
        @SuppressWarnings("unchecked")
        Map<String, Object> addressData = (Map<String, Object>) payload.get("shippingAddress");

        if (addressData == null) {
            throw new RuntimeException("Adresse de livraison manquante");
        }

        Address address = Address.builder()
                .user(user)
                .fullName((String) addressData.get("fullName"))
                .phone((String) addressData.get("phone"))
                .addressLine((String) addressData.get("street"))
                .city((String) addressData.get("city"))
                .postalCode((String) addressData.get("postalCode"))
                .country(addressData.get("country") != null ? (String) addressData.get("country") : "Tunisia")
                .label(addressData.get("label") != null ? (String) addressData.get("label") : "Home")
                .isDefault(Boolean.TRUE.equals(addressData.get("isDefault")))
                .build();

        // Save the address once (will also update user phone if needed)
        return addressService.createAddress(user.getId(), address);
    }

    /**
     * Extract BigDecimal value from payload
     */
    private BigDecimal extractBigDecimal(Map<String, Object> payload, String key) {
        Object value = payload.get(key);
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof Number) {
            return BigDecimal.valueOf(((Number) value).doubleValue());
        }
        if (value instanceof String) {
            try {
                return new BigDecimal((String) value);
            } catch (NumberFormatException e) {
                return BigDecimal.ZERO;
            }
        }
        return BigDecimal.ZERO;
    }

    private String getCookie(HttpServletRequest request, String name) {
        if (request.getCookies() == null) return null;
        return Arrays.stream(request.getCookies())
                .filter(c -> name.equals(c.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElse(null);
    }
}