package com.example.adminservice.constant;

import lombok.Getter;

@Getter
public enum UserRole {
    USER("USER"),
    CLIENT("CLIENT"),
    ADMIN("ADMIN");

    private final String value;

    UserRole(String value) {
        this.value = value;
    }

    public static UserRole fromValue(String value) {
        for (UserRole role : UserRole.values()) {
            if (role.value.equals(value.toUpperCase())) {
                return role;
            }
        }
        throw new IllegalArgumentException("Invalid UserRole value: " + value);
    }
}
