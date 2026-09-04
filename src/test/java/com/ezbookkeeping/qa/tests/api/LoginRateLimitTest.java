package com.ezbookkeeping.qa.tests.api;

import com.ezbookkeeping.qa.api.client.AuthClient;
import com.ezbookkeeping.qa.config.AppConfig;
import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.equalTo;

@Tag("rate-limit")
public class LoginRateLimitTest {

    private final AuthClient auth = new AuthClient();

    @Test
    @DisplayName("CT-007 - Bloquear o usuario temporariamente apos varias tentativas de login com senha errada")
    public void bloquearUsuarioComExcessivasTentativasDeLogin() {
        String mensagemBloqueio = "failure count exceeded maximum limit";
        int tentativa = 0;
        ValidatableResponse response = null;

        while (tentativa < 20) {
            tentativa++;
            response = auth.login(AppConfig.USERNAME, "wrong" + tentativa);
            String erro = response.extract().jsonPath().getString("errorMessage");
            if (mensagemBloqueio.equals(erro)) break;
        }

        response.statusCode(400)
                .body("errorMessage", equalTo(mensagemBloqueio));
    }
}