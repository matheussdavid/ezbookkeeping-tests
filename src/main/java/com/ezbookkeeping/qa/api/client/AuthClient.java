package com.ezbookkeeping.qa.api.client;

import com.ezbookkeeping.qa.api.model.ApiResponse;
import com.ezbookkeeping.qa.api.model.AuthResponse;
import com.ezbookkeeping.qa.api.model.RegisterRequest;
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

    public ApiResponse<AuthResponse> register(RegisterRequest request) {
        return registerRaw(request)
                .statusCode(200)
                .extract()
                .as(new TypeRef<>() {});
    }

    public ValidatableResponse registerRaw(RegisterRequest request) {
        return baseSpec()
                .body(request)
                .when()
                .post("/api/register.json")
                .then();
    }

    public ValidatableResponse registerRaw(String username, String email, String password) {
        return registerRaw(new RegisterRequest(
                username,
                username,
                email,
                password,
                AppConfig.DEFAULT_LANGUAGE,
                AppConfig.DEFAULT_CURRENCY,
                1
        ));
    }

    public String getToken(String username, String password) {
        return login(username, password).getResult().getToken();
    }
}