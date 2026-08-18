package com.ndash.identity_framework.controller;

import com.ndash.identity_framework.dto.*;
import com.ndash.identity_framework.services.DelegateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/delegates")
@RequiredArgsConstructor
public class DelegateController {

    private final DelegateService delegateService;

    @PostMapping("/request")
    public ResponseEntity<ApiResponse<String>> createRequest(
            @RequestBody DelegateRequestDTO dto, @AuthenticationPrincipal Jwt jwt) {

        delegateService.createRequest(jwt.getClaim("userId"), dto);
        return ResponseEntity.ok(ApiResponse.success("Request submitted", HttpStatus.OK.value()));
    }

    @GetMapping("/requests")
    public ResponseEntity<ApiResponse<List<DelegateRequestResponseDTO>>> getRequests(
            @RequestParam Long departmentId) {

        return ResponseEntity.ok(ApiResponse.success(
                delegateService.getPendingRequests(departmentId),
                HttpStatus.OK.value()
        ));
    }

    @PostMapping("/{id}/action")
    public ResponseEntity<ApiResponse<String>> actOnRequest(
            @PathVariable Long id,
            @RequestBody DelegateActionDTO dto,
            @AuthenticationPrincipal Jwt jwt) {

        delegateService.actOnRequest(id, jwt.getClaim("userId"), dto);
        return ResponseEntity.ok(ApiResponse.success("Action completed", HttpStatus.OK.value()));
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<?>>> getDelegatedUsers(
            @AuthenticationPrincipal Jwt jwt) {

        return ResponseEntity.ok(ApiResponse.success(
                delegateService.getDelegatedUsers(jwt.getClaim("userId")),
                HttpStatus.OK.value()
        ));
    }

    @GetMapping("/my-requests")
    public ResponseEntity<ApiResponse<List<DelegateRequestResponseDTO>>> getMyRequests(
            @AuthenticationPrincipal Jwt jwt) {

        return ResponseEntity.ok(ApiResponse.success(
                delegateService.getMyRequests(jwt.getClaim("userId")),
                HttpStatus.OK.value()
        ));
    }

    @PostMapping("/revoke")
    public ResponseEntity<ApiResponse<String>> revokeDelegate(
            @RequestBody RevokeRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        delegateService.revokeDelegate(
                request.getRequesterId(),
                request.getTargetDepartmentId(),
                request.getComments(),
                jwt.getClaim("userId")
        );
        return ResponseEntity.ok(ApiResponse.success("Delegation revoked successfully", HttpStatus.OK.value()));
    }
}
