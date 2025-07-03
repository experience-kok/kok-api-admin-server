package com.example.adminservice.constant;

import lombok.Getter;

@Getter
public enum Provider {
    GOOGLE("GOOGLE"),
    FACEBOOK("FACEBOOK"),
    KAKAO("KAKAO"),
    NAVER("NAVER"),
    LOCAL("LOCAL");

    private final String value;

    Provider(String value) {
        this.value = value;
    }

    public static Provider fromValue(String value) {
        for (Provider provider : Provider.values()) {
            if (provider.value.equals(value.toUpperCase())) {
                return provider;
            }
        }
        throw new IllegalArgumentException("Invalid Provider value: " + value);
    }
}
