package com.ndash.identity_framework.controller;

import com.ndash.identity_framework.dto.ApiResponse;
import com.ndash.identity_framework.dto.ApplicationDto;
import com.ndash.identity_framework.dto.UserApplicationDto;
import com.ndash.identity_framework.services.ApplicationService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<ApplicationDto>>> searchApplications(
            @RequestParam String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal Jwt jwt) {

        Page<ApplicationDto> results = applicationService.searchApplicationsByName(name, page, size);
        return ResponseEntity.ok(ApiResponse.success(results, 200));
    }

    @GetMapping()
    public ResponseEntity<ApiResponse<Page<ApplicationDto>>> getAllApplications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal Jwt jwt) {

        Page<ApplicationDto> results = applicationService.getAllApplications( page, size);
        return ResponseEntity.ok(ApiResponse.success(results, 200));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ApplicationDto>> getApplicationById(
            @PathVariable Long id){
        ApplicationDto app = applicationService.getApplicationById(id);
        return ResponseEntity.ok(ApiResponse.success(app, 200));
    }


    @GetMapping("/users/{id}")
    public ResponseEntity<ApiResponse<List<UserApplicationDto>>> getUserApplicationById(
            @PathVariable Long id){
        List<UserApplicationDto> userApplications = applicationService.getUserApplications(id);
        return ResponseEntity.ok(ApiResponse.success(userApplications, 200));
    }
}

