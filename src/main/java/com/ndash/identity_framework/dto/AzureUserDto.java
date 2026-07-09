package com.ndash.identity_framework.dto;

import lombok.Data;

@Data
public class AzureUserDto {

    private String id;
    private String displayName;
    private String mail;
    private String userPrincipalName;
    private Boolean accountEnabled;
}
