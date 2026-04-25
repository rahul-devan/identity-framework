package com.ndash.identity_framework.repositories;

import com.ndash.identity_framework.domain.UserDepartmentAccess;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserDepartmentAccessRepository extends JpaRepository<UserDepartmentAccess, Long> {

    List<UserDepartmentAccess> findByUserId(Long userId);

    boolean existsByUserIdAndDepartmentId(Long userId, Long departmentId);
    void deleteByUserIdAndDepartmentId(Long userId, Long departmentId);
}
