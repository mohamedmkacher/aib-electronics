package com.aib.aib_backend.controller;

import com.aib.aib_backend.dto.request.ProductRequest;
import com.aib.aib_backend.dto.request.ProductSearchRequest;
import com.aib.aib_backend.dto.response.ProductListResponse;
import com.aib.aib_backend.dto.response.ProductResponse;
import com.aib.aib_backend.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:4200")
public class ProductController {

    private final ProductService productService;

    // ============================================
    // ADMIN ENDPOINTS
    // ============================================

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductRequest request) {
        log.info("REST request to create product: {}", request.getName());
        ProductResponse response = productService.createProduct(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request) {
        log.info("REST request to update product: {}", id);
        ProductResponse response = productService.updateProduct(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        log.info("REST request to delete product: {}", id);
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/toggle-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> toggleProductStatus(@PathVariable Long id) {
        log.info("REST request to toggle product status: {}", id);
        ProductResponse response = productService.toggleProductStatus(id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/stock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> updateStock(
            @PathVariable Long id,
            @RequestParam Integer quantity) {
        log.info("REST request to update product stock: {} to {}", id, quantity);
        ProductResponse response = productService.updateStock(id, quantity);
        return ResponseEntity.ok(response);
    }

    // ============================================
    // PUBLIC ENDPOINTS
    // ============================================

    @PostMapping("/search")
    public ResponseEntity<ProductListResponse> searchProducts(
            @RequestBody ProductSearchRequest searchRequest) {
        log.debug("REST request to search products");
        ProductListResponse response = productService.searchProducts(searchRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        log.debug("REST request to get product: {}", id);
        ProductResponse response = productService.getProductById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<ProductResponse> getProductBySlug(@PathVariable String slug) {
        log.debug("REST request to get product by slug: {}", slug);
        ProductResponse response = productService.getProductBySlug(slug);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ProductResponse>> getProductsByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        log.debug("REST request to get products by category: {}", categoryId);
        List<ProductResponse> products = productService.getProductsByCategory(categoryId, page, size);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/featured")
    public ResponseEntity<List<ProductResponse>> getFeaturedProducts(
            @RequestParam(defaultValue = "8") int limit) {
        log.debug("REST request to get featured products");
        List<ProductResponse> products = productService.getFeaturedProducts(limit);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/discounted")
    public ResponseEntity<List<ProductResponse>> getDiscountedProducts(
            @RequestParam(defaultValue = "8") int limit) {
        log.debug("REST request to get discounted products");
        List<ProductResponse> products = productService.getDiscountedProducts(limit);
        return ResponseEntity.ok(products);
    }

    // ============================================
    // UTILITY ENDPOINTS
    // ============================================

    @GetMapping("/brands")
    public ResponseEntity<List<String>> getAllBrands() {
        log.debug("REST request to get all brands");
        List<String> brands = productService.getAllBrands();
        return ResponseEntity.ok(brands);
    }

    @GetMapping("/price-range")
    public ResponseEntity<PriceRangeResponse> getPriceRange(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) Boolean inStock,
            @RequestParam(required = false) Boolean hasDiscount
    ) {
        log.debug("REST request to get price range");
        ProductSearchRequest searchRequest = ProductSearchRequest.builder()
                .keyword(keyword)
                .categoryId(categoryId)
                .brand(brand)
                .inStock(inStock)
                .hasDiscount(hasDiscount)
                .build();
        ProductService.PriceRange priceRange = productService.getPriceRange(searchRequest);
        PriceRangeResponse response = new PriceRangeResponse(
                priceRange.minPrice,
                priceRange.maxPrice
        );
        return ResponseEntity.ok(response);
    }

    // Inner class for price range response
    public record PriceRangeResponse(BigDecimal minPrice, BigDecimal maxPrice) {}
}
