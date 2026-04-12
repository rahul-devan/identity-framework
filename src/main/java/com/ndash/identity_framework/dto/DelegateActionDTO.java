package com.ndash.identity_framework.dto;

import com.ndash.identity_framework.domain.enums.RequestStatus;
import lombok.Data;

@Data
public class DelegateActionDTO {
    private RequestStatus status;
    private String comments;
}
