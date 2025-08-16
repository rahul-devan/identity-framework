package com.ndash.identity_framework.controller;

import com.ndash.identity_framework.dto.ApiResponse;
import com.ndash.identity_framework.dto.ApplicationDto;
import com.ndash.identity_framework.services.ApplicationService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<ApplicationDto>>> searchApplications(
            @RequestParam String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<ApplicationDto> results = applicationService.searchApplicationsByName(name, page, size);
        return ResponseEntity.ok(ApiResponse.success(results, 200));
    }
}

