package com.booking.common.entity;

import com.booking.common.enums.UserRole;

public class User {
    private String name;
    private String email;
    private String role;

    public User() {}

    public String getName() { return name; }
    public String getEmail() { return email; }
    public UserRole getRole() { return UserRole.valueOf(role); }

    @Override
    public String toString() {
        return name + " (" + email + ", " + role + ")";
    }
}
