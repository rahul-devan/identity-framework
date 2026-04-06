package com.ndash.identity_framework.controller;

import com.ndash.identity_framework.dto.ApiResponse;
import com.ndash.identity_framework.dto.ResetPasswordRequest;
import com.ndash.identity_framework.dto.UserDto;
import com.ndash.identity_framework.exception.ApiException;
import com.ndash.identity_framework.services.UserService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import org.springframework.http.HttpStatus;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserDto>> createUser(@RequestBody UserDto userDto,
                                                           @AuthenticationPrincipal Jwt jwt) throws ApiException {
        UserDto createdUser = userService.createUser(userDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(createdUser, HttpStatus.CREATED.value()));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserDto>>> getAllUsers(@AuthenticationPrincipal Jwt jwt) throws ApiException {
        List<UserDto> users = userService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success(users, HttpStatus.OK.value()));
    }

    @GetMapping("/department/{departmentId}")
    public ResponseEntity<ApiResponse<List<UserDto>>> getUsersByDepartment(@PathVariable Long departmentId,
                                                                          @AuthenticationPrincipal Jwt jwt) throws ApiException {
        List<UserDto> users = userService.getUsersByDepartment(departmentId);
        return ResponseEntity.ok(ApiResponse.success(users, HttpStatus.OK.value()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDto>> getUserById(@PathVariable Long id,
                                                            @AuthenticationPrincipal Jwt jwt) {
        UserDto user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(user, HttpStatus.OK.value()));
    }

    @PostMapping("/sync")
    public ResponseEntity<ApiResponse<String>> syncAzureUsers() {
        userService.syncUsersFromAzure();
        return ResponseEntity.ok(ApiResponse.success("Sync completed", 200));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<UserDto>>> searchUsers(
            @RequestParam String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal Jwt jwt) {

        Page<UserDto> results = userService.searchUsersByUsername(username, page, size);
        return ResponseEntity.ok(ApiResponse.success(results, 200));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDto>> updateUser(
            @PathVariable Long id,
            @RequestBody UserDto userDto,
            @AuthenticationPrincipal Jwt jwt) throws ApiException {

        UserDto updatedUser = userService.updateUser(id, userDto);

        return ResponseEntity.ok(
                ApiResponse.success(updatedUser, HttpStatus.OK.value())
        );
    }

    @PostMapping("/{id}/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword(
            @PathVariable Long id,
            @RequestBody ResetPasswordRequest request,
            @AuthenticationPrincipal Jwt jwt) throws ApiException {

        userService.resetPassword(id, request, jwt);

        return ResponseEntity.ok(
                ApiResponse.success("Password updated successfully", HttpStatus.OK.value())
        );
    }
}

