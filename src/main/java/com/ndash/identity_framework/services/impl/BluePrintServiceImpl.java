package com.ndash.identity_framework.services.impl;

import com.ndash.identity_framework.domain.*;
import com.ndash.identity_framework.dto.BlueprintApplicationRequest;
import com.ndash.identity_framework.dto.BlueprintRequest;
import com.ndash.identity_framework.dto.BlueprintResponse;
import com.ndash.identity_framework.exception.BadRequestException;
import com.ndash.identity_framework.exception.ResourceNotFoundException;
import com.ndash.identity_framework.mapper.BlueprintMapper;
import com.ndash.identity_framework.repositories.ApplicationRepository;
import com.ndash.identity_framework.repositories.ApplicationRoleRepository;
import com.ndash.identity_framework.repositories.BlueprintRepository;
import com.ndash.identity_framework.repositories.JobTitleRepository;
import com.ndash.identity_framework.services.BluePrintService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class BluePrintServiceImpl implements BluePrintService {

    private final BlueprintRepository blueprintRepository;
    private final JobTitleRepository jobTitleRepository;
    private final ApplicationRepository applicationRepository;
    private final BlueprintMapper blueprintMapper;
    private final ApplicationRoleRepository applicationRoleRepository;

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

    @Override
    public BlueprintResponse createBlueprint(BlueprintRequest request) {

        log.info(
                "Creating blueprint with name={}",
                request.getName()
        );

        validateRequest(request);

        if (blueprintRepository.existsByNameIgnoreCase(request.getName())) {
            throw new BadRequestException("Blueprint already exists");
        }

        Blueprint blueprint = new Blueprint();

        blueprint.setName(request.getName());

        // =====================================
        // JOB TITLES
        // =====================================

        List<JobTitle> jobTitles =
                jobTitleRepository.findAllById(
                        request.getJobTitleIds()
                );

        if (jobTitles.size()
                != request.getJobTitleIds().size()) {

            throw new BadRequestException(
                    "Invalid jobTitleIds"
            );
        }

        blueprint.setJobTitles(
                new HashSet<>(jobTitles)
        );

        // =====================================
        // APPLICATION ROLE MAPPINGS
        // =====================================

        Set<BlueprintApplicationRole> mappings =
                new HashSet<>();

        for (BlueprintApplicationRequest appReq
                : request.getApplications()) {

            Application application =
                    applicationRepository.findById(
                                    appReq.getApplicationId()
                            )
                            .orElseThrow(() ->
                                    new BadRequestException(
                                            "Invalid applicationId"
                                    ));

            for (String roleName : appReq.getRoles()) {

                BlueprintApplicationRole mapping =
                        new BlueprintApplicationRole();

                mapping.setBlueprint(blueprint);

                mapping.setApplication(application);

                mapping.setApplicationRole(null);

                mapping.setRoleName(roleName);

                mappings.add(mapping);
            }
        }

        blueprint.setApplicationRoles(mappings);

        Blueprint saved =
                blueprintRepository.save(blueprint);

        return blueprintMapper.toResponse(saved);
    }

    // 🔹 UPDATE
    @Override
    public BlueprintResponse updateBlueprint(
            Long id,
            BlueprintRequest request
    ) {

        Blueprint existing =
                blueprintRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Blueprint not found"
                                ));

        // =====================================
        // NAME
        // =====================================

        if (request.getName() != null) {
            existing.setName(request.getName());
        }

        // =====================================
        // JOB TITLES
        // =====================================

        if (request.getJobTitleIds() != null) {

            List<JobTitle> jobTitles =
                    jobTitleRepository.findAllById(
                            request.getJobTitleIds()
                    );

            existing.getJobTitles().clear();

            existing.getJobTitles().addAll(jobTitles);
        }

        // =====================================
        // APPLICATION ROLE MAPPINGS
        // =====================================

        existing.getApplicationRoles().clear();

        Set<BlueprintApplicationRole> mappings =
                new HashSet<>();

        for (BlueprintApplicationRequest appReq
                : request.getApplications()) {

            Application application =
                    applicationRepository.findById(
                                    appReq.getApplicationId()
                            )
                            .orElseThrow(() ->
                                    new BadRequestException(
                                            "Invalid applicationId"
                                    ));

            for (String roleName : appReq.getRoles()) {

                BlueprintApplicationRole mapping =
                        new BlueprintApplicationRole();

                mapping.setBlueprint(existing);

                mapping.setApplication(application);

                mapping.setRoleName(roleName);

                mappings.add(mapping);
            }
        }

        existing.getApplicationRoles().addAll(mappings);

        Blueprint saved =
                blueprintRepository.save(existing);

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

        if (request.getApplications() == null || request.getApplications().isEmpty()) {
            throw new BadRequestException("At least one application is required");
        }
    }
}
