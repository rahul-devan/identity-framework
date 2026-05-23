package com.ndash.identity_framework.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class BlueprintResponse {

    private Long id;
    private String name;

    private List<IdNameDto> jobTitles;
    private List<BlueprintApplicationResponse> applications;
}
