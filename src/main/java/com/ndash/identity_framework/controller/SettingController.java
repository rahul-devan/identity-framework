package com.ndash.identity_framework.controller;

import com.ndash.identity_framework.config.ApiPaths;
import com.ndash.identity_framework.dto.ApiResponse;
import com.ndash.identity_framework.security.RoleConstants;
import com.ndash.identity_framework.services.SettingService;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping({ApiPaths.V1 + "/settings", ApiPaths.LEGACY + "/settings"})
@RequiredArgsConstructor
@Validated
public class SettingController {

    private final SettingService service;

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSettings() {
        return ResponseEntity.ok(ApiResponse.success(service.getAllSettings(), HttpStatus.OK.value()));
    }

    @PostMapping
    @PreAuthorize(RoleConstants.ADMIN_AUTHORITIES)
    public ResponseEntity<ApiResponse<Void>> saveSettings(
            @RequestBody @NotEmpty final Map<String, String> settings) {
        service.saveAll(settings);
        return ResponseEntity.ok(ApiResponse.success(null, HttpStatus.OK.value()));
    }
}
