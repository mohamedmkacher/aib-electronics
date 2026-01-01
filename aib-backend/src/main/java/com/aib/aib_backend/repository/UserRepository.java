package com.aib.aib_backend.repository;

import com.aib.aib_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    @Procedure(procedureName = "sp_exists_user_by_email", outputParameterName = "p_exists")
    Boolean existsByEmailRaw(@Param("p_email") String email);

    default boolean existsByEmail(String email) {
        Boolean result = existsByEmailRaw(email);
        return result != null && result;
    }

    @Procedure(procedureName = "sp_exists_user_by_phone", outputParameterName = "p_exists")
    Boolean existsByPhoneRaw(@Param("p_phone") String phone);

    default boolean existsByPhone(String phone) {
        Boolean result = existsByPhoneRaw(phone);
        return result != null && result;
    }

    @Procedure(procedureName = "sp_exists_user_by_phone_and_id_not", outputParameterName = "p_exists")
    Boolean existsByPhoneAndIdNotRaw(@Param("p_phone") String phone, @Param("p_id") Long id);

    default boolean existsByPhoneAndIdNot(String phone, Long id) {
        Boolean result = existsByPhoneAndIdNotRaw(phone, id);
        return result != null && result;
    }

    @Procedure(procedureName = "sp_get_user_by_provider")
    Optional<User> findByProviderAndProviderId(@Param("p_provider") String provider, @Param("p_provider_id") String providerId);

    @Procedure(procedureName = "sp_get_users_by_role_name")
    List<User> findByRoleName(@Param("p_role_name") String roleName);

    @Procedure(procedureName = "sp_count_users_after_date", outputParameterName = "p_count")
    Long countByCreatedAtAfterRaw(@Param("p_date") LocalDateTime date);

    default long countByCreatedAtAfter(LocalDateTime date) {
        Long result = countByCreatedAtAfterRaw(date);
        return result != null ? result : 0L;
    }

    @Procedure(procedureName = "sp_count_non_admin_users", outputParameterName = "p_count")
    Long countNonAdminUsersRaw();

    default long countNonAdminUsers() {
        Long result = countNonAdminUsersRaw();
        return result != null ? result : 0L;
    }

    @Procedure(procedureName = "sp_count_non_admin_users_after", outputParameterName = "p_count")
    Long countNonAdminUsersByCreatedAtAfterRaw(@Param("p_date") LocalDateTime date);

    default long countNonAdminUsersByCreatedAtAfter(LocalDateTime date) {
        Long result = countNonAdminUsersByCreatedAtAfterRaw(date);
        return result != null ? result : 0L;
    }
}
