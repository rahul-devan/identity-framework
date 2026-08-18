package com.ndash.identity_framework.controller;

import com.microsoft.graph.models.User;
import com.ndash.identity_framework.dto.ApiResponse;
import com.ndash.identity_framework.services.AzureADService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/azure")
public class AzureController {

    @Autowired
    private AzureADService azureADService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers() {
        return ResponseEntity.ok(ApiResponse.success(azureADService.getAllUsers(), HttpStatus.OK.value()));
    }

    @PostMapping("/user/create")
    public ResponseEntity<ApiResponse<User>> createUser(@RequestParam String firstName,
                                                        @RequestParam String lastName,
                                                        @RequestParam String mail,
                                                        @RequestParam(required = false) Long userId) {
        Long resolvedUserId = userId != null ? userId : System.currentTimeMillis();
        User user = azureADService.createUser(firstName, lastName, mail, resolvedUserId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(user, HttpStatus.CREATED.value()));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<String>> deleteUser(@PathVariable String userId) {
        azureADService.deleteUser(userId);
        return ResponseEntity.ok(ApiResponse.success("User deleted", HttpStatus.OK.value()));
    }
}

