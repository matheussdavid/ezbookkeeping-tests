package com.ezbookkeeping.qa.utils;

import com.ezbookkeeping.qa.api.model.RegisterRequest;
import com.ezbookkeeping.qa.config.AppConfig;
import net.datafaker.Faker;

public final class UserFaker {

    private static final Faker FAKER = new Faker();

    private UserFaker() {
    }

    public static String username() {
        return "user_" + FAKER.regexify("[a-z0-9]{6}");
    }

    public static String email() {
        return FAKER.internet().emailAddress();
    }

    public static String password() {
        return FAKER.internet().password(8, 16, true, true);
    }

    public static String nickname() { return FAKER.name().name(); }

    public static String lowercaseAlpha(int length) {
        return FAKER.regexify("[a-z]{" + length + "}");
    }

    public static String alphanumeric(int length) {
        return FAKER.regexify("[a-zA-Z0-9]{" + length + "}");
    }

    public static RegisterRequest randomRegister() {
        return new RegisterRequest(
                username(),
                nickname(),
                email(),
                password(),
                AppConfig.DEFAULT_LANGUAGE,
                AppConfig.DEFAULT_CURRENCY,
                1
        );
    }
}