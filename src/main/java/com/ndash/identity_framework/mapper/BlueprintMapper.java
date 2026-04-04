package com.ndash.identity_framework.mapper;

import com.ndash.identity_framework.domain.Blueprint;
import com.ndash.identity_framework.dto.BlueprintResponse;
import com.ndash.identity_framework.dto.IdNameDto;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class BlueprintMapper {

    public BlueprintResponse toResponse(Blueprint blueprint) {

        return BlueprintResponse.builder()
                .id(blueprint.getId())
                .name(blueprint.getName())
                .jobTitles(
                        blueprint.getJobTitles().stream()
                                .map(jt -> new IdNameDto(jt.getId(), jt.getName()))
                                .collect(Collectors.toList())
                )
                .applications(
                        blueprint.getApplications().stream()
                                .map(app -> new IdNameDto(app.getId(), app.getName()))
                                .collect(Collectors.toList())
                )
                .build();
    }
}
