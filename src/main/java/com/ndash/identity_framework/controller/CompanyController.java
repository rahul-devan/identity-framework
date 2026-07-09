package com.ndash.identity_framework.controller;

import com.ndash.identity_framework.config.ApiPaths;
import com.ndash.identity_framework.dto.ApiResponse;
import com.ndash.identity_framework.dto.CompanyRequestDto;
import com.ndash.identity_framework.dto.CompanyResponseDto;
import com.ndash.identity_framework.exception.ApiException;
import com.ndash.identity_framework.security.RoleConstants;
import com.ndash.identity_framework.services.CompanyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({ApiPaths.V1 + "/companies", ApiPaths.LEGACY + "/companies"})
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService service;

    @PostMapping
    @PreAuthorize(RoleConstants.ADMIN_AUTHORITIES)
    public ResponseEntity<ApiResponse<Void>> create(
            @Valid @RequestBody final CompanyRequestDto dto,
            @AuthenticationPrincipal final Jwt jwt) throws ApiException {
        service.createCompany(dto, jwt.getClaim("userId"));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(null, HttpStatus.CREATED.value()));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CompanyResponseDto>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(service.getAllCompanies(), HttpStatus.OK.value()));
    }

    @PutMapping("/{id}")
    @PreAuthorize(RoleConstants.ADMIN_AUTHORITIES)
    public ResponseEntity<ApiResponse<Void>> update(
            @PathVariable final Long id,
            @Valid @RequestBody final CompanyRequestDto dto) {
        service.updateCompany(id, dto);
        return ResponseEntity.ok(ApiResponse.success(null, HttpStatus.OK.value()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(RoleConstants.ADMIN_AUTHORITIES)
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable final Long id) {
        service.deleteCompany(id);
        return ResponseEntity.ok(ApiResponse.success(null, HttpStatus.OK.value()));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<CompanyResponseDto>>> getMyCompanies(
            @AuthenticationPrincipal final Jwt jwt) {
        final Long userId = jwt.getClaim("userId");
        return ResponseEntity.ok(ApiResponse.success(service.getMyCompanies(userId), HttpStatus.OK.value()));
    }
}
