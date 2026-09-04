package com.ezbookkeeping.qa.api.client;

import com.ezbookkeeping.qa.config.AppConfig;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;

import java.util.Map;

public class AuthClient {

    public ValidatableResponse login(String username, String password) {
        return RestAssured.given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "loginName", username,
                        "password", password
                ))
                .when()
                .post("/api/authorize.json")
                .then();
    }

    public ValidatableResponse loginWithDefaults() {
        return login(AppConfig.USERNAME, AppConfig.PASSWORD);
    }

    public ValidatableResponse register(String username, String email, String password) {
        return RestAssured.given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "username", username,
                        "email", email,
                        "password", password,
                        "language", AppConfig.DEFAULT_LANGUAGE,
                        "defaultCurrency", AppConfig.DEFAULT_CURRENCY,
                        "firstDayOfWeek", 1
                ))
                .when()
                .post("/api/register.json")
                .then();
    }

    public ValidatableResponse registerWithDefaults() {
        return register(AppConfig.USERNAME, AppConfig.EMAIL, AppConfig.PASSWORD);
    }

    public String getTokenFromLogin(String username, String password) {
        return login(username, password)
                .statusCode(200)
                .extract()
                .jsonPath()
                .getString("result.token");
    }

    public String getTokenFromLoginWithDefaults() {
        return getTokenFromLogin(AppConfig.USERNAME, AppConfig.PASSWORD);
    }

    public String getTokenFromRegister(String username, String email, String password) {
        return register(username, email, password)
                .statusCode(200)
                .extract()
                .jsonPath()
                .getString("result.token");
    }
}
