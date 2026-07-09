package com.ndash.identity_framework.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DelegateRequestDTO {

    @NotNull
    private Long targetDepartmentId;

    private String comments;
}
