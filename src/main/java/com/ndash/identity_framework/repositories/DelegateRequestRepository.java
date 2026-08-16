package com.ndash.identity_framework.repositories;

import com.ndash.identity_framework.domain.DelegateRequest;
import com.ndash.identity_framework.domain.enums.RequestStatus;
import com.ndash.identity_framework.dto.DelegateRequestResponseDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Query("""
            SELECT new com.ndash.identity_framework.dto.DelegateRequestResponseDTO(
                dr.id,
                TRIM(CONCAT(COALESCE(r.firstName, ''), ' ', COALESCE(r.lastName, ''))),
                td.name,
                dr.status,
                dr.comments,
                dr.requestedAt,
                TRIM(CONCAT(COALESCE(ab.firstName, ''), ' ', COALESCE(ab.lastName, ''))),
                dr.actionedAt
            )
            FROM DelegateRequest dr
            JOIN dr.requester r
            JOIN dr.targetDepartment td
            LEFT JOIN dr.actionedBy ab
            WHERE dr.requester.id = :requesterId
              AND dr.status <> com.ndash.identity_framework.domain.enums.RequestStatus.REVOKED
            ORDER BY dr.requestedAt DESC
            """)
    List<DelegateRequestResponseDTO> findMyRequestResponsesByRequesterId(
            @Param("requesterId") Long requesterId);

    @Query("""
            SELECT new com.ndash.identity_framework.dto.DelegateRequestResponseDTO(
                dr.id,
                TRIM(CONCAT(COALESCE(r.firstName, ''), ' ', COALESCE(r.lastName, ''))),
                td.name,
                dr.status,
                dr.comments,
                dr.requestedAt,
                '',
                null
            )
            FROM DelegateRequest dr
            JOIN dr.requester r
            JOIN dr.targetDepartment td
            WHERE dr.targetDepartment.id = :departmentId AND dr.status = :status
            ORDER BY dr.requestedAt DESC
            """)
    List<DelegateRequestResponseDTO> findPendingRequestResponsesByDepartmentId(
            @Param("departmentId") Long departmentId,
            @Param("status") RequestStatus status);
}
