package com.ndash.identity_framework.util;

public class CommonUtil {

    public static String maskSSN(String ssn) {
        if (ssn == null || ssn.length() < 4) return "****";
        return "XXX-XX-" + ssn.substring(ssn.length() - 4);
    }

    public static String generateNoIcon(String firstName, String lastName) {
        String firstInitial = (firstName != null && !firstName.isEmpty()) ? firstName.substring(0, 1).toUpperCase() : "X";
        String lastInitial = (lastName != null && !lastName.isEmpty()) ? lastName.substring(0, 1).toUpperCase() : "X";
        return firstInitial + lastInitial;
    }
}
