package com.aib.aib_backend.repository;

import com.aib.aib_backend.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    @Procedure(procedureName = "sp_get_role_by_name")
    Optional<Role> findByName(@Param("p_name") String name);

    @Procedure(procedureName = "sp_exists_role_by_name", outputParameterName = "p_exists")
    Boolean existsByNameRaw(@Param("p_name") String name);

    default boolean existsByName(String name) {
        Boolean result = existsByNameRaw(name);
        return result != null && result;
    }
}
