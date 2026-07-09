package com.ndash.identity_framework.controller;

import com.ndash.identity_framework.config.ApiPaths;
import com.ndash.identity_framework.dto.*;
import com.ndash.identity_framework.services.DelegateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({ApiPaths.V1 + "/delegates", ApiPaths.LEGACY + "/delegates"})
@RequiredArgsConstructor
public class DelegateController {

    private final DelegateService delegateService;

    // 🔹 Submit request
    @PostMapping("/request")
    public ResponseEntity<ApiResponse<Void>> createRequest(
            @Valid @RequestBody final DelegateRequestDTO dto,
            @AuthenticationPrincipal final Jwt jwt) {

        delegateService.createRequest(jwt.getClaim("userId"), dto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(null, HttpStatus.CREATED.value()));
    }

    // 🔹 Get pending requests for department
    @GetMapping("/requests")
    public ResponseEntity<ApiResponse<List<DelegateRequestResponseDTO>>> getRequests(
            @RequestParam Long departmentId) {

        return ResponseEntity.ok(
                ApiResponse.success(delegateService.getPendingRequests(departmentId), HttpStatus.OK.value())
        );
    }

    // 🔹 Approve / Reject
    @PostMapping("/{id}/action")
    public ResponseEntity<ApiResponse<Void>> actOnRequest(
            @PathVariable final Long id,
            @Valid @RequestBody final DelegateActionDTO dto,
            @AuthenticationPrincipal final Jwt jwt) {

        delegateService.actOnRequest(id, jwt.getClaim("userId"), dto);

        return ResponseEntity.ok(ApiResponse.success(null, HttpStatus.OK.value()));
    }

    // 🔹 Get delegated users (UI screen)
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<?>>> getDelegatedUsers(
            @AuthenticationPrincipal Jwt jwt) {

        return ResponseEntity.ok(
                ApiResponse.success(delegateService.getDelegatedUsers(jwt.getClaim("userId")), HttpStatus.OK.value())
        );
    }

    @GetMapping("/my-requests")
    public ResponseEntity<ApiResponse<List<DelegateRequestResponseDTO>>> getMyRequests(@AuthenticationPrincipal Jwt jwt) {

        return ResponseEntity.ok(
                ApiResponse.success(delegateService.getMyRequests(jwt.getClaim("userId")), HttpStatus.OK.value())
        );
    }

    @PostMapping("/revoke")
    public ResponseEntity<ApiResponse<Void>> revokeDelegate(
            @Valid @RequestBody final RevokeRequest request,
            @AuthenticationPrincipal final Jwt jwt) {

        delegateService.revokeDelegate(
                request.getRequesterId(),
                request.getTargetDepartmentId(),
                request.getComments(),
                jwt.getClaim("userId")
        );
        return ResponseEntity.ok(ApiResponse.success(null, HttpStatus.OK.value()));
    }
}