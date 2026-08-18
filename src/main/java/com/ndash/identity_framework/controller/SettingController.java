package com.ndash.identity_framework.controller;

import com.ndash.identity_framework.dto.ApiResponse;
import com.ndash.identity_framework.services.SettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
public class SettingController {

    private final SettingService service;

    private static final String SESSION_TIMEOUT_KEY = "jwt.session.timeout";
    private static final String SESSION_TIMEOUT_UNIT_KEY = "jwt.session.timeout.unit";

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSettings() {
        return ResponseEntity.ok(ApiResponse.success(service.getAllSettings(), HttpStatus.OK.value()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<String>> saveSettings(@RequestBody Map<String, String> settings) {
        service.saveAll(settings);
        return ResponseEntity.ok(ApiResponse.success("Settings saved", HttpStatus.OK.value()));
    }

    @GetMapping("/security")
    public ResponseEntity<ApiResponse<Map<String, String>>> getSecuritySettings() {
        Map<String, Object> settings = service.getAllSettings();

        return ResponseEntity.ok(ApiResponse.success(Map.of(
                "value", String.valueOf(settings.getOrDefault(SESSION_TIMEOUT_KEY, "1")),
                "unit", String.valueOf(settings.getOrDefault(SESSION_TIMEOUT_UNIT_KEY, "HOURS"))
        ), HttpStatus.OK.value()));
    }
}
