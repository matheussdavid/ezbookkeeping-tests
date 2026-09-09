package com.ezbookkeeping.qa.core;

import com.ezbookkeeping.qa.api.client.AuthClient;
import com.ezbookkeeping.qa.config.AppConfig;
import io.restassured.RestAssured;
import io.restassured.parsing.Parser;
import org.junit.jupiter.api.BeforeAll;

public abstract class TestBase {

    private static final AuthClient AUTH = new AuthClient();
    private static boolean initialized = false;

    @BeforeAll
    static synchronized void baseSetup() {
        if (initialized) {
            return;
        }

        RestAssured.baseURI = AppConfig.BASE_URL;
        RestAssured.defaultParser = Parser.JSON;
        RestAssured.authentication = RestAssured.oauth2(resolveToken());

        initialized = true;
    }

    private static String resolveToken() {
        if (AUTH.loginRaw(AppConfig.USERNAME, AppConfig.PASSWORD)
                .extract().statusCode() == 200) {
            return AUTH.getToken(AppConfig.USERNAME, AppConfig.PASSWORD);
        }

        AUTH.registerRaw(AppConfig.USERNAME, AppConfig.EMAIL, AppConfig.PASSWORD);

        return AUTH.getToken(AppConfig.USERNAME, AppConfig.PASSWORD);
    }
}