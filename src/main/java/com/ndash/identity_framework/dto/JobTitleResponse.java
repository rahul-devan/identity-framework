package com.ndash.identity_framework.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JobTitleResponse {

    private Long id;
    private String name;
    private String externalSource;
}
