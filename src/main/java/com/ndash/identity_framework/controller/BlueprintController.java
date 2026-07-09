package com.ndash.identity_framework.controller;

import com.ndash.identity_framework.config.ApiPaths;
import com.ndash.identity_framework.dto.ApiResponse;
import com.ndash.identity_framework.dto.BlueprintRequest;
import com.ndash.identity_framework.dto.BlueprintResponse;
import com.ndash.identity_framework.security.RoleConstants;
import com.ndash.identity_framework.services.BluePrintService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({ApiPaths.V1 + "/blueprints", ApiPaths.LEGACY + "/blueprints"})
@RequiredArgsConstructor
public class BlueprintController {

    private final BluePrintService blueprintService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<BlueprintResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(blueprintService.getAllBlueprints(), HttpStatus.OK.value()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BlueprintResponse>> getById(@PathVariable final Long id) {
        return ResponseEntity.ok(ApiResponse.success(blueprintService.getBlueprintById(id), HttpStatus.OK.value()));
    }

    @PostMapping
    @PreAuthorize(RoleConstants.ADMIN_AUTHORITIES)
    public ResponseEntity<ApiResponse<BlueprintResponse>> create(@Valid @RequestBody final BlueprintRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                blueprintService.createBlueprint(request), HttpStatus.CREATED.value()));
    }

    @PutMapping("/{id}")
    @PreAuthorize(RoleConstants.ADMIN_AUTHORITIES)
    public ResponseEntity<ApiResponse<BlueprintResponse>> update(
            @PathVariable final Long id,
            @Valid @RequestBody final BlueprintRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                blueprintService.updateBlueprint(id, request), HttpStatus.OK.value()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(RoleConstants.ADMIN_AUTHORITIES)
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable final Long id) {
        blueprintService.deleteBlueprint(id);
        return ResponseEntity.ok(ApiResponse.success(null, HttpStatus.OK.value()));
    }
}
