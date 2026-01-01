package com.aib.aib_backend.repository;

import com.aib.aib_backend.model.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    @Procedure(procedureName = "sp_get_password_reset_token_by_token")
    Optional<PasswordResetToken> findByToken(@Param("p_token") String token);

    @Procedure(procedureName = "sp_get_password_reset_token_by_user")
    Optional<PasswordResetToken> findByUserId(@Param("p_user_id") Long userId);

    @Transactional
    @Procedure(procedureName = "sp_delete_expired_password_reset_tokens", outputParameterName = "p_deleted_count")
    Integer deleteByExpiryDateBeforeRaw(@Param("p_date") LocalDateTime date);

    default void deleteByExpiryDateBefore(LocalDateTime date) {
        deleteByExpiryDateBeforeRaw(date);
    }
}
