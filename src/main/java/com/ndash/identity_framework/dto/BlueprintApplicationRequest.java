package com.ndash.identity_framework.dto;

import lombok.Data;

import java.util.List;

@Data
public class BlueprintApplicationRequest {

    private Long applicationId;

    private List<String> roles;
}
