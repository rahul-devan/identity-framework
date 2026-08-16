package com.ndash.identity_framework.repositories;

import com.ndash.identity_framework.domain.Company;
import com.ndash.identity_framework.domain.enums.RequestStatus;
import com.ndash.identity_framework.dto.CompanyResponseDto;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, Long> {
    List<Company> findByApproverId(Long approverId);

    @EntityGraph(attributePaths = {"approver"})
    Optional<Company> findWithApproverById(Long id);

    @EntityGraph(attributePaths = {"approver", "primaryContact"})
    Optional<Company> findWithDetailsById(Long id);

    @Query("""
            SELECT new com.ndash.identity_framework.dto.CompanyResponseDto(
                c.id, c.name, c.location, c.phoneNumber,
                a.firstName, a.id,
                pc.id,
                TRIM(CONCAT(COALESCE(pc.firstName, ''), ' ', COALESCE(pc.lastName, ''))),
                c.isEnabled
            )
            FROM Company c
            LEFT JOIN c.approver a
            LEFT JOIN c.primaryContact pc
            WHERE (c.status IS NULL OR c.status = :approvedStatus)
              AND (:enabled IS NULL OR c.isEnabled = :enabled)
            ORDER BY c.isEnabled DESC, c.name ASC
            """)
    List<CompanyResponseDto> findApprovedCompanyResponses(
            @Param("approvedStatus") RequestStatus approvedStatus,
            @Param("enabled") Boolean enabled);

    @Query("""
            SELECT new com.ndash.identity_framework.dto.CompanyResponseDto(
                c.id, c.name, c.location, c.phoneNumber,
                a.firstName, a.id,
                pc.id,
                TRIM(CONCAT(COALESCE(pc.firstName, ''), ' ', COALESCE(pc.lastName, ''))),
                c.isEnabled
            )
            FROM Company c
            LEFT JOIN c.approver a
            LEFT JOIN c.primaryContact pc
            WHERE c.approver.id = :approverId
            ORDER BY c.isEnabled DESC, c.name ASC
            """)
    List<CompanyResponseDto> findCompanyResponsesByApproverId(@Param("approverId") Long approverId);
}

