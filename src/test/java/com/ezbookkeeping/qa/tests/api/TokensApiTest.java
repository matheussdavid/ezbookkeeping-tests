package com.ezbookkeeping.qa.tests.api;

import com.ezbookkeeping.qa.api.client.AuthClient;
import com.ezbookkeeping.qa.config.AppConfig;
import com.ezbookkeeping.qa.core.TestBase;
import com.ezbookkeeping.qa.fixtures.TestUsers;
import io.restassured.RestAssured;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

@Tag("api")
@Execution(ExecutionMode.SAME_THREAD)
public class TokensApiTest extends TestBase {

    private final AuthClient auth = new AuthClient();

    @Test
    @Tag("contract")
    @Tag("smoke")
    @DisplayName("CT-001 - Contrato do response de GET /api/v1/tokens/list.json")
    public void deveValidarContratoDoTokensList() {
        String token = auth.login(TestUsers.mainUsername(), TestUsers.mainPassword())
                .getResult().getToken();

        RestAssured.given()
                .header("Authorization", "Bearer " + token)
                .header("X-Timezone-Name", AppConfig.DEFAULT_TIMEZONE)
                .when()
                .get("/api/v1/tokens/list.json")
                .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/v1/tokens-list-response.json"));
    }

    @Test
    @Tag("smoke")
    @DisplayName("CT-002 - Token valido concede acesso a endpoints autenticados")
    public void deveConcederAcessoAEndpointAutenticadoComTokenValido() {
        String token = auth.login(TestUsers.mainUsername(), TestUsers.mainPassword())
                .getResult().getToken();

        var response = RestAssured.given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/api/v1/tokens/list.json")
                .then()
                .extract();

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body().jsonPath().getBoolean("success")).isTrue();
        Object result = response.body().jsonPath().get("result");
        assertThat(result).isNotNull();
    }
}