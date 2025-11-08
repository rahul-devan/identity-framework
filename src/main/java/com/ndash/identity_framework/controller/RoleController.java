package com.ndash.identity_framework.controller;

import com.ndash.identity_framework.dto.ApiResponse;
import com.ndash.identity_framework.dto.PaginatedResponse;
import com.ndash.identity_framework.dto.RoleDto;
import com.ndash.identity_framework.exception.ApiException;
import com.ndash.identity_framework.services.RoleService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RoleDto>> createRole(@RequestBody RoleDto roleDto,
                                                           @AuthenticationPrincipal Jwt jwt) {
        RoleDto createdRole = roleService.createRole(roleDto);
        return ResponseEntity.ok(ApiResponse.success(createdRole, 200));
    }

    // Get All Roles (non-paginated)
    @GetMapping
    public ResponseEntity<ApiResponse<List<RoleDto>>> getAllRoles(@AuthenticationPrincipal Jwt jwt) throws ApiException {
        List<RoleDto> roles = roleService.getAllRoles();
        return ResponseEntity.ok(ApiResponse.success(roles, 200));
    }

    // Search Roles (paginated)
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<RoleDto>>> searchRoles(
            @RequestParam String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal Jwt jwt) {

        List<RoleDto> rolesPage = roleService.searchRolesByName(name, page, size);
        return ResponseEntity.ok(ApiResponse.success(rolesPage, 200));
    }

    // Get Role by ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RoleDto>> getRoleById(@PathVariable Long id,
                                                            @AuthenticationPrincipal Jwt jwt) {
        RoleDto role = roleService.getRoleById(id);
        return ResponseEntity.ok(ApiResponse.success(role, 200));
    }

    // Delete Role
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRole(@PathVariable Long id,
                                                        @AuthenticationPrincipal Jwt jwt) {
        roleService.deleteRole(id);
        return ResponseEntity.ok(ApiResponse.success(null, 200));
    }
}
