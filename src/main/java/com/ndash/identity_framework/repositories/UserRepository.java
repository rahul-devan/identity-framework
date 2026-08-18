package com.ndash.identity_framework.repositories;

import com.ndash.identity_framework.domain.User;
import com.ndash.identity_framework.dto.SimpleUserDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    @EntityGraph(attributePaths = {"userRoles", "userRoles.role"})
    Optional<User> findByAzureId(String azureId);
    Optional<User> findByEmail(String email);
    List<User> findByActiveTrue();

    List<User> findByActiveFalse();

    @EntityGraph(attributePaths = {"userRoles", "userRoles.role", "blueprint", "company"})
    @Query("SELECT u FROM User u ORDER BY u.active DESC, u.firstName ASC, u.lastName ASC")
    List<User> findAllWithDetails();

    @EntityGraph(attributePaths = {"userRoles", "userRoles.role", "blueprint", "company"})
    @Query("SELECT u FROM User u WHERE u.active = true")
    List<User> findAllActiveWithDetails();

    @EntityGraph(attributePaths = {"userRoles", "userRoles.role", "blueprint", "company"})
    @Query("SELECT u FROM User u WHERE u.active = false")
    List<User> findAllInactiveWithDetails();

    @Query("""
            SELECT u.manager.id AS managerId, u.id AS id, u.firstName AS firstName, u.lastName AS lastName,
                   u.email AS email, COALESCE(c.name, :defaultCompanyName) AS companyName, u.active AS active,
                   TRIM(CONCAT(COALESCE(m.firstName, ''), ' ', COALESCE(m.lastName, ''))) AS managerName
            FROM User u LEFT JOIN u.company c JOIN u.manager m
            WHERE u.manager.id IS NOT NULL AND u.active = true
            """)
    List<SubordinateProjection> findAllActiveSubordinateRows(
            @Param("defaultCompanyName") String defaultCompanyName);

    @EntityGraph(attributePaths = {"userRoles", "userRoles.role", "blueprint", "company", "manager"})
    Page<User> findByUsernameContainingIgnoreCaseAndActiveTrue(String username, Pageable pageable);
    List<User> findByDepartmentIdAndActiveTrue(Long departmentId);
    List<User> findByDepartmentIdIn(List<Long> departmentIds);
    List<User> findByManagerId(Long managerId);

    @EntityGraph(attributePaths = {"userRoles", "userRoles.role", "blueprint", "company", "manager"})
    @Query("""
            SELECT DISTINCT u FROM User u
            LEFT JOIN FETCH u.manager
            LEFT JOIN FETCH u.company
            LEFT JOIN FETCH u.blueprint
            LEFT JOIN FETCH u.userRoles ur
            LEFT JOIN FETCH ur.role
            WHERE u.id = :id
            """)
    Optional<User> findWithDetailsById(@Param("id") Long id);

    @Query("""
            SELECT new com.ndash.identity_framework.dto.SimpleUserDto(
                u.id, u.firstName, u.lastName, u.email, COALESCE(c.name, :defaultCompanyName), u.active,
                m.id, TRIM(CONCAT(COALESCE(m.firstName, ''), ' ', COALESCE(m.lastName, ''))))
            FROM User u LEFT JOIN u.company c JOIN u.manager m
            WHERE u.manager.id = :managerId
            """)
    List<SimpleUserDto> findSubordinateProjectionsByManagerId(
            @Param("managerId") Long managerId,
            @Param("defaultCompanyName") String defaultCompanyName);

    @Query("""
            SELECT new com.ndash.identity_framework.dto.SimpleUserDto(
                u.id, u.firstName, u.lastName, u.email, COALESCE(c.name, :defaultCompanyName), u.active,
                m.id, TRIM(CONCAT(COALESCE(m.firstName, ''), ' ', COALESCE(m.lastName, ''))))
            FROM User u LEFT JOIN u.company c JOIN u.manager m
            WHERE u.manager.id = :managerId AND u.active = true
            """)
    List<SimpleUserDto> findActiveSubordinateProjectionsByManagerId(
            @Param("managerId") Long managerId,
            @Param("defaultCompanyName") String defaultCompanyName);

    @Query("""
            SELECT new com.ndash.identity_framework.dto.SimpleUserDto(
                u.id, u.firstName, u.lastName, u.email, COALESCE(c.name, :defaultCompanyName), u.active,
                m.id, TRIM(CONCAT(COALESCE(m.firstName, ''), ' ', COALESCE(m.lastName, ''))))
            FROM User u LEFT JOIN u.company c JOIN u.manager m
            WHERE u.manager.id = :managerId AND u.active = false
            """)
    List<SimpleUserDto> findInactiveSubordinateProjectionsByManagerId(
            @Param("managerId") Long managerId,
            @Param("defaultCompanyName") String defaultCompanyName);

    @EntityGraph(attributePaths = {"userRoles", "userRoles.role"})
    List<User> findDistinctByUserRolesRoleNameIgnoreCase(String roleName);

    /**
     * Convenience method for managers only
     */
    default List<User> findAllManagers() {
        return findDistinctByUserRolesRoleNameIgnoreCase("manager");
    }

    List<User> findByCompanyId(Long companyId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE User u SET u.active = :active WHERE u.company.id = :companyId")
    int updateActiveByCompanyId(@Param("companyId") Long companyId, @Param("active") boolean active);

    @EntityGraph(attributePaths = {"userRoles", "userRoles.role", "blueprint", "company", "manager"})
    @Query("""
            SELECT u FROM User u
            WHERE u.company.id = :companyId AND u.id <> :excludeUserId
            ORDER BY u.active DESC, u.firstName ASC, u.lastName ASC
            """)
    List<User> findByCompanyIdAndIdNotWithDetails(@Param("companyId") Long companyId,
                                                  @Param("excludeUserId") Long excludeUserId);

    Optional<User> findByPhoneNumber(String phoneNumber);

    Optional<User> findBySsn(String ssn);

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);

    boolean existsBySsn(String ssn);


}
