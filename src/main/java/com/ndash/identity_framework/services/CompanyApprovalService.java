package com.ndash.identity_framework.services;

import com.ndash.identity_framework.domain.Company;
import com.ndash.identity_framework.domain.CompanyApprovalRequest;
import com.ndash.identity_framework.domain.User;
import com.ndash.identity_framework.domain.enums.RequestStatus;
import com.ndash.identity_framework.dto.CompanyApprovalResponseDto;
import com.ndash.identity_framework.repositories.CompanyApprovalRequestRepository;
import com.ndash.identity_framework.repositories.CompanyRepository;
import com.ndash.identity_framework.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CompanyApprovalService {

    private final CompanyApprovalRequestRepository approvalRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;

    // -------------------------------------------------------
    // Create Approval Request
    // -------------------------------------------------------
    public void createCompanyApproval(Company company, User approver, User requestedBy) {

        CompanyApprovalRequest approval = new CompanyApprovalRequest();
        approval.setCompany(company);
        approval.setApprover(approver);
        approval.setRequestedBy(requestedBy);
        approval.setStatus(RequestStatus.PENDING);
        approval.setRequestedAt(LocalDateTime.now());

        approvalRepository.save(approval);
    }

    // -------------------------------------------------------
    // Pending approvals for logged-in approver
    // -------------------------------------------------------
    public List<CompanyApprovalResponseDto> getPendingApprovalsForApprover(Long approverId) {

        return approvalRepository
                .findByApproverIdAndStatusOrderByRequestedAtDesc(
                        approverId,
                        RequestStatus.PENDING
                )
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    // -------------------------------------------------------
    // Approve Company
    // -------------------------------------------------------
    @Transactional
    public void approveCompany(Long approvalId, Long approverId) {

        CompanyApprovalRequest request = approvalRepository.findById(approvalId)
                .orElseThrow(() -> new RuntimeException("Approval request not found"));

        // Security validation
        if (!request.getApprover().getId().equals(approverId)) {
            throw new RuntimeException("You are not authorized to approve this request");
        }

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new RuntimeException("Approval request already processed");
        }

        // Approval Request
        request.setStatus(RequestStatus.APPROVED);
        request.setReviewedAt(LocalDateTime.now());

        // Company
        Company company = request.getCompany();
        company.setStatus(RequestStatus.APPROVED);

        // Primary Contact User Activation
        User primaryContact = company.getPrimaryContact();
        primaryContact.setActive(true);

        approvalRepository.save(request);
        companyRepository.save(company);
        userRepository.save(primaryContact);
    }

    // -------------------------------------------------------
    // Reject Company
    // -------------------------------------------------------
    @Transactional
    public void rejectCompany(Long approvalId, Long approverId, String comments) {

        CompanyApprovalRequest request = approvalRepository.findById(approvalId)
                .orElseThrow(() -> new RuntimeException("Approval request not found"));

        // Security validation
        if (!request.getApprover().getId().equals(approverId)) {
            throw new RuntimeException("You are not authorized to reject this request");
        }

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new RuntimeException("Approval request already processed");
        }

        request.setStatus(RequestStatus.REJECTED);
        request.setComments(comments);
        request.setReviewedAt(LocalDateTime.now());

        Company company = request.getCompany();
        company.setStatus(RequestStatus.REJECTED);

        approvalRepository.save(request);
        companyRepository.save(company);
    }

    // -------------------------------------------------------
    // Approval history for logged-in approver
    // -------------------------------------------------------
    public List<CompanyApprovalResponseDto> getApprovalHistory(Long approverId) {

        return approvalRepository
                .findByApproverIdOrderByRequestedAtDesc(approverId)
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    // -------------------------------------------------------
    // Admin - All approvals
    // -------------------------------------------------------
    public List<CompanyApprovalResponseDto> getAllApprovals() {

        return approvalRepository
                .findAllByOrderByRequestedAtDesc()
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    // -------------------------------------------------------
    // DTO Mapper
    // -------------------------------------------------------
    private CompanyApprovalResponseDto mapToDto(CompanyApprovalRequest request) {

        CompanyApprovalResponseDto dto = new CompanyApprovalResponseDto();

        dto.setApprovalId(request.getId());

        // Approval
        dto.setStatus(request.getStatus());
        dto.setComments(request.getComments());
        dto.setRequestedAt(request.getRequestedAt());
        dto.setReviewedAt(request.getReviewedAt());

        // Company
        dto.setCompanyId(request.getCompany().getId());
        dto.setCompanyName(request.getCompany().getName());
        dto.setCompanyLocation(request.getCompany().getLocation());
        dto.setCompanyPhoneNumber(request.getCompany().getPhoneNumber());

        // Primary Contact
        if (request.getCompany().getPrimaryContact() != null) {
            User primary = request.getCompany().getPrimaryContact();

            dto.setPrimaryContactUserId(primary.getId());
            dto.setPrimaryContactFirstName(primary.getFirstName());
            dto.setPrimaryContactLastName(primary.getLastName());
            dto.setPrimaryContactEmail(primary.getEmail());
            dto.setPrimaryContactPhoneNumber(primary.getPhoneNumber());
        }

        // Approver
        User approver = request.getApprover();
        dto.setApproverId(approver.getId());
        dto.setApproverName(
                (approver.getFirstName() != null ? approver.getFirstName() : "") +
                        " " +
                        (approver.getLastName() != null ? approver.getLastName() : "")
        );
        dto.setApproverEmail(approver.getEmail());

        // Requested By
        if (request.getRequestedBy() != null) {
            User requester = request.getRequestedBy();

            dto.setRequestedById(requester.getId());
            dto.setRequestedByName(
                    (requester.getFirstName() != null ? requester.getFirstName() : "") +
                            " " +
                            (requester.getLastName() != null ? requester.getLastName() : "")
            );
        }

        return dto;
    }
}