package com.ndash.identity_framework.mapper;

import com.ndash.identity_framework.domain.JobTitle;
import com.ndash.identity_framework.dto.JobTitleResponse;
import org.springframework.stereotype.Component;

@Component
public class JobTitleMapper {

    public JobTitleResponse toResponse(JobTitle jobTitle) {
        return JobTitleResponse.builder()
                .id(jobTitle.getId())
                .name(jobTitle.getName())
                .externalSource(jobTitle.getExternalSource())
                .build();
    }
}