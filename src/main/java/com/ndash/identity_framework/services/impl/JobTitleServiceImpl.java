package com.ndash.identity_framework.services.impl;

import com.ndash.identity_framework.domain.JobTitle;
import com.ndash.identity_framework.dto.JobTitleResponse;
import com.ndash.identity_framework.exception.ResourceNotFoundException;
import com.ndash.identity_framework.mapper.JobTitleMapper;
import com.ndash.identity_framework.repositories.JobTitleRepository;
import com.ndash.identity_framework.services.JobTitleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@Transactional
public class JobTitleServiceImpl implements JobTitleService {

    private final JobTitleRepository repository;
    private final JobTitleMapper mapper;

    public JobTitleServiceImpl(JobTitleRepository repository,
                               JobTitleMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    // 🔹 GET ALL
    @Override
    @Transactional(readOnly = true)
    public List<JobTitleResponse> getAllJobTitles() {

        log.info("Fetching all job titles");

        return repository.findAll()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    // 🔹 GET BY ID
    @Override
    @Transactional(readOnly = true)
    public JobTitleResponse getById(Long id) {

        log.info("Fetching job title with id={}", id);

        JobTitle jt = repository.findById(id)
                .orElseThrow(() -> {
                    log.error("JobTitle not found with id={}", id);
                    return new ResourceNotFoundException("JobTitle not found with id: " + id);
                });

        return mapper.toResponse(jt);
    }
}
