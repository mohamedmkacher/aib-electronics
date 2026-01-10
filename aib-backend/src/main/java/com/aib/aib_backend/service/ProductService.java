package com.aib.aib_backend.service;

import com.aib.aib_backend.dto.request.ProductRequest;
import com.aib.aib_backend.dto.request.ProductSearchRequest;
import com.aib.aib_backend.dto.response.ProductListResponse;
import com.aib.aib_backend.dto.response.ProductResponse;
import java.math.BigDecimal;
import java.util.List;

/**
 * Service interface for Product operations
 */
public interface ProductService {

    // ============================================
    // ADMIN OPERATIONS
    // ============================================

    /**
     * Create a new product
     * @param request Product details
     * @return Created product
     */
    ProductResponse createProduct(ProductRequest request);

    /**
     * Update existing product
     * @param id Product ID
     * @param request Updated product details
     * @return Updated product
     */
    ProductResponse updateProduct(Long id, ProductRequest request);

    /**
     * Delete product
     * @param id Product ID
     */
    void deleteProduct(Long id);

    /**
     * Toggle product active/inactive status
     * @param id Product ID
     * @return Updated product
     */
    ProductResponse toggleProductStatus(Long id);

    /**
     * Update product stock quantity
     * @param id Product ID
     * @param quantity New stock quantity
     * @return Updated product
     */
    ProductResponse updateStock(Long id, Integer quantity);

    // ============================================
    // PUBLIC OPERATIONS
    // ============================================

    /**
     * Search products with advanced filtering
     * @param searchRequest Search and filter parameters
     * @return Paginated product list with filter summary
     */
    ProductListResponse searchProducts(ProductSearchRequest searchRequest);

    /**
     * Get product by ID
     * @param id Product ID
     * @return Product details
     */
    ProductResponse getProductById(Long id);

    /**
     * Get product by slug
     * @param slug Product slug
     * @return Product details
     */
    ProductResponse getProductBySlug(String slug);

    /**
     * Get products by category with pagination
     * @param categoryId Category ID
     * @param page Page number
     * @param size Page size
     * @return List of products
     */
    List<ProductResponse> getProductsByCategory(Long categoryId, int page, int size);

    /**
     * Get featured products
     * @param limit Maximum number of products to return
     * @return List of featured products
     */
    List<ProductResponse> getFeaturedProducts(int limit);

    /**
     * Get discounted products
     * @param limit Maximum number of products to return
     * @return List of discounted products
     */
    List<ProductResponse> getDiscountedProducts(int limit);

    // ============================================
    // UTILITY METHODS
    // ============================================

    /**
     * Get all distinct brands
     * @return List of brand names
     */
    List<String> getAllBrands();




    /**
     * Get price range (min and max prices)
     * @return Price range
     */
    PriceRange getPriceRange(ProductSearchRequest searchRequest);
    /**
     * Price range wrapper class
     */
    class PriceRange {
        public BigDecimal minPrice;
        public BigDecimal maxPrice;

        public PriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
            this.minPrice = minPrice;
            this.maxPrice = maxPrice;
        }
    }
}
