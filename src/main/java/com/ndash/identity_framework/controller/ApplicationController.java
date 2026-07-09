package com.ndash.identity_framework.controller;

import com.ndash.identity_framework.config.ApiPaths;
import com.ndash.identity_framework.dto.ApiResponse;
import com.ndash.identity_framework.dto.ApplicationDto;
import com.ndash.identity_framework.dto.ApplicationRequestDto;
import com.ndash.identity_framework.dto.UserApplicationDto;
import com.ndash.identity_framework.security.RoleConstants;
import com.ndash.identity_framework.services.ApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({ApiPaths.V1 + "/applications", ApiPaths.LEGACY + "/applications"})
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<ApplicationDto>>> searchApplications(
            @RequestParam final String name,
            @RequestParam(defaultValue = "0") final int page,
            @RequestParam(defaultValue = "10") final int size,
            @AuthenticationPrincipal final Jwt jwt) {
        return ResponseEntity.ok(ApiResponse.success(
                applicationService.searchApplicationsByName(name, page, size), HttpStatus.OK.value()));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ApplicationDto>>> getAllApplications(
            @RequestParam(defaultValue = "0") final int page,
            @RequestParam(defaultValue = "10") final int size,
            @AuthenticationPrincipal final Jwt jwt) {
        return ResponseEntity.ok(ApiResponse.success(
                applicationService.getAllApplications(page, size), HttpStatus.OK.value()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ApplicationDto>> getApplicationById(@PathVariable final Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                applicationService.getApplicationById(id), HttpStatus.OK.value()));
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<ApiResponse<List<UserApplicationDto>>> getUserApplicationById(
            @PathVariable final Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                applicationService.getUserApplications(id), HttpStatus.OK.value()));
    }

    @PostMapping
    @PreAuthorize(RoleConstants.ADMIN_AUTHORITIES)
    public ResponseEntity<ApiResponse<ApplicationDto>> createApplication(
            @Valid @RequestBody final ApplicationRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                applicationService.createApplication(request), HttpStatus.CREATED.value()));
    }

    @PutMapping("/{id}")
    @PreAuthorize(RoleConstants.ADMIN_AUTHORITIES)
    public ResponseEntity<ApiResponse<ApplicationDto>> updateApplication(
            @PathVariable final Long id,
            @Valid @RequestBody final ApplicationRequestDto request) {
        return ResponseEntity.ok(ApiResponse.success(
                applicationService.updateApplication(id, request), HttpStatus.OK.value()));
    }

    @PostMapping("/{id}/deactivate")
    @PreAuthorize(RoleConstants.ADMIN_AUTHORITIES)
    public ResponseEntity<ApiResponse<Void>> deactivateApplication(@PathVariable final Long id) {
        applicationService.deactivateApplication(id);
        return ResponseEntity.ok(ApiResponse.success(null, HttpStatus.OK.value()));
    }
}
