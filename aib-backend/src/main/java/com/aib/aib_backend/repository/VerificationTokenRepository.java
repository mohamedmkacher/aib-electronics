package com.aib.aib_backend.repository;

import com.aib.aib_backend.model.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {

    @Procedure(procedureName = "sp_get_verification_token_by_token")
    Optional<VerificationToken> findByToken(@Param("p_token") String token);

    @Procedure(procedureName = "sp_get_verification_token_by_user")
    Optional<VerificationToken> findByUserId(@Param("p_user_id") Long userId);

    @Transactional
    @Procedure(procedureName = "sp_delete_verification_token_by_user", outputParameterName = "p_deleted_count")
    Integer deleteByUserIdRaw(@Param("p_user_id") Long userId);

    default void deleteByUserId(Long userId) {
        deleteByUserIdRaw(userId);
    }
}
