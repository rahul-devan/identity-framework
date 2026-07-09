package com.ndash.identity_framework.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RevokeRequest {

    @NotNull
    private Long requesterId;

    @NotNull
    private Long targetDepartmentId;

    private String comments;
}
