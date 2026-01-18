package com.aib.aib_backend.service.impl;

import com.aib.aib_backend.dto.response.CartItemResponse;
import com.aib.aib_backend.dto.response.CartResponse;
import com.aib.aib_backend.exception.ResourceNotFoundException;
import com.aib.aib_backend.model.Cart;
import com.aib.aib_backend.model.CartItem;
import com.aib.aib_backend.model.Product;
import com.aib.aib_backend.model.User;
import com.aib.aib_backend.repository.CartRepository;
import com.aib.aib_backend.repository.ProductRepository;
import com.aib.aib_backend.service.CartService;
import com.aib.aib_backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.TreeSet;

@Service
@RequiredArgsConstructor
@Transactional
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserService userService;

    public Cart getActiveCart(String sessionId) {
        User user = userService.getCurrentUser().orElse(null);

        if (user != null) {
            if (user.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_ADMIN"))) {
                return createEmptyTransientCart();
            }
            String userSessionId = "USER_CART_" + user.getId();
            return cartRepository.findBySessionId(userSessionId)
                    .orElseGet(() -> createUserCart(user));
        } else {
            return cartRepository.findBySessionId(sessionId)
                    .orElseGet(() -> createAnonymousCart(sessionId));
        }
    }

    public Cart getAnonymousCart(String sessionId) {
        return cartRepository.findBySessionId(sessionId)
                .orElseGet(() -> createAnonymousCart(sessionId));
    }

    public Cart getUserCart(User user) {
        String userSessionId = "USER_CART_" + user.getId();
        return cartRepository.findBySessionId(userSessionId)
                .orElseGet(() -> createUserCart(user));
    }

    public boolean hasUserCart(User user) {
        String userSessionId = "USER_CART_" + user.getId();
        Optional<Cart> userCart = cartRepository.findBySessionId(userSessionId);
        return userCart.isPresent() && !userCart.get().getItems().isEmpty();
    }

    public Cart mergeCarts(String anonymousSessionId) {
        User user = userService.getCurrentUser()
                .orElseThrow(() -> new IllegalStateException("User must be logged in to merge carts"));

        Optional<Cart> anonCartOpt = cartRepository.findBySessionId(anonymousSessionId);
        if (anonCartOpt.isEmpty() || anonCartOpt.get().getItems().isEmpty()) {
            return getActiveCart(anonymousSessionId); // Return user's cart
        }

        Cart anonCart = anonCartOpt.get();
        String userSessionId = "USER_CART_" + user.getId();
        Cart userCart = cartRepository.findBySessionId(userSessionId)
                .orElseGet(() -> createUserCart(user));

        mergeCartItems(anonCart, userCart);
        cartRepository.delete(anonCart);
        cartRepository.flush();

        userCart.setExpiresAt(LocalDateTime.now().plusDays(30));
        return cartRepository.save(userCart);
    }

    public Cart replaceUserCart(String anonymousSessionId) {
        User user = userService.getCurrentUser()
                .orElseThrow(() -> new IllegalStateException("User must be logged in to replace cart"));

        Optional<Cart> anonCartOpt = cartRepository.findBySessionId(anonymousSessionId);
        if (anonCartOpt.isEmpty() || anonCartOpt.get().getItems().isEmpty()) {
            return getActiveCart(anonymousSessionId);
        }

        Cart anonCart = anonCartOpt.get();
        String userSessionId = "USER_CART_" + user.getId();
        
        // Delete existing user cart
        cartRepository.findBySessionId(userSessionId).ifPresent(cartRepository::delete);
        cartRepository.flush();

        // Convert anonymous cart to user cart
        Cart userCart = createUserCart(user);
        for (CartItem anonItem : anonCart.getItems()) {
            CartItem newItem = new CartItem();
            newItem.setCart(userCart);
            newItem.setProduct(anonItem.getProduct());
            newItem.setQuantity(anonItem.getQuantity());
            userCart.getItems().add(newItem);
        }
        
        cartRepository.delete(anonCart);
        return cartRepository.save(userCart);
    }

    public void keepUserCart(String anonymousSessionId) {
        cartRepository.findBySessionId(anonymousSessionId).ifPresent(cartRepository::delete);
    }

    private Cart createEmptyTransientCart() {
        Cart cart = new Cart();
        cart.setId(-1L); // Dummy ID
        cart.setItems(new TreeSet<>());
        return cart;
    }

    private void mergeCartItems(Cart sourceCart, Cart targetCart) {
        for (CartItem sourceItem : sourceCart.getItems()) {
            Optional<CartItem> existingItem = targetCart.getItems().stream()
                    .filter(item -> item.getProduct().getId().equals(sourceItem.getProduct().getId()))
                    .findFirst();

            if (existingItem.isPresent()) {
                existingItem.get().setQuantity(existingItem.get().getQuantity() + sourceItem.getQuantity());
            } else {
                CartItem newItem = new CartItem();
                newItem.setCart(targetCart);
                newItem.setProduct(sourceItem.getProduct());
                newItem.setQuantity(sourceItem.getQuantity());
                targetCart.getItems().add(newItem);
            }
        }
    }

    public CartResponse addItem(String sessionId, Long productId, int qty) {
        Cart cart = getActiveCart(sessionId);
        
        if (cart.getId() != null && cart.getId() == -1L) {
            return toResponse(cart);
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (!product.getActive()) {
            throw new IllegalStateException("Product is not available");
        }

        if (product.getStockQuantity() < qty) {
            throw new IllegalStateException("Insufficient stock. Only " + product.getStockQuantity() + " available.");
        }

        cart.getItems().stream()
                .filter(i -> i.getProduct().getId().equals(productId))
                .findFirst()
                .ifPresentOrElse(
                        item -> {
                            int newQuantity = item.getQuantity() + qty;
                            if (product.getStockQuantity() < newQuantity) {
                                throw new IllegalStateException("Insufficient stock. Only " + product.getStockQuantity() + " available.");
                            }
                            item.setQuantity(newQuantity);
                        },
                        () -> {
                            CartItem item = new CartItem();
                            item.setCart(cart);
                            item.setProduct(product);
                            item.setQuantity(qty);
                            cart.getItems().add(item);
                        }
                );

        cart.setExpiresAt(LocalDateTime.now().plusDays(7));
        cartRepository.save(cart);
        return toResponse(cart);
    }

    public CartResponse getCart(String sessionId) {
        return toResponse(getActiveCart(sessionId));
    }

    public CartResponse updateQuantity(String sessionId, Long productId, int quantity) {
        Cart cart = getActiveCart(sessionId);
        
        if (cart.getId() != null && cart.getId() == -1L) {
            return toResponse(cart);
        }

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getProduct().getId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Item not found in cart"));

        Product product = item.getProduct();
        if (product.getStockQuantity() < quantity) {
            throw new IllegalStateException("Insufficient stock. Only " + product.getStockQuantity() + " available.");
        }

        if (quantity <= 0) {
            cart.getItems().remove(item);
        } else {
            item.setQuantity(quantity);
        }

        cart.setExpiresAt(LocalDateTime.now().plusDays(7));
        cartRepository.save(cart);
        return toResponse(cart);
    }

    public CartResponse removeItem(String sessionId, Long productId) {
        Cart cart = getActiveCart(sessionId);
        
        if (cart.getId() != null && cart.getId() == -1L) {
            return toResponse(cart);
        }

        CartItem itemToRemove = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Item not found in cart"));
        cart.getItems().remove(itemToRemove);
        cart.setExpiresAt(LocalDateTime.now().plusDays(7));
        cartRepository.save(cart);
        return toResponse(cart);
    }

    public void clearCart(String sessionId) {
        Cart cart = getActiveCart(sessionId);
        
        if (cart.getId() != null && cart.getId() == -1L) {
            return;
        }

        cart.getItems().clear();
        cartRepository.save(cart);
    }

    public void clearCart(Cart cart) {
        if (cart.getId() != null && cart.getId() != -1L) {
            cartRepository.delete(cart);
        }
    }

    public CartResponse toResponse(Cart cart) {
        List<CartItemResponse> items = cart.getItems().stream()
                .map(item -> {
                    Product product = item.getProduct();
                    BigDecimal unitPrice = product.getCurrentPrice();
                    return new CartItemResponse(
                            product.getId(),
                            product.getName(),
                            product.getSlug(),
                            product.getMainImageUrl(),
                            product.getPrice(),
                            product.getDiscount(),
                            unitPrice,
                            item.getQuantity(),
                            unitPrice.multiply(BigDecimal.valueOf(item.getQuantity())),
                            product.getStockQuantity(),
                            product.getActive()
                    );
                })
                .toList();

        BigDecimal total = items.stream()
                .map(CartItemResponse::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal subtotal = items.stream()
                .map(item -> item.price().multiply(BigDecimal.valueOf(item.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartResponse(cart.getId(), items.size(), subtotal, total, items);
    }

    public void validateCartForCheckout(Cart cart) {
        for (CartItem item : cart.getItems()) {
            Product product = item.getProduct();
            if (!product.getActive()) {
                throw new IllegalStateException("Product '" + product.getName() + "' is no longer available.");
            }
            if (product.getStockQuantity() < item.getQuantity()) {
                throw new IllegalStateException("Insufficient stock for '" + product.getName() + "'. Only " + product.getStockQuantity() + " left.");
            }
        }
    }

    private Cart createUserCart(User user) {
        Cart cart = Cart.builder()
                .user(user)
                .sessionId("USER_CART_" + user.getId())
                .expiresAt(LocalDateTime.now().plusDays(30))
                .build();
        return cartRepository.save(cart);
    }

    private Cart createAnonymousCart(String sessionId) {
        Cart cart = Cart.builder()
                .sessionId(sessionId)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();
        return cartRepository.save(cart);
    }
}
