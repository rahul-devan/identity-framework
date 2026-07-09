package com.ndash.identity_framework.controller;

import com.ndash.identity_framework.config.ApiPaths;
import com.ndash.identity_framework.dto.ApiResponse;
import com.ndash.identity_framework.dto.DepartmentResponseDTO;
import com.ndash.identity_framework.services.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({ApiPaths.V1 + "/departments", ApiPaths.LEGACY + "/departments"})
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<DepartmentResponseDTO>>> getAllDepartments() {
        List<DepartmentResponseDTO> departments = departmentService.getAllDepartments();

        return ResponseEntity.ok(
                ApiResponse.success(departments, 200)
        );
    }
}