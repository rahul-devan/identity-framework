package com.ndash.identity_framework.controller;

import com.ndash.identity_framework.services.SettingService;
import lombok.RequiredArgsConstructor;
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
    public ResponseEntity<Map<String, Object>> getSettings() {
        return ResponseEntity.ok(service.getAllSettings());
    }

    @PostMapping
    public ResponseEntity<?> saveSettings(@RequestBody Map<String, String> settings) {
        service.saveAll(settings);
        return ResponseEntity.ok("Settings saved");
    }

    @GetMapping("/security")
    public ResponseEntity<Map<String, String>> getSecuritySettings() {

        Map<String, Object> settings = service.getAllSettings();

        return ResponseEntity.ok(Map.of(
                "value", String.valueOf(settings.getOrDefault(SESSION_TIMEOUT_KEY, "1")),
                "unit", String.valueOf(settings.getOrDefault(SESSION_TIMEOUT_UNIT_KEY, "HOURS"))
        ));
    }
}
