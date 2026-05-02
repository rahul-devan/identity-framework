package com.ndash.identity_framework.dto;

import com.ndash.identity_framework.domain.enums.RequestStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CompanyApprovalResponseDto {

    private Long approvalId;

    // Approval Info
    private RequestStatus status;
    private String comments;
    private LocalDateTime requestedAt;
    private LocalDateTime reviewedAt;

    // Company Info
    private Long companyId;
    private String companyName;
    private String companyLocation;
    private String companyPhoneNumber;

    // Primary Contact User Info
    private Long primaryContactUserId;
    private String primaryContactFirstName;
    private String primaryContactLastName;
    private String primaryContactEmail;
    private String primaryContactPhoneNumber;

    // Approver Info
    private Long approverId;
    private String approverName;
    private String approverEmail;

    // Requested By Info
    private Long requestedById;
    private String requestedByName;
}