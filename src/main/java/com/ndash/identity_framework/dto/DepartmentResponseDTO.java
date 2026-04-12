package com.ndash.identity_framework.dto;

import lombok.Data;

@Data
public class DepartmentResponseDTO {

    private Long id;
    private String name;
    private String externalId;
    private String externalSource;
}