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

    public AzureADService(@Value("${azure.client-id}") String clientId,
                          @Value("${azure.client-secret}") String clientSecret,
                          @Value("${azure.tenant-id}") String tenantId) {

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

    public User createUser(String displayName, String mail) {
        User user = new User();
        user.displayName = displayName;
        user.mailNickname = mail.split("@")[0];
        user.userPrincipalName = displayName + "@NETORGFT16179011.onmicrosoft.com";
        user.accountEnabled = true;
        user.passwordProfile = new PasswordProfile();
        user.passwordProfile.password = "Stro  ngPassword123!";
        user.passwordProfile.forceChangePasswordNextSignIn = true;

        return graphClient.users().buildRequest().post(user);
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
                .filter("mail eq '" + email + "'")
                .get()
                .getCurrentPage();

        return users.isEmpty() ? null : users.get(0);
    }


}

