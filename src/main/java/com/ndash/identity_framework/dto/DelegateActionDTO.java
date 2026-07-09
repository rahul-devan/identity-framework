package com.ndash.identity_framework.dto;

import com.ndash.identity_framework.domain.enums.RequestStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DelegateActionDTO {

    @NotNull
    private RequestStatus status;

    private String comments;
}
