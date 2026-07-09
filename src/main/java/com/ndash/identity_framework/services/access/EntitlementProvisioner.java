package com.ndash.identity_framework.services.access;

public interface EntitlementProvisioner {

    void provisionForUser(final Long userId);

    void provisionForBlueprintAssignment(final Long userId, final Long blueprintId);
}
