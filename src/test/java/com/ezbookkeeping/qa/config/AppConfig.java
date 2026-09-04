package com.ezbookkeeping.qa.config;

import io.github.cdimascio.dotenv.Dotenv;

public final class AppConfig {

    private static final Dotenv dotenv = Dotenv.configure()
            .ignoreIfMissing()
            .load();

    public static final String BASE_URL = get("BASE_URL", "http://localhost:8080");
    public static final String UI_URL = get("UI_URL", "http://localhost:8080");
    public static final String USERNAME = get("USER_USERNAME", "tester");
    public static final String PASSWORD = get("USER_PASSWORD", "senha1234");
    public static final String EMAIL = get("USER_EMAIL", "qa@teste.com");
    public static final String DEFAULT_CURRENCY = get("DEFAULT_CURRENCY", "BRL");
    public static final String DEFAULT_LANGUAGE = get("DEFAULT_LANGUAGE", "pt_BR");

    private AppConfig() {
    }

    private static String get(String key, String fallback) {
        String env = System.getenv(key);
        if (env != null && !env.isBlank()) {
            return env;
        }
        try {
            return dotenv.get(key, fallback);
        } catch (Exception e) {
            return fallback;
        }
    }
}
