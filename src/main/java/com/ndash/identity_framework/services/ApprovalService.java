package com.ndash.identity_framework.services;

import com.ndash.identity_framework.domain.Company;
import com.ndash.identity_framework.domain.CompanyApprovalRequest;
import com.ndash.identity_framework.domain.User;
import com.ndash.identity_framework.domain.enums.RequestStatus;
import com.ndash.identity_framework.repositories.CompanyApprovalRequestRepository;
import com.ndash.identity_framework.repositories.CompanyRepository;
import com.ndash.identity_framework.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ApprovalService {

    private final CompanyApprovalRequestRepository approvalRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;

    public void createCompanyApproval(Company company, User approver, User requestedBy) {

        CompanyApprovalRequest approval = new CompanyApprovalRequest();
        approval.setCompany(company);
        approval.setApprover(approver);
        approval.setRequestedBy(requestedBy);
        approval.setStatus(RequestStatus.PENDING);

        approvalRepository.save(approval);
    }

    @Transactional
    public void approveCompany(Long approvalId) {

        CompanyApprovalRequest request = approvalRepository.findById(approvalId)
                .orElseThrow();

        request.setStatus(RequestStatus.APPROVED);
        request.setReviewedAt(LocalDateTime.now());

        Company company = request.getCompany();
        company.setStatus(RequestStatus.APPROVED);

        User primaryContact = company.getPrimaryContact();
        primaryContact.setActive(true);

        approvalRepository.save(request);
        companyRepository.save(company);
        userRepository.save(primaryContact);
    }

    @Transactional
    public void rejectCompany(Long approvalId, String comments) {

        CompanyApprovalRequest request = approvalRepository.findById(approvalId)
                .orElseThrow();

        request.setStatus(RequestStatus.REJECTED);
        request.setComments(comments);
        request.setReviewedAt(LocalDateTime.now());

        Company company = request.getCompany();
        company.setStatus(RequestStatus.REJECTED);

        approvalRepository.save(request);
        companyRepository.save(company);
    }
}