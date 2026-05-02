package com.ndash.identity_framework.services;

import com.ndash.identity_framework.domain.Company;
import com.ndash.identity_framework.domain.CompanyContact;
import com.ndash.identity_framework.domain.User;
import com.ndash.identity_framework.domain.enums.UserSource;
import com.ndash.identity_framework.dto.CompanyRequestDto;
import com.ndash.identity_framework.dto.CompanyResponseDto;
import com.ndash.identity_framework.exception.ApiException;
import com.ndash.identity_framework.repositories.CompanyRepository;
import com.ndash.identity_framework.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final ApprovalService approvalService;

    public void createCompany(CompanyRequestDto dto, Long requestedById) throws ApiException {

        User requestedBy = userRepository.findById(requestedById).orElse(null);
        if (requestedBy == null) {
            throw new ApiException("Requesting user not found");
        }

        Company company = new Company();
        company.setName(dto.getName());
        company.setLocation(dto.getLocation());
        company.setPhoneNumber(dto.getPhoneNumber());

        User approver = userRepository.findById(dto.getApproverId())
                .orElseThrow();
        company.setApprover(approver);

        // -----------------------------
        // Primary Contact User (Inactive until approval)
        // -----------------------------
        User primaryContact = new User();
        primaryContact.setFirstName(dto.getContact().getFirstName());
        primaryContact.setLastName(dto.getContact().getLastName());
        primaryContact.setEmail(dto.getContact().getEmail());
        primaryContact.setPhoneNumber(dto.getContact().getPhoneNumber());
        primaryContact.setDob(dto.getContact().getDob().atStartOfDay());
        primaryContact.setSsn(dto.getContact().getSsn());

        // Optional defaults
        primaryContact.setUsername(dto.getContact().getEmail());
        primaryContact.setPassword(null); // Or temp password
        primaryContact.setSource(UserSource.APP);
        primaryContact.setManager(requestedBy);

        // IMPORTANT
        primaryContact.setActive(false);
        company.setPrimaryContact(primaryContact);

        userRepository.save(primaryContact);

        companyRepository.save(company);
        approvalService.createCompanyApproval(company, approver, requestedBy);
    }

    public List<CompanyResponseDto> getAllCompanies() {
        return companyRepository.findAll().stream()
                .filter(c -> c.getStatus() == null || c.getStatus().name().equals("APPROVED")) // Only show approved companies
                .map(c -> new CompanyResponseDto(
                        c.getId(),
                        c.getName(),
                        c.getLocation(),
                        c.getPhoneNumber(),
                        c.getApprover() != null ? c.getApprover().getFirstName() : null,
                        c.getApprover() != null ? c.getApprover().getId() : null,
                        c.getPrimaryContact() != null
                                ? c.getPrimaryContact().getFirstName() + " " + c.getPrimaryContact().getLastName()
                                : null
                ))
                .toList();
    }

    public void updateCompany(Long id, CompanyRequestDto dto) {

        Company company = companyRepository.findById(id).orElseThrow();

        company.setName(dto.getName());
        company.setLocation(dto.getLocation());
        company.setPhoneNumber(dto.getPhoneNumber());

        User approver = userRepository.findById(dto.getApproverId()).orElseThrow();
        company.setApprover(approver);

        User contact = company.getPrimaryContact();
        if (contact == null) contact = new User();

        contact.setFirstName(dto.getContact().getFirstName());
        contact.setLastName(dto.getContact().getLastName());
        if(!Objects.isNull(dto.getContact().getEmail())){
            contact.setEmail(dto.getContact().getEmail());
        }
        if (!Objects.isNull(dto.getContact().getPhoneNumber())) {
            contact.setPhoneNumber(dto.getContact().getPhoneNumber());
        }
        if(!Objects.isNull(dto.getContact().getDob())) {
            contact.setDob(dto.getContact().getDob().atStartOfDay());
        }
        if(!Objects.isNull(dto.getContact().getSsn())) {
            contact.setSsn(dto.getContact().getSsn());
        }

//        contact.setCompany(company);
        company.setPrimaryContact(contact);

        companyRepository.save(company);
    }

    public void deleteCompany(Long id) {
        companyRepository.deleteById(id);
    }
}
