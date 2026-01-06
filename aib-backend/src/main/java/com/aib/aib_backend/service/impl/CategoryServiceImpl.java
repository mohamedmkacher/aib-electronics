package com.aib.aib_backend.service.impl;

import com.aib.aib_backend.dto.request.CategoryRequest;
import com.aib.aib_backend.dto.response.CategoryResponse;
import com.aib.aib_backend.exception.ResourceNotFoundException;
import com.aib.aib_backend.exception.DuplicateResourceException;
import com.aib.aib_backend.model.Category;
import com.aib.aib_backend.repository.CategoryRepository;
import com.aib.aib_backend.repository.ProductRepository;
import com.aib.aib_backend.service.CategoryService;
import com.aib.aib_backend.util.SlugUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final JdbcTemplate jdbcTemplate;  // NEW: For executing cascade update

    @Override
    public CategoryResponse createCategory(CategoryRequest request) {
        log.info("Creating new category: {}", request.getName());

        if (categoryRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Category with name '" + request.getName() + "' already exists");
        }

        String slug = SlugUtil.generateSlug(request.getName());

        Category category = Category.builder()
                .name(request.getName())
                .slug(slug)
                .description(request.getDescription())
                .imageUrl(request.getImageUrl())
                .active(request.getActive() != null ? request.getActive() : true)
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .build();

        try {
            Category savedCategory = categoryRepository.save(category);
            log.info("Category created successfully with ID: {}", savedCategory.getId());
            return mapToResponse(savedCategory);
        } catch (DataIntegrityViolationException e) {
            log.error("Error creating category: {}", e.getMessage());
            throw new DuplicateResourceException("Category with this name or slug already exists");
        }
    }

    @Override
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        log.info("Updating category with ID: {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));

        // Store old active status to detect changes
        Boolean oldActiveStatus = category.getActive();

        if (!category.getName().equals(request.getName()) &&
                categoryRepository.existsByNameAndIdNot(request.getName(), id)) {
            throw new DuplicateResourceException("Category with name '" + request.getName() + "' already exists");
        }

        // Update fields
        category.setName(request.getName());
        category.setSlug(SlugUtil.generateSlug(request.getName()));
        category.setDescription(request.getDescription());
        category.setImageUrl(request.getImageUrl());

        if (request.getActive() != null) {
            category.setActive(request.getActive());
        }

        if (request.getDisplayOrder() != null) {
            category.setDisplayOrder(request.getDisplayOrder());
        }

        try {
            Category updatedCategory = categoryRepository.save(category);

            // NEW: Cascade deactivation to products if category status changed
            if (oldActiveStatus != updatedCategory.getActive()) {
                cascadeStatusToProducts(updatedCategory.getId(), updatedCategory.getActive(), oldActiveStatus);
            }

            log.info("Category updated successfully: {}", updatedCategory.getId());
            return mapToResponse(updatedCategory);
        } catch (DataIntegrityViolationException e) {
            log.error("Error updating category: {}", e.getMessage());
            throw new DuplicateResourceException("Category with this name or slug already exists");
        }
    }

    /**
     * NEW METHOD: Cascade category status changes to products
     * When category is deactivated, deactivate all its products
     * When category is reactivated, products remain inactive (manual control)
     */
    private void cascadeStatusToProducts(Long categoryId, Boolean newStatus, Boolean oldStatus) {
        if (oldStatus && !newStatus) {
            // Category was deactivated (true -> false)
            int deactivatedCount = jdbcTemplate.update(
                    "UPDATE products SET active = 0, updated_at = CURRENT_TIMESTAMP " +
                            "WHERE category_id = ? AND active = 1",
                    categoryId
            );
            log.info("Deactivated {} products in category {}", deactivatedCount, categoryId);
        }
        // Note: We do NOT automatically reactivate products when category is reactivated
        // This is intentional - admin must manually choose which products to reactivate
    }

    /**
     * NEW METHOD: Toggle category status with cascade
     */
    public CategoryResponse toggleCategoryStatus(Long id) {
        log.info("Toggling status for category ID: {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));

        Boolean oldStatus = category.getActive();
        Boolean newStatus = !oldStatus;

        category.setActive(newStatus);
        Category updatedCategory = categoryRepository.save(category);

        // Cascade to products
        cascadeStatusToProducts(id, newStatus, oldStatus);

        log.info("Category status toggled to: {}", updatedCategory.getActive());
        return mapToResponse(updatedCategory);
    }

    /**
     * NEW METHOD: Bulk reactivate products in a category
     * Useful after reactivating a category
     */
    public int reactivateAllProductsInCategory(Long categoryId) {
        log.info("Reactivating all products in category: {}", categoryId);

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + categoryId));

        if (!category.getActive()) {
            throw new IllegalStateException("Cannot reactivate products in an inactive category");
        }

        int reactivatedCount = jdbcTemplate.update(
                "UPDATE products SET active = 1, updated_at = CURRENT_TIMESTAMP " +
                        "WHERE category_id = ? AND active = 0",
                categoryId
        );

        log.info("Reactivated {} products in category {}", reactivatedCount, categoryId);
        return reactivatedCount;
    }

    @Override
    public void deleteCategory(Long id) {
        log.info("Deleting category with ID: {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));

        Long productCount = categoryRepository.countProductsByCategory(id);
        if (productCount > 0) {
            throw new IllegalStateException(
                    "Cannot delete category '" + category.getName() + "' because it has " + productCount + " product(s). " +
                            "Please delete or reassign the products first."
            );
        }

        categoryRepository.delete(category);
        log.info("Category deleted successfully: {}", id);
    }

    @Override
    @Transactional(readOnly = false)
    public CategoryResponse getCategoryById(Long id) {
        log.debug("Fetching category by ID: {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));

        CategoryResponse response = mapToResponse(category);
        Long productCount = categoryRepository.countProductsByCategory(id);
        response.setProductCount(productCount.intValue());

        return response;
    }

    @Override
    @Transactional(readOnly = false)
    public CategoryResponse getCategoryBySlug(String slug) {
        log.debug("Fetching category by slug: {}", slug);

        Category category = categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with slug: " + slug));

        CategoryResponse response = mapToResponse(category);
        Long productCount = categoryRepository.countProductsByCategory(category.getId());
        response.setProductCount(productCount.intValue());

        return response;
    }

    @Override
    @Transactional(readOnly = false)
    public List<CategoryResponse> getAllCategories() {
        log.debug("Fetching all categories");

        List<Category> categories = categoryRepository.findAllCategories();

        return categories.stream()
                .map(category -> {
                    CategoryResponse response = mapToResponse(category);
                    Long productCount = categoryRepository.countProductsByCategory(category.getId());
                    response.setProductCount(productCount.intValue());
                    return response;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = false)
    public List<CategoryResponse> getActiveCategories() {
        log.debug("Fetching active categories");

        List<Category> categories = categoryRepository.findActiveCategories();

        return categories.stream()
                .map(category -> {
                    CategoryResponse response = mapToResponse(category);
                    Long productCount = categoryRepository.countProductsByCategory(category.getId());
                    response.setProductCount(productCount.intValue());
                    return response;
                })
                .collect(Collectors.toList());
    }

    private CategoryResponse mapToResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .description(category.getDescription())
                .imageUrl(category.getImageUrl())
                .active(category.getActive())
                .displayOrder(category.getDisplayOrder())
                .createdAt(category.getCreatedAt())
                .productCount(0)
                .build();
    }
}