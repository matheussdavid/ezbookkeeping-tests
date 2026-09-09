package com.ezbookkeeping.qa.tests.api;

import com.ezbookkeeping.qa.api.client.AuthClient;
import com.ezbookkeeping.qa.core.TestBase;
import com.ezbookkeeping.qa.fixtures.TestUsers;
import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;

@Tag("rate-limit")
@Execution(ExecutionMode.SAME_THREAD)
public class LoginRateLimitTest extends TestBase {

    private final AuthClient auth = new AuthClient();

    @BeforeAll
    static void prepararUsuarioRateLimit() {
        new AuthClient().registerRaw(
                TestUsers.rateLimitUsername(),
                "rate-limit@teste.com",
                TestUsers.rateLimitPassword())
                .extract();
    }

    @Test
    @DisplayName("CT-007 - Bloquear o usuario temporariamente apos varias tentativas de login com senha errada")
    public void deveBloquearAcessoAposExcessivasTentativasFalhas() {
        String mensagemBloqueio = "failure count exceeded maximum limit";
        int tentativa = 0;
        ValidatableResponse response = null;

        while (tentativa < 20) {
            tentativa++;
            response = auth.loginRaw(TestUsers.rateLimitUsername(), "wrong" + tentativa);
            String erro = response.extract().jsonPath().getString("errorMessage");
            if (mensagemBloqueio.equals(erro)) {
                break;
            }
        }

        assertThat(response).isNotNull();
        response.statusCode(400)
                .body("errorMessage", equalTo(mensagemBloqueio));
    }
}