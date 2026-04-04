package com.ndash.identity_framework.services.impl;

import com.ndash.identity_framework.domain.Application;
import com.ndash.identity_framework.domain.Blueprint;
import com.ndash.identity_framework.domain.JobTitle;
import com.ndash.identity_framework.dto.BlueprintRequest;
import com.ndash.identity_framework.dto.BlueprintResponse;
import com.ndash.identity_framework.exception.BadRequestException;
import com.ndash.identity_framework.exception.ResourceNotFoundException;
import com.ndash.identity_framework.mapper.BlueprintMapper;
import com.ndash.identity_framework.repositories.ApplicationRepository;
import com.ndash.identity_framework.repositories.BlueprintRepository;
import com.ndash.identity_framework.repositories.JobTitleRepository;
import com.ndash.identity_framework.services.BluePrintService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;

@Service
@Slf4j
@Transactional
public class BluePrintServiceImpl implements BluePrintService {

    private final BlueprintRepository blueprintRepository;
    private final JobTitleRepository jobTitleRepository;
    private final ApplicationRepository applicationRepository;
    private final BlueprintMapper blueprintMapper;

    public BluePrintServiceImpl(BlueprintRepository blueprintRepository,
                                JobTitleRepository jobTitleRepository,
                                ApplicationRepository applicationRepository,
                                BlueprintMapper blueprintMapper) {
        this.blueprintRepository = blueprintRepository;
        this.jobTitleRepository = jobTitleRepository;
        this.applicationRepository = applicationRepository;
        this.blueprintMapper = blueprintMapper;
    }

    // 🔹 GET ALL
    @Override
    @Transactional(readOnly = true)
    public List<BlueprintResponse> getAllBlueprints() {

        log.info("Fetching all blueprints");

        List<Blueprint> blueprints = blueprintRepository.findAll();

        return blueprints.stream()
                .map(blueprintMapper::toResponse)
                .toList();
    }

    // 🔹 GET BY ID
    @Override
    @Transactional(readOnly = true)
    public BlueprintResponse getBlueprintById(Long id) {

        log.info("Fetching blueprint with id={}", id);

        Blueprint blueprint = blueprintRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Blueprint not found with id={}", id);
                    return new ResourceNotFoundException("Blueprint not found with id: " + id);
                });

        return blueprintMapper.toResponse(blueprint);
    }

    // 🔹 CREATE
    @Override
    public BlueprintResponse createBlueprint(BlueprintRequest request) {

        log.info("Creating blueprint with name={}", request.getName());

        validateRequest(request);

        if (blueprintRepository.existsByNameIgnoreCase(request.getName())) {
            log.error("Blueprint already exists with name={}", request.getName());
            throw new BadRequestException("Blueprint already exists with name: " + request.getName());
        }

        Blueprint blueprint = new Blueprint();
        blueprint.setName(request.getName());

        // 🔥 JobTitles
        List<JobTitle> jobTitles = jobTitleRepository.findAllById(request.getJobTitleIds());

        if (jobTitles.size() != request.getJobTitleIds().size()) {
            throw new BadRequestException("Invalid jobTitleIds provided");
        }

        blueprint.setJobTitles(new HashSet<>(jobTitles));

        // 🔥 Applications
        List<Application> applications = applicationRepository.findAllById(request.getApplicationIds());

        if (applications.size() != request.getApplicationIds().size()) {
            throw new BadRequestException("Invalid applicationIds provided");
        }

        blueprint.setApplications(new HashSet<>(applications));

        Blueprint saved = blueprintRepository.save(blueprint);

        log.info("Blueprint created successfully with id={}", saved.getId());

        return blueprintMapper.toResponse(saved);
    }

    // 🔹 UPDATE
    @Override
    public BlueprintResponse updateBlueprint(Long id, BlueprintRequest request) {

        log.info("Updating blueprint with id={}", id);

        validateRequest(request);

        Blueprint existing = blueprintRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Blueprint not found for update with id={}", id);
                    return new ResourceNotFoundException("Blueprint not found with id: " + id);
                });

        // 🔥 Update Name
        if (request.getName() != null) {
            existing.setName(request.getName());
        }

        // 🔥 Update JobTitles
        if (request.getJobTitleIds() != null) {

            List<JobTitle> jobTitles =
                    jobTitleRepository.findAllById(request.getJobTitleIds());

            if (jobTitles.size() != request.getJobTitleIds().size()) {
                throw new BadRequestException("Invalid jobTitleIds provided");
            }

            existing.getJobTitles().clear();
            existing.getJobTitles().addAll(jobTitles);
        }

        // 🔥 Update Applications
        if (request.getApplicationIds() != null) {

            List<Application> applications =
                    applicationRepository.findAllById(request.getApplicationIds());

            if (applications.size() != request.getApplicationIds().size()) {
                throw new BadRequestException("Invalid applicationIds provided");
            }

            existing.getApplications().clear();
            existing.getApplications().addAll(applications);
        }

        Blueprint saved = blueprintRepository.save(existing);

        log.info("Blueprint updated successfully with id={}", saved.getId());

        return blueprintMapper.toResponse(saved);
    }

    // 🔹 DELETE
    @Override
    public void deleteBlueprint(Long id) {

        log.info("Deleting blueprint with id={}", id);

        Blueprint blueprint = blueprintRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Blueprint not found for deletion with id={}", id);
                    return new ResourceNotFoundException("Blueprint not found with id: " + id);
                });

        blueprintRepository.delete(blueprint);

        log.info("Blueprint deleted successfully with id={}", id);
    }

    // 🔥 VALIDATION METHOD
    private void validateRequest(BlueprintRequest request) {

        if (request.getName() == null || request.getName().isBlank()) {
            throw new BadRequestException("Blueprint name cannot be empty");
        }

        if (request.getJobTitleIds() == null || request.getJobTitleIds().isEmpty()) {
            throw new BadRequestException("At least one jobTitleId is required");
        }

        if (request.getApplicationIds() == null || request.getApplicationIds().isEmpty()) {
            throw new BadRequestException("At least one applicationId is required");
        }
    }
}
