package com.ndash.identity_framework.services;

import com.ndash.identity_framework.domain.Application;
import com.ndash.identity_framework.dto.ApplicationDto;
import com.ndash.identity_framework.dto.ApplicationRequestDto;
import com.ndash.identity_framework.dto.UserApplicationDto;
import com.ndash.identity_framework.exception.BadRequestException;
import com.ndash.identity_framework.exception.ResourceNotFoundException;
import com.ndash.identity_framework.mapper.ApplicationMapper;
import com.ndash.identity_framework.mapper.UserMapper;
import com.ndash.identity_framework.repositories.ApplicationRepository;
import com.ndash.identity_framework.repositories.UserApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final UserApplicationRepository userApplicationRepository;

    @Transactional(readOnly = true)
    public Page<ApplicationDto> searchApplicationsByName(final String name, final int page, final int size) {
        final Pageable pageable = PageRequest.of(page, size);
        return applicationRepository.findByNameContainingIgnoreCase(name, pageable)
                .map(ApplicationMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<ApplicationDto> getAllApplications(final int page, final int size) {
        final Pageable pageable = PageRequest.of(page, size);
        return applicationRepository.findAll(pageable).map(ApplicationMapper::toDto);
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "applications", key = "#id")
    public ApplicationDto getApplicationById(final Long id) {
        final Application app = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + id));
        return ApplicationMapper.toDto(app);
    }

    @Transactional(readOnly = true)
    public List<UserApplicationDto> getUserApplications(final Long userId) {
        return userApplicationRepository.findByUserIdAndActiveTrue(userId)
                .stream()
                .map(UserMapper::toUserApplicationDto)
                .toList();
    }

    public ApplicationDto createApplication(final ApplicationRequestDto request) {
        if (applicationRepository.existsByName(request.getName())) {
            throw new BadRequestException("Application already exists: " + request.getName());
        }
        final Application application = new Application();
        application.setName(request.getName());
        application.setDescription(request.getDescription());
        application.setAppUrl(request.getAppUrl());
        application.setIntegrationName(request.getIntegrationName());
        application.setEssential(request.getEssential());
        application.setActive(request.getActive() == null || request.getActive());
        return ApplicationMapper.toDto(applicationRepository.save(application));
    }

    public ApplicationDto updateApplication(final Long id, final ApplicationRequestDto request) {
        final Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + id));
        if (request.getName() != null) {
            application.setName(request.getName());
        }
        if (request.getDescription() != null) {
            application.setDescription(request.getDescription());
        }
        if (request.getAppUrl() != null) {
            application.setAppUrl(request.getAppUrl());
        }
        if (request.getIntegrationName() != null) {
            application.setIntegrationName(request.getIntegrationName());
        }
        if (request.getEssential() != null) {
            application.setEssential(request.getEssential());
        }
        if (request.getActive() != null) {
            application.setActive(request.getActive());
        }
        return ApplicationMapper.toDto(applicationRepository.save(application));
    }

    public void deactivateApplication(final Long id) {
        final Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + id));
        application.setActive(false);
        applicationRepository.save(application);
    }
}
