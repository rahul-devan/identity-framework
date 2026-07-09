package com.ndash.identity_framework.services.access;

import com.ndash.identity_framework.domain.*;
import com.ndash.identity_framework.repositories.BlueprintRepository;
import com.ndash.identity_framework.repositories.UserApplicationRepository;
import com.ndash.identity_framework.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EntitlementProvisionerImpl implements EntitlementProvisioner {

    private final UserRepository userRepository;
    private final BlueprintRepository blueprintRepository;
    private final UserApplicationRepository userApplicationRepository;

    @Override
    public void provisionForUser(final Long userId) {
        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        if (user.getBlueprint() == null) {
            return;
        }
        provisionBlueprintRoles(user, user.getBlueprint());
    }

    @Override
    public void provisionForBlueprintAssignment(final Long userId, final Long blueprintId) {
        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        final Blueprint blueprint = blueprintRepository.findById(blueprintId)
                .orElseThrow(() -> new IllegalArgumentException("Blueprint not found: " + blueprintId));
        user.setBlueprint(blueprint);
        userRepository.save(user);
        provisionBlueprintRoles(user, blueprint);
    }

    private void provisionBlueprintRoles(final User user, final Blueprint blueprint) {
        for (final BlueprintApplicationRole mapping : blueprint.getApplicationRoles()) {
            final Application application = mapping.getApplication();
            if (application == null) {
                continue;
            }

            final Optional<UserApplication> existing = userApplicationRepository
                    .findByUserIdAndApplicationId(user.getId(), application.getId());

            if (existing.isPresent()) {
                final UserApplication userApplication = existing.get();
                userApplication.setActive(true);
                userApplication.setRemovedAt(null);
                userApplicationRepository.save(userApplication);
            } else {
                final UserApplication userApplication = new UserApplication();
                userApplication.setUser(user);
                userApplication.setApplication(application);
                userApplication.setActive(true);
                userApplication.setAssignedAt(LocalDateTime.now());
                userApplicationRepository.save(userApplication);
            }

            log.info("[identity-framework] - ENTITLEMENT: provisioned app={} for userId={}",
                    application.getName(), user.getId());
        }
    }
}
