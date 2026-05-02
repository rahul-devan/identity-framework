package com.ndash.identity_framework.controller;

import com.ndash.identity_framework.dto.CompanyRequestDto;
import com.ndash.identity_framework.dto.CompanyResponseDto;
import com.ndash.identity_framework.exception.ApiException;
import com.ndash.identity_framework.services.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
@CrossOrigin("*")
public class CompanyController {

    private final CompanyService service;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody CompanyRequestDto dto, @AuthenticationPrincipal Jwt jwt) throws ApiException {
        service.createCompany(dto, jwt.getClaim("userId"));
        return ResponseEntity.ok("Created");
    }

    @GetMapping
    public List<CompanyResponseDto> getAll() {
        return service.getAllCompanies();
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id,
                                    @RequestBody CompanyRequestDto dto) {
        service.updateCompany(id, dto);
        return ResponseEntity.ok("Updated");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        service.deleteCompany(id);
        return ResponseEntity.ok("Deleted");
    }
}
