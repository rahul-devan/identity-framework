package com.ndash.identity_framework.controller;

import com.ndash.identity_framework.dto.ApiResponse;
import com.ndash.identity_framework.dto.BlueprintRequest;
import com.ndash.identity_framework.dto.BlueprintResponse;
import com.ndash.identity_framework.services.BluePrintService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/blueprints")
public class BlueprintController {

    private final BluePrintService blueprintService;

    public BlueprintController(BluePrintService blueprintService) {
        this.blueprintService = blueprintService;
    }

    // 🔹 GET ALL
    @GetMapping
    public ResponseEntity<ApiResponse<List<BlueprintResponse>>> getAll() {

        List<BlueprintResponse> response = blueprintService.getAllBlueprints();

        return ResponseEntity.ok(ApiResponse.success(response, 200));
    }

    // 🔹 GET BY ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BlueprintResponse>> getById(
            @PathVariable Long id) {

        BlueprintResponse response = blueprintService.getBlueprintById(id);

        return ResponseEntity.ok(ApiResponse.success(response, 200));
    }

    // 🔹 CREATE
    @PostMapping
    public ResponseEntity<ApiResponse<BlueprintResponse>> create(
            @RequestBody BlueprintRequest request) {

        BlueprintResponse response = blueprintService.createBlueprint(request);

        return ResponseEntity.ok(ApiResponse.success(response, 201));
    }

    // 🔹 UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BlueprintResponse>> update(
            @PathVariable Long id,
            @RequestBody BlueprintRequest request) {

        BlueprintResponse response = blueprintService.updateBlueprint(id, request);

        return ResponseEntity.ok(ApiResponse.success(response, 200));
    }

    // 🔹 DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id) {

        blueprintService.deleteBlueprint(id);

        return ResponseEntity.ok(
                new ApiResponse<>("SUCCESS", 200, null, null)
        );
    }
}
