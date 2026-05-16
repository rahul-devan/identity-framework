package com.ndash.identity_framework.controller;

import com.ndash.identity_framework.dto.ApiResponse;
import com.ndash.identity_framework.dto.JobTitleResponse;
import com.ndash.identity_framework.services.JobTitleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/job-titles")
public class JobTitleController {

    private final JobTitleService jobTitleService;

    public JobTitleController(JobTitleService jobTitleService) {
        this.jobTitleService = jobTitleService;
    }

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