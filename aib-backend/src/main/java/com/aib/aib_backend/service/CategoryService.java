package com.aib.aib_backend.service;

import com.aib.aib_backend.dto.request.CategoryRequest;
import com.aib.aib_backend.dto.response.CategoryResponse;
import java.util.List;

/**
 * Service interface for Category operations
 */
public interface CategoryService {

    /**
     * Create a new category
     * @param request Category details
     * @return Created category
     */
    CategoryResponse createCategory(CategoryRequest request);

    /**
     * Update existing category
     * @param id Category ID
     * @param request Updated category details
     * @return Updated category
     */
    CategoryResponse updateCategory(Long id, CategoryRequest request);

    /**
     * Delete category (only if no products exist)
     * @param id Category ID
     */
    void deleteCategory(Long id);

    /**
     * Get category by ID
     * @param id Category ID
     * @return Category details with product count
     */
    CategoryResponse getCategoryById(Long id);

    /**
     * Get category by slug
     * @param slug Category slug
     * @return Category details with product count
     */
    CategoryResponse getCategoryBySlug(String slug);

    /**
     * Get all categories (admin view)
     * @return List of all categories with product counts
     */
    List<CategoryResponse> getAllCategories();

    /**
     * Get only active categories (public view)
     * @return List of active categories with product counts
     */
    List<CategoryResponse> getActiveCategories();
    CategoryResponse toggleCategoryStatus(Long id);
    int reactivateAllProductsInCategory(Long categoryId);
}
