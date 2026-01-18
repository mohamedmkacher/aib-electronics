package com.aib.aib_backend.repository;

import com.aib.aib_backend.model.Cart;
import com.aib.aib_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    @Procedure(procedureName = "sp_get_cart_by_session")
    Optional<Cart> findBySessionId(@Param("p_session_id") String sessionId);

    @Procedure(procedureName = "sp_get_cart_by_user_no_session")
    Optional<Cart> findByUserIdAndSessionIdIsNull(@Param("p_user_id") Long userId);

    default Optional<Cart> findByUserAndSessionIdIsNull(User user) {
        if (user == null || user.getId() == null) {
            return Optional.empty();
        }
        return findByUserIdAndSessionIdIsNull(user.getId());
    }

    @Procedure(procedureName = "sp_get_carts_by_product")
    List<Cart> findCartsByProductId(@Param("p_product_id") Long productId);
}
