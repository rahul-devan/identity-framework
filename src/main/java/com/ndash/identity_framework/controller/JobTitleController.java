package com.ndash.identity_framework.controller;

import com.ndash.identity_framework.config.ApiPaths;
import com.ndash.identity_framework.dto.ApiResponse;
import com.ndash.identity_framework.dto.JobTitleResponse;
import com.ndash.identity_framework.services.JobTitleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({ApiPaths.V1 + "/job-titles", ApiPaths.LEGACY + "/job-titles"})
@RequiredArgsConstructor
public class JobTitleController {

    private final JobTitleService jobTitleService;

    // 🔹 GET ALL
    @GetMapping
    public ResponseEntity<ApiResponse<List<JobTitleResponse>>> getAll() {

        List<JobTitleResponse> response = jobTitleService.getAllJobTitles();

        return ResponseEntity.ok(ApiResponse.success(response, 200));
    }

    // 🔹 GET BY ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<JobTitleResponse>> getById(
            @PathVariable Long id) {

        JobTitleResponse response = jobTitleService.getById(id);

        return ResponseEntity.ok(ApiResponse.success(response, 200));
    }

}