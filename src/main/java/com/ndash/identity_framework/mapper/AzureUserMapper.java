package com.ndash.identity_framework.mapper;

import com.microsoft.graph.models.User;
import com.ndash.identity_framework.dto.AzureUserDto;

public final class AzureUserMapper {

    private AzureUserMapper() {
        throw new UnsupportedOperationException("This class should never be instantiated");
    }

    public static AzureUserDto toDto(final User azureUser) {
        if (azureUser == null) {
            return null;
        }

        final AzureUserDto dto = new AzureUserDto();
        dto.setId(azureUser.id);
        dto.setDisplayName(azureUser.displayName);
        dto.setMail(azureUser.mail);
        dto.setUserPrincipalName(azureUser.userPrincipalName);
        dto.setAccountEnabled(azureUser.accountEnabled);
        return dto;
    }
}
