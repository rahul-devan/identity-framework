package com.ndash.identity_framework.util;

public class CommonUtil {

    public static String maskSSN(String ssn) {
        if (ssn == null || ssn.length() < 4) return "****";
        return "XXX-XX-" + ssn.substring(ssn.length() - 4);
    }
}
