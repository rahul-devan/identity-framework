package com.ndash.identity_framework.services;

import com.ndash.identity_framework.domain.Application;
import com.ndash.identity_framework.dto.ApplicationDto;
import com.ndash.identity_framework.mapper.ApplicationMapper;
import com.ndash.identity_framework.repositories.ApplicationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;

    public ApplicationService(ApplicationRepository applicationRepository) {
        this.applicationRepository = applicationRepository;
    }

    public Page<ApplicationDto> searchApplicationsByName(String name, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Application> apps = applicationRepository
                .findByNameContainingIgnoreCase(name, pageable);

        return apps.map(ApplicationMapper::toDto);
    }
}

