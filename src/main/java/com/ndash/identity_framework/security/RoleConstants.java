package com.ndash.identity_framework.security;

public final class RoleConstants {

    public static final String USER = "user";
    public static final String MANAGER = "manager";
    public static final String ADMINISTRATION = "administration";
    public static final String SUPER_ADMIN = "super_admin";

    public static final String ADMIN_AUTHORITIES =
            "hasAnyAuthority('" + ADMINISTRATION + "', '" + SUPER_ADMIN + "')";

    private RoleConstants() {
        throw new UnsupportedOperationException("This class should never be instantiated");
    }
}
