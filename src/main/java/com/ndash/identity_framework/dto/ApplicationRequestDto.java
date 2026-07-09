package com.ndash.identity_framework.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ApplicationRequestDto {

    @NotBlank
    private String name;

    private String description;
    private String appUrl;
    private String integrationName;
    private Boolean essential;
    private Boolean active;
}
