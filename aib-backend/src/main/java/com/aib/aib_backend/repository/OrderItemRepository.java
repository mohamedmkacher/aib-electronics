package com.aib.aib_backend.repository;

import com.aib.aib_backend.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    @Procedure(procedureName = "sp_exists_order_item_by_product", outputParameterName = "p_exists")
    Boolean existsByProductIdRaw(@Param("p_product_id") Long productId);

    default boolean existsByProductId(Long productId) {
        Boolean result = existsByProductIdRaw(productId);
        return result != null && result;
    }
}
