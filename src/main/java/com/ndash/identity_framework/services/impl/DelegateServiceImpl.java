package com.ndash.identity_framework.services.impl;

import com.ndash.identity_framework.domain.*;
import com.ndash.identity_framework.domain.enums.RequestStatus;
import com.ndash.identity_framework.dto.*;
import com.ndash.identity_framework.dto.DelegateRequestDTO;
import com.ndash.identity_framework.exception.ApiException;
import com.ndash.identity_framework.repositories.*;
import com.ndash.identity_framework.services.DelegateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DelegateServiceImpl implements DelegateService {

    private final DelegateRequestRepository delegateRequestRepository;
    private final UserDepartmentAccessRepository accessRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;

    @Override
    public void createRequest(Long userId, DelegateRequestDTO dto) {

        if (delegateRequestRepository.existsByRequesterIdAndTargetDepartmentIdAndStatus(
                userId, dto.getTargetDepartmentId(), RequestStatus.PENDING)) {
            throw new RuntimeException("Request already pending");
        }

        com.ndash.identity_framework.domain.DelegateRequest request = new com.ndash.identity_framework.domain.DelegateRequest();
        request.setRequester(userRepository.findById(userId).orElseThrow());
        request.setTargetDepartment(departmentRepository.findById(dto.getTargetDepartmentId()).orElseThrow());
        request.setStatus(RequestStatus.PENDING);
        request.setComments(dto.getComments());
        request.setRequestedAt(LocalDateTime.now());

        delegateRequestRepository.save(request);
    }

    @Override
    public List<DelegateRequestResponseDTO> getPendingRequests(Long departmentId) {

        return delegateRequestRepository
                .findByTargetDepartmentIdAndStatus(departmentId, RequestStatus.PENDING)
                .stream()
                .map(req -> DelegateRequestResponseDTO.builder()
                        .id(req.getId())
                        .requesterName(req.getRequester().getFirstName() + " " + req.getRequester().getLastName())
                        .departmentName(req.getTargetDepartment().getName())
                        .status(req.getStatus().name())
                        .comments(req.getComments())
                        .requestedAt(req.getRequestedAt())
                        .build()
                ).toList();
    }

    @Override
    public void actOnRequest(Long requestId, Long approverId, DelegateActionDTO dto) {

        DelegateRequest request = delegateRequestRepository.findById(requestId)
                .orElseThrow();

        request.setStatus(dto.getStatus());
        request.setActionedBy(userRepository.findById(approverId).orElseThrow());
        request.setActionedAt(LocalDateTime.now());
        request.setComments(dto.getComments());

        delegateRequestRepository.save(request);

        if (dto.getStatus() == RequestStatus.APPROVED) {

            boolean exists = accessRepository.existsByUserIdAndDepartmentId(
                    request.getRequester().getId(),
                    request.getTargetDepartment().getId()
            );

            if (!exists) {
                UserDepartmentAccess access = new UserDepartmentAccess();
                access.setUser(request.getRequester());
                access.setDepartment(request.getTargetDepartment());
                access.setGrantedAt(LocalDateTime.now());

                accessRepository.save(access);
            }
        }
    }

    @Override
    public List<DelegatedUserDto> getDelegatedUsers(Long userId) {

        List<Long> deptIds = accessRepository.findByUserId(userId)
                .stream()
                .map(a -> a.getDepartment().getId())
                .toList();

        return userRepository.findByDepartmentIdIn(deptIds)
                .stream()
                .map(user -> new DelegatedUserDto(
                        user.getId(),
                        user.getFirstName() + " " + user.getLastName(),
                        user.getEmail(),
                        user.getFirstName() + " " + user.getLastName(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getJobTitleName(),
                        user.getAzureId(),
                        user.getDepartment().getId(),
                        user.getDepartment().getName()
                ))
                .toList();
    }

    @Override
    public List<DelegateRequestResponseDTO> getMyRequests(Long userId) {

        return delegateRequestRepository.findByRequesterId(userId)
                .stream()
                .filter(req -> req.getStatus() != RequestStatus.REVOKED)
                .map(req -> DelegateRequestResponseDTO.builder()
                        .id(req.getId())
                        .requesterName(req.getRequester().getFirstName() + " " + req.getRequester().getLastName())
                        .departmentName(req.getTargetDepartment().getName())
                        .status(req.getStatus().name())
                        .comments(req.getComments())
                        .requestedAt(req.getRequestedAt())
                        .actionedAt(req.getActionedAt() != null ? req.getActionedAt() : null)
                        .actionedByName(req.getActionedBy() != null ? req.getActionedBy().getFirstName() + " " + req.getActionedBy().getLastName() : "")
                        .build()
                ).toList();
    }

    @Override
    @Transactional
    public void revokeDelegate(Long requesterId, Long departmentId, String comments, Long actionedById) {

        User currentUser = userRepository.findById(actionedById)
                .orElseThrow(() -> new RuntimeException("User not found"));

        DelegateRequest request = delegateRequestRepository
                .findByRequesterIdAndTargetDepartmentIdAndStatus(
                        requesterId,
                        departmentId,
                        RequestStatus.APPROVED
                )
                .orElseThrow(() -> new RuntimeException("No active delegation found"));

        // 👇 SIMPLE check (not heavy)
        if (!request.getRequester().getId().equals(currentUser.getId())
                && (request.getActionedBy() == null ||
                !request.getActionedBy().getId().equals(currentUser.getId()))) {

            throw new RuntimeException("Not allowed to revoke");
        }

        request.setStatus(RequestStatus.REVOKED);
        request.setComments(comments);
        request.setActionedAt(LocalDateTime.now());
        request.setActionedBy(currentUser);

        delegateRequestRepository.save(request);
        accessRepository.deleteByUserIdAndDepartmentId(requesterId, departmentId);
    }
}