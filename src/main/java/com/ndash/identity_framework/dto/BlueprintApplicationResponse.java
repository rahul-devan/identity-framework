package com.ndash.identity_framework.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class BlueprintApplicationResponse {

    private Long applicationId;

    private String applicationName;
    private Boolean essential;

    private List<String> roles;
}