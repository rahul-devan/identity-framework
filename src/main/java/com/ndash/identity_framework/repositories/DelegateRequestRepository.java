package com.ndash.identity_framework.repositories;

import com.ndash.identity_framework.domain.DelegateRequest;
import com.ndash.identity_framework.domain.enums.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DelegateRequestRepository extends JpaRepository<DelegateRequest, Long> {

    List<DelegateRequest> findByRequesterId(Long requesterId);

    List<DelegateRequest> findByTargetDepartmentIdAndStatus(Long deptId, RequestStatus status);

    boolean existsByRequesterIdAndTargetDepartmentIdAndStatus(Long requesterId, Long deptId, RequestStatus status);

    Optional<DelegateRequest>
    findByRequesterIdAndTargetDepartmentIdAndStatus(
            Long requesterId,
            Long departmentId,
            RequestStatus status
    );

}
