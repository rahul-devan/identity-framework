package com.ndash.identity_framework.services;

import com.ndash.identity_framework.domain.Application;
import com.ndash.identity_framework.dto.ApplicationDto;
import com.ndash.identity_framework.exception.ResourceNotFoundException;
import com.ndash.identity_framework.dto.UserApplicationDto;
import com.ndash.identity_framework.mapper.ApplicationMapper;
import com.ndash.identity_framework.repositories.ApplicationRepository;
import com.ndash.identity_framework.repositories.UserApplicationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final UserApplicationRepository userApplicationRepository;

    public ApplicationService(ApplicationRepository applicationRepository, UserApplicationRepository userApplicationRepository) {
        this.applicationRepository = applicationRepository;
        this.userApplicationRepository = userApplicationRepository;
    }

    @Transactional(readOnly = true)
    public Page<ApplicationDto> searchApplicationsByName(String name, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Application> apps = applicationRepository
                .findByNameContainingIgnoreCase(name, pageable);

        return apps.map(ApplicationMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<ApplicationDto> getAllApplications(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Application> apps = applicationRepository.findAll(pageable);
        return apps.map(ApplicationMapper::toDto);
    }

    @Transactional(readOnly = true)
    public ApplicationDto getApplicationById(Long id) {
        Application app = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + id));
        return ApplicationMapper.toDto(app);
    }

    @Transactional(readOnly = true)
    public List<UserApplicationDto> getUserApplications(Long userId) {
        return userApplicationRepository.findActiveApplicationDtosByUserId(userId);
    }
}
