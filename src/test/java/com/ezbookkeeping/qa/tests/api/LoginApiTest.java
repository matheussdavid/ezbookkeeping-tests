package com.ezbookkeeping.qa.tests.api;

import com.ezbookkeeping.qa.api.client.AuthClient;
import com.ezbookkeeping.qa.config.AppConfig;
import io.restassured.RestAssured;
import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.*;


@Tag("api")
public class LoginApiTest {

    private final AuthClient auth = new AuthClient();

    //Fluxo principal: credenciais validas
    @Test
    @Tag("smoke")
    @DisplayName("CT-001 - Login com username e senha válidos, retorna token e sucess = true")
    public void loginComUsernameValido() {
        ValidatableResponse response = auth.login(AppConfig.USERNAME, AppConfig.PASSWORD);
        response
                .statusCode(200)
                .body("success", is(true))
                .body("result.token", not(emptyOrNullString()))
                .body("result.user.username", equalTo(AppConfig.USERNAME));
    }

    @Test
    @Tag("smoke")
    @DisplayName("CT-002 - Login com email e senha válidos, retorna token e sucess = true")
    public void loginComEmailValido() {
        ValidatableResponse response = auth.login(AppConfig.EMAIL, AppConfig.PASSWORD);
        response
                .statusCode(200)
                .body("success", is(true))
                .body("result.token", not(emptyOrNullString()))
                .body("result.user.username", equalTo(AppConfig.USERNAME));
    }

    //Fluxo principal: credenciais validas
    @Test
    @Tag("smoke")
    @DisplayName("CT-003 - Login com senha errada retorna 401")
    public void loginComSenhaErrada() {
        auth.login(AppConfig.USERNAME, "12131415")
                .statusCode(401)
                .body("errorMessage", equalTo("login name or password is wrong"))
                .body("success", is(false));
    }

    @Test
    @Tag("smoke")
    @DisplayName("CT-004 - Login com campos obrigatorios vazios")
    public void loginComCamposObrigatoriosVazios() {
        auth.login("", "")
                .statusCode(401)
                .body("errorMessage", equalTo("login name or password is invalid"))
                .body("success", is(false));
    }

    @Test
    @Tag("smoke")
    @DisplayName("CT-009 - Token valido concede acesso a endpoints autenticados")
    public void tokenValidoConcedeAcesso() {
        String token = auth.getTokenFromLoginWithDefaults();

        RestAssured.given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/api/v1/tokens/list.json")
                .then()
                .statusCode(200)
                .body("success", is(true))
                .body("result", is(notNullValue()));
    }

    @Test
    @Tag("smoke")
    @DisplayName("CT-010 - Senha nao retornada em claro na resposta")
    public void senhaNaoRetornadaEmClaro() {
        auth.login(AppConfig.USERNAME, AppConfig.PASSWORD)
                .statusCode(200)
                .body("success", is(true))
                .body("result.password", nullValue())
                .body("result.passwordHash", nullValue());
    }


    @Test
    @DisplayName("CT-011 - Login passando username em formato invalido")
    public void loginComUsernameEmFormatoInvalido() {
        auth.login("user teste", "123456")
                .statusCode(401)
                .body("errorMessage", equalTo("login name or password is invalid"))
                .body("success", is(false));
    }
}
