package com.ezbookkeeping.qa.api.client;

import com.ezbookkeeping.qa.api.model.ApiResponse;
import com.ezbookkeeping.qa.api.model.AuthResponse;
import com.ezbookkeeping.qa.config.AppConfig;
import io.restassured.common.mapper.TypeRef;
import io.restassured.response.ValidatableResponse;

import java.util.Map;

public class AuthClient extends ClientBase {

    public ValidatableResponse loginRaw(String username, String password) {
        return baseSpec()
                .body(Map.of(
                        "loginName", username,
                        "password", password
                ))
                .when()
                .post("/api/authorize.json")
                .then();
    }

    public ApiResponse<AuthResponse> login(String username, String password) {
        return loginRaw(username, password)
                .statusCode(200)
                .extract()
                .as(new TypeRef<>() {});
    }

    public ValidatableResponse registerRaw(String username, String email, String password) {
        return baseSpec()
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

    public String getToken(String username, String password) {
        return login(username, password).getResult().getToken();
    }
}