package com.ndash.identity_framework.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component
public class AzureUpnGenerator {

    private static final int MAX_LOCAL_PART_LENGTH = 64;
    private static final int MAX_SUFFIX_ATTEMPTS = 99;

    @Value("${azure.upn-domain:NETORGFT16179011.onmicrosoft.com}")
    private String upnDomain;

    public String getUpnDomain() {
        return upnDomain;
    }

    public String buildUpn(String localPart) {
        return localPart + "@" + upnDomain;
    }

    public String sanitize(String input) {
        if (input == null || input.isBlank()) {
            return "";
        }
        return input.toLowerCase()
                .trim()
                .replaceAll("[^a-z0-9._-]", "")
                .replaceAll("[._-]+", ".")
                .replaceAll("^\\.+|\\.+$", "");
    }

    public List<String> candidateBases(String email, String firstName, String lastName, Long userId) {
        Set<String> candidates = new LinkedHashSet<>();

        if (email != null && email.contains("@")) {
            String emailLocal = sanitize(email.split("@", 2)[0]);
            if (!emailLocal.isBlank()) {
                candidates.add(emailLocal);
            }
        }

        String first = sanitizeNamePart(firstName);
        String last = sanitizeNamePart(lastName);
        String nameBased = first + "." + last;
        if (!nameBased.equals(".")) {
            candidates.add(nameBased);
        }

        if (userId != null) {
            candidates.add(nameBased + "." + userId);
        }

        return new ArrayList<>(candidates);
    }

    public String truncate(String localPart) {
        if (localPart.length() <= MAX_LOCAL_PART_LENGTH) {
            return localPart;
        }
        return localPart.substring(0, MAX_LOCAL_PART_LENGTH);
    }

    public int getMaxSuffixAttempts() {
        return MAX_SUFFIX_ATTEMPTS;
    }

    private String sanitizeNamePart(String part) {
        String sanitized = sanitize(part);
        return sanitized.isBlank() ? "user" : sanitized;
    }
}
