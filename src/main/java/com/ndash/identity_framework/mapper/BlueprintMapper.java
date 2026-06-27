package com.ndash.identity_framework.mapper;

import com.ndash.identity_framework.domain.Blueprint;
import com.ndash.identity_framework.domain.BlueprintApplicationRole;
import com.ndash.identity_framework.dto.BlueprintApplicationResponse;
import com.ndash.identity_framework.dto.BlueprintResponse;
import com.ndash.identity_framework.dto.IdNameDto;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class BlueprintMapper {

    public BlueprintResponse toResponse(
            Blueprint blueprint
    ) {

        // =====================================
        // GROUP APPLICATION ROLES
        // =====================================

        Map<Long, List<BlueprintApplicationRole>>
                groupedApps =
                blueprint.getApplicationRoles()
                        .stream()
                        .collect(Collectors.groupingBy(
                                mapping ->
                                        mapping.getApplication()
                                                .getId()
                        ));

        // =====================================
        // BUILD APPLICATION RESPONSE
        // =====================================

        List<BlueprintApplicationResponse>
                applications =
                groupedApps.values()
                        .stream()
                        .map(group -> {

                            BlueprintApplicationRole first =
                                    group.get(0);

                            return BlueprintApplicationResponse
                                    .builder()
                                    .applicationId(
                                            first.getApplication()
                                                    .getId()
                                    )
                                    .applicationName(
                                            first.getApplication()
                                                    .getName()
                                    )
                                    .essential(first.getApplication().getEssential())
                                    .roles(
                                            group.stream()
                                                    .map(
                                                            BlueprintApplicationRole::getRoleName
                                                    )
                                                    .distinct()
                                                    .toList()
                                    )
                                    .build();
                        })
                        .toList();

        // =====================================
        // FINAL RESPONSE
        // =====================================

        return BlueprintResponse.builder()
                .id(blueprint.getId())
                .name(blueprint.getName())

                .jobTitles(
                        blueprint.getJobTitles()
                                .stream()
                                .map(jt ->
                                        new IdNameDto(
                                                jt.getId(),
                                                jt.getName()
                                        )
                                )
                                .toList()
                )

                .applications(applications)

                .build();
    }
}