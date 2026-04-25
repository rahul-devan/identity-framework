package com.ndash.identity_framework.dto;

import lombok.Data;

@Data
public class RevokeRequest {
    private Long requesterId;
    private Long targetDepartmentId;
    private String comments;
}
