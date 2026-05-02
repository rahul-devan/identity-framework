package com.ndash.identity_framework.dto;

import lombok.Data;

@Data
public class UserApplicationDto {

    private Long id;
    private Long applicationId;
    private String name;
    private String description;
    private String accessLevel;
    private String grantedDate;
    private boolean essential;
    private boolean active;
}