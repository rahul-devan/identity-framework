package com.ndash.identity_framework.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ApplicationDto {
    private Long id;
    private String name;
    private String description;
    private String appUrl;
    private boolean active;
}

