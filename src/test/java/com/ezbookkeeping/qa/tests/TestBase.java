package com.ezbookkeeping.qa.tests;

import com.ezbookkeeping.qa.api.client.AuthClient;
import com.ezbookkeeping.qa.config.AppConfig;
import io.restassured.RestAssured;
import io.restassured.parsing.Parser;
import org.junit.jupiter.api.BeforeAll;

public abstract class TestBase {

    private static boolean initialized = false;

    @BeforeAll
    static void baseSetup() {
        if (initialized) {
            return;
        }

        RestAssured.baseURI = AppConfig.BASE_URL;
        RestAssured.defaultParser = Parser.JSON;

        String token = tryLoginOrCreateUser();
        RestAssured.authentication = RestAssured.oauth2(token);

        initialized = true;
    }

    private static String tryLoginOrCreateUser() {
        AuthClient auth = new AuthClient();

        var loginResponse = auth.loginWithDefaults();
        if (loginResponse.extract().statusCode() == 200) {
            return loginResponse.extract().jsonPath().getString("result.token");
        }

        var registerResponse = auth.registerWithDefaults();
        if (registerResponse.extract().statusCode() == 200) {
            return registerResponse.extract().jsonPath().getString("result.token");
        }

        return auth.getTokenFromLoginWithDefaults();
    }
}
