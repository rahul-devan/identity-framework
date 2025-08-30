package com.ndash.identity_framework.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ndash.identity_framework.domain.User;
import com.ndash.identity_framework.dto.ApiResponse;
import com.ndash.identity_framework.dto.LoginRequest;
import com.ndash.identity_framework.dto.UserDto;
import com.ndash.identity_framework.mapper.UserMapper;
import com.ndash.identity_framework.repositories.UserRepository;
import com.ndash.identity_framework.services.AuthService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

@Service
public class AuthServiceImpl implements AuthService {

    @Value("${azure.tenant-id}")
    private String tenantId;

    @Value("${azure.client-id}")
    private String clientId;

    @Value("${azure.client-secret}")
    private String clientSecret;

    @Value("${azure.scope:openid profile offline_access}")
    private String scope;

    private final UserRepository userRepository;
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }


    @Override
    public ApiResponse<UserDto> authenticate(Jwt jwt) {
        String azureId = jwt.getClaimAsString("oid");
        String email = jwt.getClaimAsString("upn");
        String name = jwt.getClaimAsString("name");

        User user = userRepository.findByAzureId(azureId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "User not registered in local system"
                ));

        if (!user.isActive()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is deactivated");
        }

        UserDto dto = UserMapper.toDto(user);
        return ApiResponse.success(dto, HttpStatus.OK.value());
    }

    @Override
    public ApiResponse<UserDto> authenticate(LoginRequest loginRequest) {
        try {

            Optional<User> userOpt = userRepository.findByEmail(loginRequest.getUsername());

            if (userOpt.isEmpty() || !userOpt.get().getPassword().equals(loginRequest.getPassword())) {
                return ApiResponse.error("Invalid username or password", 401);
            }

            User user = userOpt.get();
            // Map user details (you could decode idToken for profile info)
            UserDto userDto = UserMapper.toDto(user);

            return ApiResponse.success(userDto, 200);
//            // Form data
//            String form = "client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8) +
//                    "&client_secret=" + URLEncoder.encode(clientSecret, StandardCharsets.UTF_8) +
//                    "&grant_type=password" +
//                    "&username=" + URLEncoder.encode(loginRequest.getUsername(), StandardCharsets.UTF_8) +
//                    "&password=" + URLEncoder.encode(loginRequest.getPassword(), StandardCharsets.UTF_8) +
//                    "&scope=" + URLEncoder.encode(scope, StandardCharsets.UTF_8);
//
//            HttpRequest httpRequest = HttpRequest.newBuilder()
//                    .uri(URI.create("https://login.microsoftonline.com/" + tenantId + "/oauth2/v2.0/token"))
//                    .header("Content-Type", "application/x-www-form-urlencoded")
//                    .POST(HttpRequest.BodyPublishers.ofString(form))
//                    .build();
//
//            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
//
//            if (response.statusCode() == 200) {
//                Map<String, Object> json = objectMapper.readValue(response.body(), Map.class);
//
//                String accessToken = (String) json.get("access_token");
//                String idToken = (String) json.get("id_token");
//
//                // Map user details (you could decode idToken for profile info)
//                UserDto userDto = UserMapper.toDto(user);
//
//                return ApiResponse.success(userDto, 200);
//            } else {
//                return ApiResponse.error("Invalid credentials", 401);
//            }

        } catch (Exception e) {
            return ApiResponse.error("Authentication error: " + e.getMessage(), 500);
        }
    }
}
