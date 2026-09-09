package com.ezbookkeeping.qa.tests.api;

import com.ezbookkeeping.qa.api.client.AuthClient;
import com.ezbookkeeping.qa.api.model.ApiResponse;
import com.ezbookkeeping.qa.api.model.AuthResponse;
import com.ezbookkeeping.qa.core.TestBase;
import com.ezbookkeeping.qa.fixtures.TestUsers;
import io.restassured.RestAssured;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("api")
public class LoginApiTest extends TestBase {

    private final AuthClient auth = new AuthClient();

    @Test
    @Tag("smoke")
    @DisplayName("CT-001 - Login com username e senha válidos, retorna token e success = true")
    public void deveAutenticarUsuarioComCredenciaisValidas() {
        ApiResponse<AuthResponse> response =
                auth.login(TestUsers.mainUsername(), TestUsers.mainPassword());

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getResult().getToken()).isNotBlank();
        assertThat(response.getResult().getUser().getUsername())
                .isEqualTo(TestUsers.mainUsername());
    }

    @Test
    @Tag("smoke")
    @DisplayName("CT-002 - Login com email e senha válidos, retorna token e success = true")
    public void deveAutenticarUsuarioComEmailValido() {
        ApiResponse<AuthResponse> response =
                auth.login(TestUsers.mainEmail(), TestUsers.mainPassword());

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getResult().getToken()).isNotBlank();
        assertThat(response.getResult().getUser().getUsername())
                .isEqualTo(TestUsers.mainUsername());
    }

    @Test
    @Tag("smoke")
    @DisplayName("CT-003 - Login com senha errada retorna 401")
    public void deveRejeitarLoginComSenhaIncorreta() {
        var response = auth.loginRaw(TestUsers.mainUsername(), "12131415")
                .extract();

        assertThat(response.statusCode()).isEqualTo(401);
        assertThat(response.body().jsonPath().getString("errorMessage"))
                .isEqualTo("login name or password is wrong");
        assertThat(response.body().jsonPath().getBoolean("success")).isFalse();
    }

    @Test
    @Tag("smoke")
    @DisplayName("CT-004 - Login com campos obrigatorios vazios")
    public void deveRejeitarLoginComCamposObrigatoriosVazios() {
        var response = auth.loginRaw("", "")
                .extract();

        assertThat(response.statusCode()).isEqualTo(401);
        assertThat(response.body().jsonPath().getString("errorMessage"))
                .isEqualTo("login name or password is invalid");
        assertThat(response.body().jsonPath().getBoolean("success")).isFalse();
    }

    @Test
    @Tag("smoke")
    @DisplayName("CT-009 - Token valido concede acesso a endpoints autenticados")
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

    @Test
    @Tag("smoke")
    @DisplayName("CT-010 - Senha não retornada em claro na resposta")
    public void deveNaoRetornarSenhaEmClaroNaResposta() {
        var response = auth.loginRaw(TestUsers.mainUsername(), TestUsers.mainPassword())
                .extract();

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body().jsonPath().getBoolean("success")).isTrue();
        Object password = response.body().jsonPath().get("result.password");
        assertThat(password).isNull();
        Object passwordHash = response.body().jsonPath().get("result.passwordHash");
        assertThat(passwordHash).isNull();
    }

    @Test
    @DisplayName("CT-011 - Login passando username em formato invalido")
    public void deveRejeitarLoginComUsernameEmFormatoInvalido() {
        var response = auth.loginRaw("user teste", "123456")
                .extract();

        assertThat(response.statusCode()).isEqualTo(401);
        assertThat(response.body().jsonPath().getString("errorMessage"))
                .isEqualTo("login name or password is invalid");
        assertThat(response.body().jsonPath().getBoolean("success")).isFalse();
    }
}