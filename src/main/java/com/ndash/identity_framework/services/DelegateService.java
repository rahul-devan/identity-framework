package com.ndash.identity_framework.services;

import com.ndash.identity_framework.dto.*;

import java.util.List;

public interface DelegateService {

    void createRequest(Long userId, DelegateRequestDTO dto);

    List<DelegateRequestResponseDTO> getPendingRequests(Long departmentId);

    void actOnRequest(Long requestId, Long approverId, DelegateActionDTO dto);

    List<?> getDelegatedUsers(Long userId);

    List<DelegateRequestResponseDTO> getMyRequests(Long userId);
    void revokeDelegate(Long requesterId, Long departmentId, String comments, Long actionedById);
}