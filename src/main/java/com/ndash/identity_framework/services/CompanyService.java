package com.ndash.identity_framework.services;

import com.ndash.identity_framework.domain.Company;
import com.ndash.identity_framework.domain.User;
import com.ndash.identity_framework.domain.enums.RequestStatus;
import com.ndash.identity_framework.domain.enums.UserSource;
import com.ndash.identity_framework.dto.CompanyRequestDto;
import com.ndash.identity_framework.dto.CompanyResponseDto;
import com.ndash.identity_framework.dto.FetchTypeEnum;
import com.ndash.identity_framework.dto.UserDto;
import com.ndash.identity_framework.exception.ApiException;
import com.ndash.identity_framework.exception.BadRequestException;
import com.ndash.identity_framework.exception.ResourceNotFoundException;
import com.ndash.identity_framework.mapper.UserMapper;
import com.ndash.identity_framework.repositories.CompanyRepository;
import com.ndash.identity_framework.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
                .orElseThrow(() -> new ResourceNotFoundException("Approver not found with id: " + dto.getApproverId()));
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

    @Transactional(readOnly = true)
    public List<CompanyResponseDto> getAllCompanies(final FetchTypeEnum fetchType) {
        Boolean enabledFilter = switch (fetchType) {
            case ACTIVE -> true;
            case INACTIVE -> false;
            case ALL -> null;
        };

        return companyRepository.findApprovedCompanyResponses(RequestStatus.APPROVED, enabledFilter);
    }

    @Transactional
    public void updateCompany(Long id, CompanyRequestDto dto) {
        Company company = companyRepository.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + id));

        company.setName(dto.getName());
        company.setLocation(dto.getLocation());
        company.setPhoneNumber(dto.getPhoneNumber());

        if (dto.getApproverId() != null) {
            boolean approverChanged = company.getApprover() == null
                    || !dto.getApproverId().equals(company.getApprover().getId());
            if (approverChanged) {
                if (!userRepository.existsById(dto.getApproverId())) {
                    throw new ResourceNotFoundException("Approver not found with id: " + dto.getApproverId());
                }
                company.setApprover(userRepository.getReferenceById(dto.getApproverId()));
            }
        }

        applyContactUpdates(company, dto);

        Boolean requestedEnabled = dto.getIsEnabled();
        if (requestedEnabled != null && requestedEnabled != company.isEnabled()) {
            company.setEnabled(requestedEnabled);
            int updatedUsers = userRepository.updateActiveByCompanyId(company.getId(), requestedEnabled);
            log.info(
                    "Company {} is {}. Updated active status for {} associated users.",
                    company.getName(),
                    requestedEnabled ? "enabled" : "disabled",
                    updatedUsers
            );
        }

        companyRepository.save(company);
    }

    private void applyContactUpdates(Company company, CompanyRequestDto dto) {
        if (dto.getContact() == null) {
            return;
        }

        User contact = company.getPrimaryContact();
        if (contact == null) {
            throw new BadRequestException("Primary contact not found for company id: " + company.getId());
        }

        contact.setFirstName(dto.getContact().getFirstName());
        contact.setLastName(dto.getContact().getLastName());

        if (dto.getContact().getEmail() != null) {
            contact.setEmail(dto.getContact().getEmail());
        }
        if (dto.getContact().getPhoneNumber() != null) {
            contact.setPhoneNumber(dto.getContact().getPhoneNumber());
        }
        if (dto.getContact().getDob() != null) {
            contact.setDob(dto.getContact().getDob().atStartOfDay());
        }
        if (dto.getContact().getSsn() != null) {
            contact.setSsn(dto.getContact().getSsn());
        }

        userRepository.save(contact);
    }

    public void deleteCompany(Long id) {
        companyRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<CompanyResponseDto> getMyCompanies(Long approverId) {
        return companyRepository.findCompanyResponsesByApproverId(approverId);
    }

    @Transactional(readOnly = true)
    public List<UserDto> getCompanyUsers(Long companyId, Long excludeUserId) throws ApiException {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + companyId));

        return userRepository.findByCompanyIdAndIdNotWithDetails(companyId, excludeUserId).stream()
                .map(user -> {
                    UserDto dto = UserMapper.toDto(user);
                    dto.setCompanyId(companyId);
                    dto.setCompanyName(company.getName());
                    return dto;
                })
                .toList();
    }
}
