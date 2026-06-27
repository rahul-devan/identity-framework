package com.ndash.identity_framework.services;

import com.ndash.identity_framework.domain.Company;
import com.ndash.identity_framework.domain.User;
import com.ndash.identity_framework.domain.enums.RequestStatus;
import com.ndash.identity_framework.domain.enums.UserSource;
import com.ndash.identity_framework.dto.CompanyRequestDto;
import com.ndash.identity_framework.dto.CompanyResponseDto;
import com.ndash.identity_framework.dto.UserDto;
import com.ndash.identity_framework.exception.ApiException;
import com.ndash.identity_framework.repositories.CompanyRepository;
import com.ndash.identity_framework.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    public void createCompany(CompanyRequestDto dto, Long requestedById) throws ApiException {

        User requestedBy = userRepository.findById(requestedById).orElse(null);
        if (requestedBy == null) {
            throw new ApiException("Requesting user not found");
        }

        Company company = new Company();
        company.setName(dto.getName());
        company.setLocation(dto.getLocation());
        company.setPhoneNumber(dto.getPhoneNumber());
        company.setStatus(RequestStatus.APPROVED);
        company.setEnabled(true);

        User approver = userRepository.findById(dto.getApproverId())
                .orElseThrow();
        company.setApprover(approver);

        // -----------------------------
        // Primary Contact User (Inactive until approval)
        // -----------------------------
        UserDto primaryContact = new UserDto();
        primaryContact.setFirstName(dto.getContact().getFirstName());
        primaryContact.setLastName(dto.getContact().getLastName());
        primaryContact.setEmail(dto.getContact().getEmail());
        primaryContact.setPhoneNumber(dto.getContact().getPhoneNumber());
        primaryContact.setDob(dto.getContact().getDob().atStartOfDay());
        primaryContact.setSsn(dto.getContact().getSsn());

        // Optional defaults
        primaryContact.setUsername(dto.getContact().getEmail());
        primaryContact.setSource(UserSource.APP);
        primaryContact.setManager(approver.getId());

        // IMPORTANT


        UserDto saved = userService.createUser(primaryContact, approver.getId());
        User companyContact = userRepository.findById(saved.getId()).orElse(null);
        if(null != companyContact) {
            company.setPrimaryContact(companyContact);
        }

        companyRepository.save(company);
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
                                ? c.getPrimaryContact().getId()
                                : null,
                        c.getPrimaryContact() != null
                                ? c.getPrimaryContact().getFirstName() + " " + c.getPrimaryContact().getLastName()
                                : null,
                        c.isEnabled()
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

        if(Objects.nonNull(dto.getContact())) {
            contact.setFirstName(dto.getContact().getFirstName());
            contact.setLastName(dto.getContact().getLastName());
            if (!Objects.isNull(dto.getContact().getEmail())) {
                contact.setEmail(dto.getContact().getEmail());
            }
            if (!Objects.isNull(dto.getContact().getPhoneNumber())) {
                contact.setPhoneNumber(dto.getContact().getPhoneNumber());
            }
            if (!Objects.isNull(dto.getContact().getDob())) {
                contact.setDob(dto.getContact().getDob().atStartOfDay());
            }
            if (!Objects.isNull(dto.getContact().getSsn())) {
                contact.setSsn(dto.getContact().getSsn());
            }
        }

        company.setEnabled(dto.isEnabled());
        if(!dto.isEnabled()){
            List<User> users = userRepository.findByCompanyId(company.getId());
            users.forEach(user -> {
                user.setActive(false);
                userRepository.save(user);
            });
            log.info("Company {} is disabled. All associated users have been deactivated.", company.getName());
        } else {
            List<User> users = userRepository.findByCompanyId(company.getId());
            users.forEach(user -> {
                user.setActive(true);
                userRepository.save(user);
            });
            log.info("Company {} is enabled. All associated users have been activated.", company.getName());
        }
        company.setPrimaryContact(contact);

        companyRepository.save(company);
    }

    public void deleteCompany(Long id) {
        companyRepository.deleteById(id);
    }

    public List<CompanyResponseDto> getMyCompanies(Long approverId) {

        List<Company> companies =
                companyRepository.findByApproverId(approverId);

        return companies.stream()
                .map(company -> {

                    CompanyResponseDto dto = new CompanyResponseDto();

                    dto.setId(company.getId());
                    dto.setName(company.getName());
                    dto.setLocation(company.getLocation());
                    dto.setPhoneNumber(company.getPhoneNumber());

                    if (company.getApprover() != null) {
                        dto.setApproverId(company.getApprover().getId());
                    }

                    if (company.getPrimaryContact() != null) {
                        dto.setPrimaryContactId(
                                company.getPrimaryContact().getId()
                        );
                        dto.setContactName(
                                company.getPrimaryContact().getFirstName() + " " + company.getPrimaryContact().getLastName()
                        );
                    }
                    dto.setEnabled(company.isEnabled());

                    return dto;
                })
                .toList();
    }
}
