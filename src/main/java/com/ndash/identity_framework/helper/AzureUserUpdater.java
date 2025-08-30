package com.ndash.identity_framework.helper;

import com.ndash.identity_framework.domain.Role;
import com.ndash.identity_framework.domain.User;
import com.ndash.identity_framework.domain.UserRole;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Component
public class AzureUserUpdater {

    public boolean updateUserFromAzure(User user, com.microsoft.graph.models.User azureUser, Set<Role> assignedRoles) {
        boolean updated = false;

        updated |= setIfChanged(user::setUsername, user.getUsername(), azureUser.userPrincipalName);
        updated |= setIfChanged(user::setFirstName, user.getFirstName(), getFirstName(azureUser.displayName));
        updated |= setIfChanged(user::setLastName, user.getLastName(), getLastName(azureUser.displayName));
        updated |= setIfChanged(user::setPhoneNumber, user.getPhoneNumber(), azureUser.mobilePhone);
        updated |= setIfChanged(user::setEmail, user.getEmail(), azureUser.mail);

        if (!user.isActive()) {
            user.setActive(true);
            updated = true;
        }

        // Roles
        Set<Role> currentRoles = user.getUserRoles().stream()
                .map(UserRole::getRole)
                .collect(Collectors.toSet());

        if (!currentRoles.containsAll(assignedRoles)) {
            for (Role role : assignedRoles) {
                if (!currentRoles.contains(role)) {
                    UserRole ur = new UserRole();
                    ur.setUser(user);
                    ur.setRole(role);
                    user.getUserRoles().add(ur);
                    updated = true;
                }
            }
        }

        return updated;
    }

    private <T> boolean setIfChanged(Consumer<T> setter, T oldVal, T newVal) {
        if (!Objects.equals(oldVal, newVal)) {
            setter.accept(newVal);
            return true;
        }
        return false;
    }

    private String getFirstName(String displayName) {
        return (displayName != null && displayName.contains(" "))
                ? displayName.substring(0, displayName.indexOf(" "))
                : displayName;
    }

    private String getLastName(String displayName) {
        return (displayName != null && displayName.contains(" "))
                ? displayName.substring(displayName.indexOf(" ") + 1)
                : "";
    }
}
