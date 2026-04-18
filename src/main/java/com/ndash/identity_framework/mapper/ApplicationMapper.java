package com.ndash.identity_framework.mapper;

import com.ndash.identity_framework.domain.Application;
import com.ndash.identity_framework.dto.ApplicationDto;

public class ApplicationMapper {

    public static ApplicationDto toDto(Application app) {
        if (app == null) return null;
        return ApplicationDto.builder()
                .id(app.getId())
                .name(app.getName())
                .description(app.getDescription())
                .appUrl(app.getAppUrl())
                .integrationName(app.getIntegrationName())
                .active(app.isActive())
                .build();
    }
}

