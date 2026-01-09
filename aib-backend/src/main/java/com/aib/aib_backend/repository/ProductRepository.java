package com.aib.aib_backend.repository;

import com.aib.aib_backend.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Procedure(procedureName = "sp_get_product_by_id")
    Optional<Product> findProductById(@Param("p_id") Long id, @Param("p_active_only") Boolean activeOnly);

    @Procedure(procedureName = "sp_get_product_by_slug")
    Optional<Product> findProductBySlug(@Param("p_slug") String slug, @Param("p_active_only") Boolean activeOnly);

    default Optional<Product> findBySlug(String slug) {
        return findProductBySlug(slug, false);
    }

    @Procedure(procedureName = "sp_search_products")
    List<Product> searchProducts(
            @Param("p_keyword") String keyword,
            @Param("p_category_id") Long categoryId,
            @Param("p_brand") String brand,
            @Param("p_min_price") BigDecimal minPrice,
            @Param("p_max_price") BigDecimal maxPrice,
            @Param("p_in_stock") Boolean inStock,
            @Param("p_has_discount") Boolean hasDiscount,
            @Param("p_is_featured") Boolean isFeatured,
            @Param("p_active_only") Boolean activeOnly,
            @Param("p_stock_status") String stockStatus,
            @Param("p_sort_by") String sortBy,
            @Param("p_sort_direction") String sortDirection,
            @Param("p_limit") Integer limit,
            @Param("p_offset") Integer offset
    );

    @Procedure(procedureName = "sp_count_search_products")
    Long countSearchProducts(
            @Param("p_keyword") String keyword,
            @Param("p_category_id") Long categoryId,
            @Param("p_brand") String brand,
            @Param("p_min_price") BigDecimal minPrice,
            @Param("p_max_price") BigDecimal maxPrice,
            @Param("p_in_stock") Boolean inStock,
            @Param("p_has_discount") Boolean hasDiscount,
            @Param("p_is_featured") Boolean isFeatured,
            @Param("p_active_only") Boolean activeOnly,
            @Param("p_stock_status") String stockStatus
    );

    @Procedure(procedureName = "sp_get_products_by_category")
    List<Product> findProductsByCategory(
            @Param("p_category_id") Long categoryId,
            @Param("p_active_only") Boolean activeOnly,
            @Param("p_limit") Integer limit,
            @Param("p_offset") Integer offset
    );

    @Procedure(procedureName = "sp_get_featured_products")
    List<Product> findFeaturedProducts(@Param("p_limit") Integer limit);

    @Procedure(procedureName = "sp_get_discounted_products")
    List<Product> findDiscountedProducts(@Param("p_limit") Integer limit);

    @Procedure(procedureName = "sp_get_all_brands")
    List<String> findAllBrands();

    @Procedure(procedureName = "sp_get_price_range")
    List<Object[]> getPriceRangeRaw(
            @Param("p_keyword") String keyword,
            @Param("p_category_id") Long categoryId,
            @Param("p_brand") String brand,
            @Param("p_in_stock") Boolean inStock,
            @Param("p_has_discount") Boolean hasDiscount
    );

    @Procedure(procedureName = "sp_exists_product_by_sku", outputParameterName = "p_exists")
    Boolean existsBySkuRaw(@Param("p_sku") String sku);

    default boolean existsBySku(String sku) {
        Boolean result = existsBySkuRaw(sku);
        return result != null && result;
    }

    @Procedure(procedureName = "sp_exists_product_by_sku_and_id_not", outputParameterName = "p_exists")
    Boolean existsBySkuAndIdNotRaw(@Param("p_sku") String sku, @Param("p_id") Long id);

    default boolean existsBySkuAndIdNot(String sku, Long id) {
        Boolean result = existsBySkuAndIdNotRaw(sku, id);
        return result != null && result;
    }

    @Procedure(procedureName = "sp_exists_product_by_name", outputParameterName = "p_exists")
    Boolean existsByNameRaw(@Param("p_name") String name);

    default boolean existsByName(String name) {
        Boolean result = existsByNameRaw(name);
        return result != null && result;
    }

    @Procedure(procedureName = "sp_exists_product_by_name_and_id_not", outputParameterName = "p_exists")
    Boolean existsByNameAndIdNotRaw(@Param("p_name") String name, @Param("p_id") Long id);

    default boolean existsByNameAndIdNot(String name, Long id) {
        Boolean result = existsByNameAndIdNotRaw(name, id);
        return result != null && result;
    }

    @Procedure(procedureName = "sp_count_products_low_stock", outputParameterName = "p_count")
    Long countByStockQuantityLessThanRaw(@Param("p_threshold") Integer threshold);

    default long countByStockQuantityLessThan(int threshold) {
        Long result = countByStockQuantityLessThanRaw(threshold);
        return result != null ? result : 0L;
    }

    @Procedure(procedureName = "sp_get_products_low_stock")
    List<Product> findByStockQuantityLessThanOrderByStockQuantityAsc(@Param("p_threshold") Integer threshold);
}