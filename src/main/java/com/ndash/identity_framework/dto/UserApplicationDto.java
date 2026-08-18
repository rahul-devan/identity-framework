package com.ndash.identity_framework.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserApplicationDto {

    private Long id;
    private Long applicationId;
    private String name;
    private String description;
    private String accessLevel;
    private String grantedDate;
    private boolean essential;
    private boolean active;

    public UserApplicationDto(
            Long id,
            Long applicationId,
            String name,
            String description,
            LocalDateTime assignedAt,
            boolean active
    ) {
        this.id = id;
        this.applicationId = applicationId;
        this.name = name;
        this.description = description;
        this.accessLevel = "Standard";
        this.grantedDate = assignedAt != null ? assignedAt.toString() : null;
        this.essential = name != null
                && (name.equalsIgnoreCase("Slack") || name.equalsIgnoreCase("Jira"));
        this.active = active;
    }
}