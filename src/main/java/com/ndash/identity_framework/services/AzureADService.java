package com.ndash.identity_framework.services;

import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.microsoft.graph.authentication.TokenCredentialAuthProvider;
import com.microsoft.graph.models.PasswordProfile;
import com.microsoft.graph.models.User;
import com.microsoft.graph.requests.GraphServiceClient;
import okhttp3.Request;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class AzureADService {

    private final GraphServiceClient<Request> graphClient;
    private final AzureUpnGenerator upnGenerator;

    public AzureADService(@Value("${azure.client-id}") String clientId,
                          @Value("${azure.client-secret}") String clientSecret,
                          @Value("${azure.tenant-id}") String tenantId,
                          AzureUpnGenerator upnGenerator) {

        this.upnGenerator = upnGenerator;

        ClientSecretCredential credential = new ClientSecretCredentialBuilder()
                .clientId(clientId)
                .clientSecret(clientSecret)
                .tenantId(tenantId)
                .build();

        graphClient = GraphServiceClient
                .builder()
                .authenticationProvider(new TokenCredentialAuthProvider(Collections.singletonList("https://graph.microsoft.com/.default"), credential))
                .buildClient();
    }

    public List<User> getAllUsers() {
        return graphClient.users().buildRequest().get().getCurrentPage();
    }

    public User createUser(String firstName, String lastName, String mail, Long userId) {
        String upnLocalPart = generateUniqueUpnLocalPart(mail, firstName, lastName, userId);
        String upn = upnGenerator.buildUpn(upnLocalPart);

        User user = new User();
        user.displayName = firstName + " " + lastName;
        user.givenName = firstName;
        user.surname = lastName;
        user.mailNickname = upnLocalPart;
        user.userPrincipalName = upn;
        user.accountEnabled = true;
        user.passwordProfile = new PasswordProfile();
        user.mail = mail;
        user.passwordProfile.password = "StrongPassword123!";
        user.passwordProfile.forceChangePasswordNextSignIn = true;

        return graphClient.users().buildRequest().post(user);
    }

    public String generateUniqueUpnLocalPart(String email, String firstName, String lastName, Long userId) {
        for (String base : upnGenerator.candidateBases(email, firstName, lastName, userId)) {
            String candidate = upnGenerator.truncate(base);
            if (!upnLocalPartExists(candidate)) {
                return candidate;
            }

            for (int suffix = 2; suffix <= upnGenerator.getMaxSuffixAttempts(); suffix++) {
                String suffixed = upnGenerator.truncate(base + suffix);
                if (!upnLocalPartExists(suffixed)) {
                    return suffixed;
                }
            }
        }

        throw new IllegalStateException("Could not generate unique UPN for user " + userId);
    }

    public boolean upnLocalPartExists(String localPart) {
        String upn = upnGenerator.buildUpn(localPart);
        List<User> users = graphClient.users()
                .buildRequest()
                .filter("userPrincipalName eq '" + escapeODataValue(upn) + "'")
                .get()
                .getCurrentPage();

        return !users.isEmpty();
    }

    public void deleteUser(String userId) {
        graphClient.users(userId).buildRequest().delete();
    }

    public List<com.microsoft.graph.models.Group> getAllGroups() {
        return graphClient.groups().buildRequest().get().getCurrentPage();
    }

    public com.microsoft.graph.models.User getUserByEmail(String email) {
        List<com.microsoft.graph.models.User> users = graphClient.users()
                .buildRequest()
                .filter("mail eq '" + escapeODataValue(email) + "'")
                .get()
                .getCurrentPage();

        return users.isEmpty() ? null : users.get(0);
    }

    public void updateUser(String azureId, String firstName, String lastName, String phone) {

        User user = new User();

        user.givenName = firstName;
        user.surname = lastName;
        user.mobilePhone = phone;

        graphClient.users(azureId).buildRequest()
                .patch(user);
    }

    private String escapeODataValue(String value) {
        return value.replace("'", "''");
    }
}
