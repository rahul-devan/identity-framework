package com.ndash.identity_framework.dto;

import com.ndash.identity_framework.domain.enums.RequestStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class DelegateRequestResponseDTO {

    private Long id;
    private String requesterName;
    private String departmentName;
    private String status;
    private String comments;
    private LocalDateTime requestedAt;
    private String actionedByName;
    private LocalDateTime actionedAt;

    public DelegateRequestResponseDTO(
            Long id,
            String requesterName,
            String departmentName,
            RequestStatus status,
            String comments,
            LocalDateTime requestedAt,
            String actionedByName,
            LocalDateTime actionedAt
    ) {
        this.id = id;
        this.requesterName = requesterName;
        this.departmentName = departmentName;
        this.status = status != null ? status.name() : null;
        this.comments = comments;
        this.requestedAt = requestedAt;
        this.actionedByName = actionedByName != null ? actionedByName.trim() : "";
        this.actionedAt = actionedAt;
    }
}
