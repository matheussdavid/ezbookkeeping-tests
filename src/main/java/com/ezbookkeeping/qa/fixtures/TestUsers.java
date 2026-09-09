package com.ezbookkeeping.qa.fixtures;

import com.ezbookkeeping.qa.config.AppConfig;

public final class TestUsers {

    private TestUsers() {
    }

    public static String mainUsername() {
        return AppConfig.USERNAME;
    }

    public static String mainPassword() {
        return AppConfig.PASSWORD;
    }

    public static String mainEmail() {
        return AppConfig.EMAIL;
    }

    public static String rateLimitUsername() {
        return AppConfig.RATE_LIMIT_USERNAME;
    }

    public static String rateLimitPassword() {
        return AppConfig.RATE_LIMIT_PASSWORD;
    }
}