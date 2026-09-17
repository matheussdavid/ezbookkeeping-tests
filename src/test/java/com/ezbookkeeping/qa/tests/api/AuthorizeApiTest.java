package com.ezbookkeeping.qa.tests.api;

import com.ezbookkeeping.qa.api.client.AuthClient;
import com.ezbookkeeping.qa.api.model.ApiResponse;
import com.ezbookkeeping.qa.api.model.AuthResponse;
import com.ezbookkeeping.qa.core.TestBase;
import com.ezbookkeeping.qa.fixtures.TestUsers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import static org.assertj.core.api.Assertions.assertThat;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

@Tag("api")
@Execution(ExecutionMode.SAME_THREAD)
public class AuthorizeApiTest extends TestBase {

    private final AuthClient auth = new AuthClient();

    @Test
    @Tag("contract")
    @Tag("smoke")
    @DisplayName("CT-001 - Contrato do response de sucesso do POST /api/authorize.json")
    public void deveValidarContratoDeSucessoDoAuthorize() {
        auth.loginRaw(TestUsers.mainUsername(), TestUsers.mainPassword())
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/auth/authorize-response.json"));
    }

    @Test
    @Tag("contract")
    @DisplayName("CT-002 - Contrato do response de erro do POST /api/authorize.json")
    public void deveValidarContratoDeErroDoAuthorize() {
        auth.loginRaw(TestUsers.mainUsername(), "senhaErrada")
                .statusCode(401)
                .body(matchesJsonSchemaInClasspath("schemas/common/error-response.json"));
    }

    @Test
    @Tag("smoke")
    @DisplayName("CT-003 - Login com username e senha válidos, retorna token e success = true")
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
    @DisplayName("CT-004 - Login com email e senha válidos, retorna token e success = true")
    public void deveAutenticarUsuarioComEmailValido() {
        ApiResponse<AuthResponse> response =
                auth.login(TestUsers.mainEmail(), TestUsers.mainPassword());

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getResult().getToken()).isNotBlank();
        assertThat(response.getResult().getUser().getUsername())
                .isEqualTo(TestUsers.mainUsername());
    }

    @Test
    @DisplayName("CT-005 - Login com senha errada retorna 401")
    public void deveRejeitarLoginComSenhaIncorreta() {
        var response = auth.loginRaw(TestUsers.mainUsername(), "12131415")
                .extract();

        assertThat(response.statusCode()).isEqualTo(401);
        assertThat(response.body().jsonPath().getString("errorMessage"))
                .isEqualTo("login name or password is wrong");
        assertThat(response.body().jsonPath().getBoolean("success")).isFalse();
    }

    @Test
    @DisplayName("CT-006 - Login com campos obrigatorios vazios")
    public void deveRejeitarLoginComCamposObrigatoriosVazios() {
        var response = auth.loginRaw("", "")
                .extract();

        assertThat(response.statusCode()).isEqualTo(401);
        assertThat(response.body().jsonPath().getString("errorMessage"))
                .isEqualTo("login name or password is invalid");
        assertThat(response.body().jsonPath().getBoolean("success")).isFalse();
    }

    @Test
    @DisplayName("CT-007 - Senha não retornada em claro na resposta")
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
    @DisplayName("CT-008 - Login passando username em formato invalido")
    public void deveRejeitarLoginComUsernameEmFormatoInvalido() {
        var response = auth.loginRaw("user teste", "123456")
                .extract();

        assertThat(response.statusCode()).isEqualTo(401);
        assertThat(response.body().jsonPath().getString("errorMessage"))
                .isEqualTo("login name or password is invalid");
        assertThat(response.body().jsonPath().getBoolean("success")).isFalse();
    }
}