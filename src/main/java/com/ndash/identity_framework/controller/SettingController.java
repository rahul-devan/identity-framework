package com.ndash.identity_framework.controller;

import com.ndash.identity_framework.services.SettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
@CrossOrigin("*")
public class SettingController {

    private final SettingService service;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getSettings() {
        return ResponseEntity.ok(service.getAllSettings());
    }

    @PostMapping
    public ResponseEntity<?> saveSettings(@RequestBody Map<String, String> settings) {
        service.saveAll(settings);
        return ResponseEntity.ok("Settings saved");
    }
}
