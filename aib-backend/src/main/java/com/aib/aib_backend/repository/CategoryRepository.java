package com.aib.aib_backend.repository;

import com.aib.aib_backend.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    @Procedure(procedureName = "sp_get_all_categories")
    List<Category> findAllCategories();

    @Procedure(procedureName = "sp_get_active_categories")
    List<Category> findActiveCategories();

    @Procedure(procedureName = "sp_get_category_by_id")
    Optional<Category> findCategoryById(@Param("p_id") Long id);

    @Procedure(procedureName = "sp_get_category_by_slug")
    Optional<Category> findBySlug(@Param("p_slug") String slug);

    @Procedure(procedureName = "sp_count_products_by_category", outputParameterName = "p_count")
    Long countProductsByCategoryRaw(@Param("p_category_id") Long categoryId);

    default Long countProductsByCategory(Long categoryId) {
        Long result = countProductsByCategoryRaw(categoryId);
        return result != null ? result : 0L;
    }

    @Procedure(procedureName = "sp_exists_category_by_name", outputParameterName = "p_exists")
    Boolean existsByNameRaw(@Param("p_name") String name);

    default boolean existsByName(String name) {
        Boolean result = existsByNameRaw(name);
        return result != null && result;
    }

    @Procedure(procedureName = "sp_exists_category_by_name_and_id_not", outputParameterName = "p_exists")
    Boolean existsByNameAndIdNotRaw(@Param("p_name") String name, @Param("p_id") Long id);

    default boolean existsByNameAndIdNot(String name, Long id) {
        Boolean result = existsByNameAndIdNotRaw(name, id);
        return result != null && result;
    }
}
