package com.ndash.identity_framework.controller;

import com.ndash.identity_framework.dto.ApiResponse;
import com.ndash.identity_framework.dto.UserDto;
import com.ndash.identity_framework.services.UserService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<ApiResponse<UserDto>> createUser(@RequestBody UserDto userDto) {
        UserDto createdUser = userService.createUser(userDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(createdUser, HttpStatus.CREATED.value()));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserDto>>> getAllUsers() {
        List<UserDto> users = userService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success(users, HttpStatus.OK.value()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDto>> getUserById(@PathVariable Long id) {
        UserDto user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(user, HttpStatus.OK.value()));
    }

    @PostMapping("/sync")
    public ResponseEntity<ApiResponse<String>> syncAzureUsers() {
        userService.syncUsersFromAzure();
        return ResponseEntity.ok(ApiResponse.success("Sync completed", 200));
    }

    @GetMapping("/search/users")
    public ResponseEntity<ApiResponse<Page<UserDto>>> searchUsers(
            @RequestParam String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<UserDto> results = userService.searchUsersByUsername(username, page, size);
        return ResponseEntity.ok(ApiResponse.success(results, 200));
    }
}

