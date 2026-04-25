package com.ndash.identity_framework.controller;

import com.ndash.identity_framework.dto.CompanyRequestDto;
import com.ndash.identity_framework.dto.CompanyResponseDto;
import com.ndash.identity_framework.services.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
@CrossOrigin("*")
public class CompanyController {

    private final CompanyService service;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody CompanyRequestDto dto) {
        service.createCompany(dto);
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
