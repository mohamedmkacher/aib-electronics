package com.aib.aib_backend.repository;

import com.aib.aib_backend.model.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {

    @Procedure(procedureName = "sp_get_addresses_by_user")
    List<Address> findByUserId(@Param("p_user_id") Long userId);

    @Procedure(procedureName = "sp_get_addresses_by_user_ordered")
    List<Address> findByUserIdOrderByIsDefaultDescCreatedAtDesc(@Param("p_user_id") Long userId);

    @Procedure(procedureName = "sp_get_default_address_by_user")
    Optional<Address> findByUserIdAndIsDefaultTrue(@Param("p_user_id") Long userId);

    @Procedure(procedureName = "sp_count_addresses_by_user", outputParameterName = "p_count")
    Long countByUserIdRaw(@Param("p_user_id") Long userId);

    default long countByUserId(Long userId) {
        Long result = countByUserIdRaw(userId);
        return result != null ? result : 0L;
    }

    @Procedure(procedureName = "sp_exists_address_by_id_and_user", outputParameterName = "p_exists")
    Boolean existsByIdAndUserIdRaw(@Param("p_id") Long id, @Param("p_user_id") Long userId);

    default boolean existsByIdAndUserId(Long id, Long userId) {
        Boolean result = existsByIdAndUserIdRaw(id, userId);
        return result != null && result;
    }
}
