package com.ndash.identity_framework.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class DelegateRequestResponseDTO {

    private Long id;
    private String requesterName;
    private String departmentName;
    private String status;
    private String comments;
    private LocalDateTime requestedAt;
}