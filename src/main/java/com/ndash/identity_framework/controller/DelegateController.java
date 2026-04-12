package com.ndash.identity_framework.controller;

import com.ndash.identity_framework.dto.*;
import com.ndash.identity_framework.services.DelegateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/delegates")
@RequiredArgsConstructor
@CrossOrigin("*")
public class DelegateController {

    private final DelegateService delegateService;

    // 🔹 Submit request
    @PostMapping("/request")
    public ResponseEntity<ApiResponse<Void>> createRequest(
            @RequestParam Long userId,
            @RequestBody DelegateRequestDTO dto) {

        delegateService.createRequest(userId, dto);

        return ResponseEntity.ok(
                new ApiResponse<>("Request submitted", 200, null, null)
        );
    }

    // 🔹 Get pending requests for department
    @GetMapping("/requests")
    public ResponseEntity<ApiResponse<List<DelegateRequestResponseDTO>>> getRequests(
            @RequestParam Long departmentId) {

        return ResponseEntity.ok(
                new ApiResponse<>("Success", 200,
                        delegateService.getPendingRequests(departmentId), null)
        );
    }

    // 🔹 Approve / Reject
    @PostMapping("/{id}/action")
    public ResponseEntity<ApiResponse<Void>> actOnRequest(
            @PathVariable Long id,
            @RequestParam Long approverId,
            @RequestBody DelegateActionDTO dto) {

        delegateService.actOnRequest(id, approverId, dto);

        return ResponseEntity.ok(
                new ApiResponse<>("Action completed", 200, null, null)
        );
    }

    // 🔹 Get delegated users (UI screen)
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<?>>> getDelegatedUsers(
            @RequestParam Long userId) {

        return ResponseEntity.ok(
                new ApiResponse<>("Success", 200,
                        delegateService.getDelegatedUsers(userId), null)
        );
    }
}