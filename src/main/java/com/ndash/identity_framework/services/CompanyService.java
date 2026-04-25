package com.ndash.identity_framework.services;

import com.ndash.identity_framework.domain.Company;
import com.ndash.identity_framework.domain.CompanyContact;
import com.ndash.identity_framework.domain.User;
import com.ndash.identity_framework.dto.CompanyRequestDto;
import com.ndash.identity_framework.dto.CompanyResponseDto;
import com.ndash.identity_framework.repositories.CompanyRepository;
import com.ndash.identity_framework.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;

    public void createCompany(CompanyRequestDto dto) {

        Company company = new Company();
        company.setName(dto.getName());
        company.setLocation(dto.getLocation());
        company.setPhoneNumber(dto.getPhoneNumber());

        User approver = userRepository.findById(dto.getApproverId())
                .orElseThrow();
        company.setApprover(approver);

        CompanyContact contact = new CompanyContact();
        contact.setFirstName(dto.getContact().getFirstName());
        contact.setLastName(dto.getContact().getLastName());
        contact.setEmail(dto.getContact().getEmail());
        contact.setPhoneNumber(dto.getContact().getPhoneNumber());
        contact.setDob(dto.getContact().getDob());
        contact.setSsn(dto.getContact().getSsn());

        contact.setCompany(company);
        company.setContact(contact);

        companyRepository.save(company);
    }

    public List<CompanyResponseDto> getAllCompanies() {
        return companyRepository.findAll().stream()
                .map(c -> new CompanyResponseDto(
                        c.getId(),
                        c.getName(),
                        c.getLocation(),
                        c.getPhoneNumber(),
                        c.getApprover() != null ? c.getApprover().getFirstName() : null,
                        c.getContact() != null
                                ? c.getContact().getFirstName() + " " + c.getContact().getLastName()
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

        CompanyContact contact = company.getContact();
        if (contact == null) contact = new CompanyContact();

        contact.setFirstName(dto.getContact().getFirstName());
        contact.setLastName(dto.getContact().getLastName());
        contact.setEmail(dto.getContact().getEmail());
        contact.setPhoneNumber(dto.getContact().getPhoneNumber());
        contact.setDob(dto.getContact().getDob());
        contact.setSsn(dto.getContact().getSsn());

        contact.setCompany(company);
        company.setContact(contact);

        companyRepository.save(company);
    }

    public void deleteCompany(Long id) {
        companyRepository.deleteById(id);
    }
}
