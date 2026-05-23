package com.ndash.identity_framework.dto;

import lombok.Data;

import java.util.List;

@Data
public class BlueprintRequest {

    private String name;

    private List<Long> jobTitleIds;

    private List<BlueprintApplicationRequest> applications;
}
