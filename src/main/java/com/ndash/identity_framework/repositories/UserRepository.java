package com.ndash.identity_framework.repositories;

import com.ndash.identity_framework.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByAzureId(String azureId);
    Optional<User> findByEmail(String email);
    List<User> findByActiveTrue();
    Page<User> findByUsernameContainingIgnoreCaseAndActiveTrue(String username, Pageable pageable);
    List<User> findByDepartmentIdAndActiveTrue(Long departmentId);
    List<User> findByDepartmentIdIn(List<Long> departmentIds);
    List<User> findByManagerId(Long managerId);


    @EntityGraph(attributePaths = {"userRoles", "userRoles.role"})
    List<User> findDistinctByUserRolesRoleNameIgnoreCase(String roleName);

    /**
     * Convenience method for managers only
     */
    default List<User> findAllManagers() {
        return findDistinctByUserRolesRoleNameIgnoreCase("manager");
    }


}
