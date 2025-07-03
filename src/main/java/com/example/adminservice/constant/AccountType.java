package com.example.adminservice.constant;

import lombok.Getter;

@Getter
public enum AccountType {
    SOCIAL("SOCIAL"),
    LOCAL("LOCAL");

    private final String value;

    AccountType(String value) {
        this.value = value;
    }

    public static AccountType fromValue(String value) {
        for (AccountType type : AccountType.values()) {
            if (type.value.equals(value.toUpperCase())) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid AccountType value: " + value);
    }
}
