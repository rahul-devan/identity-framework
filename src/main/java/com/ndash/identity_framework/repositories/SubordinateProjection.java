package com.ndash.identity_framework.repositories;

public interface SubordinateProjection {
    Long getManagerId();
    Long getId();
    String getFirstName();
    String getLastName();
    String getEmail();
    String getCompanyName();
    boolean getActive();
    String getManagerName();
}
