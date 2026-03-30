package com.booking.common.enums;

import java.util.EnumSet;
import java.util.Set;

public enum UserRole {
    CUSTOMER(EnumSet.noneOf(Permission.class)),
    ADMIN(EnumSet.allOf(Permission.class));

    private final Set<Permission> permissions;

    UserRole(Set<Permission> permissions) {
        this.permissions = permissions;
    }

    public boolean hasPermission(Permission permission) {
        return permissions.contains(permission);
    }
}
