package com.ndash.identity_framework.controller;

import com.ndash.identity_framework.dto.ApiResponse;
import com.ndash.identity_framework.dto.CompanyRequestDto;
import com.ndash.identity_framework.dto.CompanyResponseDto;
import com.ndash.identity_framework.dto.FetchTypeEnum;
import com.ndash.identity_framework.dto.UserDto;
import com.ndash.identity_framework.exception.ApiException;
import com.ndash.identity_framework.services.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService service;

    @PostMapping
    public ResponseEntity<ApiResponse<String>> create(@RequestBody CompanyRequestDto dto,
                                                        @AuthenticationPrincipal Jwt jwt) {
        service.createCompany(dto, jwt.getClaim("userId"));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Created", HttpStatus.CREATED.value()));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CompanyResponseDto>>> getAll(
            @RequestParam(name = "fetchType", defaultValue = "ALL") FetchTypeEnum fetchType
    ) {
        return ResponseEntity.ok(ApiResponse.success(service.getAllCompanies(fetchType), HttpStatus.OK.value()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> update(@PathVariable Long id,
                                                      @RequestBody CompanyRequestDto dto) {
        service.updateCompany(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Updated", HttpStatus.OK.value()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> delete(@PathVariable Long id) {
        service.deleteCompany(id);
        return ResponseEntity.ok(ApiResponse.success("Deleted", HttpStatus.OK.value()));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<CompanyResponseDto>>> getMyCompanies(
            @AuthenticationPrincipal Jwt jwt) {
        Long userId = jwt.getClaim("userId");
        return ResponseEntity.ok(ApiResponse.success(service.getMyCompanies(userId), HttpStatus.OK.value()));
    }

    @GetMapping("/{companyId}/users")
    public ResponseEntity<ApiResponse<List<UserDto>>> getCompanyUsers(
            @PathVariable Long companyId,
            @RequestParam Long userId) throws ApiException {
        List<UserDto> users = service.getCompanyUsers(companyId, userId);
        return ResponseEntity.ok(ApiResponse.success(users, HttpStatus.OK.value()));
    }
}
