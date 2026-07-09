package com.ndash.identity_framework.controller;

import com.ndash.identity_framework.config.ApiPaths;
import com.ndash.identity_framework.dto.ApiResponse;
import com.ndash.identity_framework.dto.ResetPasswordRequest;
import com.ndash.identity_framework.dto.UserDto;
import com.ndash.identity_framework.exception.ApiException;
import com.ndash.identity_framework.security.RoleConstants;
import com.ndash.identity_framework.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({ApiPaths.V1 + "/users", ApiPaths.LEGACY + "/users"})
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    @PreAuthorize(RoleConstants.ADMIN_AUTHORITIES)
    public ResponseEntity<ApiResponse<UserDto>> createUser(
            @RequestBody final UserDto userDto,
            @AuthenticationPrincipal final Jwt jwt) throws ApiException {
        final Long loggedInUserId = userService.getUserById(jwt.getClaim("userId")).getId();
        final UserDto createdUser = userService.createUser(userDto, loggedInUserId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(createdUser, HttpStatus.CREATED.value()));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserDto>>> getAllUsers(
            @AuthenticationPrincipal final Jwt jwt) throws ApiException {
        return ResponseEntity.ok(ApiResponse.success(userService.getAllUsers(), HttpStatus.OK.value()));
    }

    @GetMapping("/departments/{departmentId}")
    public ResponseEntity<ApiResponse<List<UserDto>>> getUsersByDepartment(
            @PathVariable final Long departmentId,
            @AuthenticationPrincipal final Jwt jwt) throws ApiException {
        return ResponseEntity.ok(ApiResponse.success(
                userService.getUsersByDepartment(departmentId), HttpStatus.OK.value()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDto>> getUserById(
            @PathVariable final Long id,
            @AuthenticationPrincipal final Jwt jwt) {
        return ResponseEntity.ok(ApiResponse.success(userService.getUserById(id), HttpStatus.OK.value()));
    }

    @PostMapping("/sync")
    @PreAuthorize(RoleConstants.ADMIN_AUTHORITIES)
    public ResponseEntity<ApiResponse<String>> syncAzureUsers() {
        userService.syncUsersFromAzure();
        return ResponseEntity.ok(ApiResponse.success("Sync completed", HttpStatus.OK.value()));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<UserDto>>> searchUsers(
            @RequestParam final String username,
            @RequestParam(defaultValue = "0") final int page,
            @RequestParam(defaultValue = "10") final int size,
            @AuthenticationPrincipal final Jwt jwt) {
        return ResponseEntity.ok(ApiResponse.success(
                userService.searchUsersByUsername(username, page, size), HttpStatus.OK.value()));
    }

    @PutMapping("/{id}")
    @PreAuthorize(RoleConstants.ADMIN_AUTHORITIES)
    public ResponseEntity<ApiResponse<UserDto>> updateUser(
            @PathVariable final Long id,
            @RequestBody final UserDto userDto,
            @AuthenticationPrincipal final Jwt jwt) throws ApiException {
        return ResponseEntity.ok(ApiResponse.success(
                userService.updateUser(id, userDto), HttpStatus.OK.value()));
    }

    @PostMapping("/{id}/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword(
            @PathVariable final Long id,
            @RequestBody final ResetPasswordRequest request,
            @AuthenticationPrincipal final Jwt jwt) throws ApiException {
        userService.resetPassword(id, request, jwt);
        return ResponseEntity.ok(ApiResponse.success(
                "Password updated successfully", HttpStatus.OK.value()));
    }

    @GetMapping("/managers")
    public ResponseEntity<ApiResponse<List<UserDto>>> getAllManagers(
            @AuthenticationPrincipal final Jwt jwt) throws ApiException {
        return ResponseEntity.ok(ApiResponse.success(userService.getAllManagers(), HttpStatus.OK.value()));
    }
}
