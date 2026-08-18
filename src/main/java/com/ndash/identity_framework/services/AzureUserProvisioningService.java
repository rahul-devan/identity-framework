package com.ndash.identity_framework.services;

import com.ndash.identity_framework.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AzureUserProvisioningService {

    private final AzureADService azureADService;
    private final UserRepository userRepository;

    @Async
    @Transactional
    public void provisionUserAsync(Long userId, String firstName, String lastName, String email) {
        try {
            com.microsoft.graph.models.User existingAzureUser = azureADService.getUserByEmail(email);
            String azureId;
            String userPrincipalName = null;

            if (existingAzureUser != null) {
                azureId = existingAzureUser.id;
                userPrincipalName = existingAzureUser.userPrincipalName;
                log.info("Azure user already exists for email {}, linking azureId {}", email, azureId);
            } else {
                com.microsoft.graph.models.User created =
                        azureADService.createUser(firstName, lastName, email, userId);
                if (created == null || created.id == null) {
                    log.error("Failed to create Azure user for email {}", email);
                    return;
                }
                azureId = created.id;
                userPrincipalName = created.userPrincipalName;
                log.info("Created Azure user for email {} with UPN {}", email, userPrincipalName);
            }

            final String linkedUpn = userPrincipalName;
            userRepository.findById(userId).ifPresent(user -> {
                user.setAzureId(azureId);
                if (linkedUpn != null) {
                    user.setUsername(linkedUpn);
                }
                userRepository.save(user);
                log.info("Linked Azure id {} to local user {} with username {}", azureId, userId, linkedUpn);
            });
        } catch (Exception ex) {
            log.error("Async Azure provisioning failed for user {} (email: {})", userId, email, ex);
        }
    }
}
