package com.ndash.identity_framework.controller;

import com.ndash.identity_framework.config.ApiPaths;
import com.ndash.identity_framework.dto.ApiResponse;
import com.ndash.identity_framework.dto.AzureUserDto;
import com.ndash.identity_framework.mapper.AzureUserMapper;
import com.ndash.identity_framework.security.RoleConstants;
import com.ndash.identity_framework.services.AzureADService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({ApiPaths.V1 + "/azure", ApiPaths.LEGACY + "/azure"})
@RequiredArgsConstructor
@PreAuthorize(RoleConstants.ADMIN_AUTHORITIES)
public class AzureController {

    private final AzureADService azureADService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AzureUserDto>>> getAllUsers() {
        final List<AzureUserDto> users = azureADService.getAllUsers().stream()
                .map(AzureUserMapper::toDto)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(users, HttpStatus.OK.value()));
    }

    @PostMapping("/user/create")
    public ResponseEntity<ApiResponse<AzureUserDto>> createUser(
            @RequestParam final String displayName,
            @RequestParam final String mail) {
        final AzureUserDto created = AzureUserMapper.toDto(azureADService.createUser(displayName, mail));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created, HttpStatus.CREATED.value()));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable final String userId) {
        azureADService.deleteUser(userId);
        return ResponseEntity.ok(ApiResponse.success(null, HttpStatus.OK.value()));
    }
}
