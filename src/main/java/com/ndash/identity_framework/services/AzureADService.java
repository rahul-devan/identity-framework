package com.ndash.identity_framework.services;

import com.microsoft.graph.models.Group;
import com.microsoft.graph.models.User;
import com.microsoft.graph.requests.GraphServiceClient;
import com.microsoft.graph.requests.GroupCollectionPage;
import com.microsoft.graph.requests.GroupCollectionRequestBuilder;
import com.microsoft.graph.requests.UserCollectionPage;
import com.microsoft.graph.requests.UserCollectionRequestBuilder;
import okhttp3.Request;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.microsoft.graph.authentication.TokenCredentialAuthProvider;
import com.microsoft.graph.models.PasswordProfile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class AzureADService {

    private final GraphServiceClient<Request> graphClient;

    public AzureADService(@Value("${azure.client-id}") final String clientId,
                          @Value("${azure.client-secret}") final String clientSecret,
                          @Value("${azure.tenant-id}") final String tenantId) {

        final ClientSecretCredential credential = new ClientSecretCredentialBuilder()
                .clientId(clientId)
                .clientSecret(clientSecret)
                .tenantId(tenantId)
                .build();

        graphClient = GraphServiceClient
                .builder()
                .authenticationProvider(new TokenCredentialAuthProvider(
                        Collections.singletonList("https://graph.microsoft.com/.default"), credential))
                .buildClient();
    }

    public List<User> getAllUsers() {
        final List<User> allUsers = new ArrayList<>();
        UserCollectionPage page = graphClient.users().buildRequest().get();
        while (page != null) {
            allUsers.addAll(page.getCurrentPage());
            final UserCollectionRequestBuilder next = page.getNextPage();
            page = next != null ? next.buildRequest().get() : null;
        }
        return allUsers;
    }

    public User createUser(final String displayName, final String mail) {
        final User user = new User();
        user.displayName = displayName;
        user.mailNickname = mail.split("@")[0];
        user.userPrincipalName = displayName + "@NETORGFT16179011.onmicrosoft.com";
        user.accountEnabled = true;
        user.passwordProfile = new PasswordProfile();
        user.mail = mail;
        user.passwordProfile.password = "StrongPassword123!";
        user.passwordProfile.forceChangePasswordNextSignIn = true;

        return graphClient.users().buildRequest().post(user);
    }

    public void deleteUser(final String userId) {
        graphClient.users(userId).buildRequest().delete();
    }

    public List<Group> getAllGroups() {
        final List<Group> allGroups = new ArrayList<>();
        GroupCollectionPage page = graphClient.groups().buildRequest().get();
        while (page != null) {
            allGroups.addAll(page.getCurrentPage());
            final GroupCollectionRequestBuilder next = page.getNextPage();
            page = next != null ? next.buildRequest().get() : null;
        }
        return allGroups;
    }

    public User getUserByEmail(final String email) {
        final String sanitizedEmail = email.replace("'", "''");
        final List<User> users = graphClient.users()
                .buildRequest()
                .filter("mail eq '" + sanitizedEmail + "'")
                .get()
                .getCurrentPage();

        return users.isEmpty() ? null : users.get(0);
    }

    public void updateUser(final String azureId, final String firstName,
                           final String lastName, final String phone) {
        final User user = new User();
        user.givenName = firstName;
        user.surname = lastName;
        user.mobilePhone = phone;
        graphClient.users(azureId).buildRequest().patch(user);
    }
}
