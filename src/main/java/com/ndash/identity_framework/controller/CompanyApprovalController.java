package com.ndash.identity_framework.controller;

import com.ndash.identity_framework.dto.CompanyApprovalResponseDto;
import com.ndash.identity_framework.dto.RejectApprovalRequestDto;
import com.ndash.identity_framework.services.CompanyApprovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/company-approvals")
@RequiredArgsConstructor
@CrossOrigin("*")
public class CompanyApprovalController {

    private final CompanyApprovalService service;

    // -------------------------------------------------------
    // Get all pending approvals assigned to logged-in approver
    // -------------------------------------------------------
    @GetMapping("/pending")
    public ResponseEntity<List<CompanyApprovalResponseDto>> getPendingApprovals(
            @AuthenticationPrincipal Jwt jwt) {

        Long approverId = Long.valueOf(jwt.getClaim("userId").toString());

        return ResponseEntity.ok(
                service.getPendingApprovalsForApprover(approverId)
        );
    }

    // -------------------------------------------------------
    // Approve company request
    // Activates primary contact user + approves company
    // -------------------------------------------------------
    @PutMapping("/{approvalId}/approve")
    public ResponseEntity<?> approveCompany(
            @PathVariable Long approvalId,
            @AuthenticationPrincipal Jwt jwt) {

        Long approverId = Long.valueOf(jwt.getClaim("userId").toString());

        service.approveCompany(approvalId, approverId);

        return ResponseEntity.ok("Company approved successfully");
    }

    // -------------------------------------------------------
    // Reject company request
    // Keeps primary contact inactive
    // -------------------------------------------------------
    @PutMapping("/{approvalId}/reject")
    public ResponseEntity<?> rejectCompany(
            @PathVariable Long approvalId,
            @RequestBody RejectApprovalRequestDto dto,
            @AuthenticationPrincipal Jwt jwt) {

        Long approverId = Long.valueOf(jwt.getClaim("userId").toString());

        service.rejectCompany(
                approvalId,
                approverId,
                dto.getComments()
        );

        return ResponseEntity.ok("Company rejected successfully");
    }

    // -------------------------------------------------------
    // Approval history for logged-in approver
    // -------------------------------------------------------
    @GetMapping("/history")
    public ResponseEntity<List<CompanyApprovalResponseDto>> getApprovalHistory(
            @AuthenticationPrincipal Jwt jwt) {

        Long approverId = Long.valueOf(jwt.getClaim("userId").toString());

        return ResponseEntity.ok(
                service.getApprovalHistory(approverId)
        );
    }

    // -------------------------------------------------------
    // Optional Admin API
    // View all approvals
    // -------------------------------------------------------
    @GetMapping
    public ResponseEntity<List<CompanyApprovalResponseDto>> getAllApprovals() {

        return ResponseEntity.ok(
                service.getAllApprovals()
        );
    }
}