package com.ezbookkeeping.qa.tests.api;

import com.ezbookkeeping.qa.api.client.AuthClient;
import com.ezbookkeeping.qa.api.model.ApiError;
import com.ezbookkeeping.qa.api.model.ApiResponse;
import com.ezbookkeeping.qa.api.model.CategoryDraft;
import com.ezbookkeeping.qa.api.model.RegisterRequest;
import com.ezbookkeeping.qa.api.model.RegisterResponse;
import com.ezbookkeeping.qa.config.AppConfig;
import com.ezbookkeeping.qa.core.TestBase;
import com.ezbookkeeping.qa.fixtures.TestUsers;
import com.ezbookkeeping.qa.utils.UserFaker;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import java.util.List;
import java.util.stream.Stream;

@Tag("api")
@Execution(ExecutionMode.SAME_THREAD)
public class RegisterApiTest extends TestBase {

    private final AuthClient auth = new AuthClient();

    @Test
    @Tag("contract")
    @Tag("smoke")
    @DisplayName("CT-001 - Contrato do response de sucesso do POST /api/register.json")
    public void deveValidarContratoDeSucessoDoRegister() {
        auth.registerRaw(UserFaker.randomRegister())
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/auth/register-response.json"));
    }

    @Test
    @Tag("contract")
    @DisplayName("CT-002 - Contrato do response de erro do POST /api/register.json")
    public void deveValidarContratoDeErroDoRegister() {
        auth.registerRaw(
                        TestUsers.mainUsername(),
                        UserFaker.email(),
                        AppConfig.PASSWORD)
                .statusCode(400)
                .body(matchesJsonSchemaInClasspath("schemas/common/error-response.json"));
    }

    @Test
    @Tag("smoke")
    @DisplayName("CT-003 - Cadastro com sucesso (happy path)")
    public void deveCadastrarUsuarioComSucesso() {
        RegisterRequest request = UserFaker.randomRegister();

        ApiResponse<RegisterResponse> response = auth.register(request);

        assertThat(response.isSuccess()).isTrue();
        RegisterResponse result = response.getResult();
        assertThat(result.getToken()).isNotBlank();
        assertThat(result.isNeed2FA()).isFalse();
        assertThat(result.isNeedVerifyEmail()).isFalse();
        assertThat(result.isPresetCategoriesSaved()).isFalse();
        assertThat(result.getUser().getUsername()).isEqualTo(request.getUsername());
        assertThat(result.getUser().getEmail()).isEqualTo(request.getEmail());
        assertThat(result.getUser().getNickname()).isEqualTo(request.getNickname());
        assertThat(result.getUser().getLanguage()).isEqualTo(AppConfig.DEFAULT_LANGUAGE);
        assertThat(result.getUser().getDefaultCurrency()).isEqualTo(AppConfig.DEFAULT_CURRENCY);
        assertThat(result.getUser().isEmailVerified()).isFalse();
    }

    @Test
    @DisplayName("CT-004 - Token autentica usuario recem-criado em /api/v1")
    public void deveAutenticarTokenDoUsuarioRecemCriado() {
        String token = auth.register(UserFaker.randomRegister()).getResult().getToken();

        var response = RestAssured.given()
                .header("Authorization", "Bearer " + token)
                .header("X-Timezone-Name", AppConfig.DEFAULT_TIMEZONE)
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
    @DisplayName("CT-005 - Cadastro com username duplicado")
    public void deveRejeitarCadastroComUsernameJaCadastrado() {
        RegisterRequest request = new RegisterRequest(
                TestUsers.mainUsername(),
                UserFaker.nickname(),
                UserFaker.email(),
                UserFaker.password(),
                AppConfig.DEFAULT_LANGUAGE,
                AppConfig.DEFAULT_CURRENCY,
                1);

        assertErro(auth.registerError(request), 201012, "username already exists");
    }

    @Test
    @DisplayName("CT-005 - Cadastro com username que difere apenas por caixa")
    public void deveCadastrarUsuarioComUsernameDiferindoApenasPorCaixa() {
        String base = UserFaker.username();

        ApiResponse<RegisterResponse> minusculo = auth.register(new RegisterRequest(
                base,
                UserFaker.nickname(),
                UserFaker.email(),
                UserFaker.password(),
                AppConfig.DEFAULT_LANGUAGE,
                AppConfig.DEFAULT_CURRENCY,
                1));
        assertThat(minusculo.isSuccess()).isTrue();
        assertThat(minusculo.getResult().getUser().getUsername()).isEqualTo(base);

        ApiResponse<RegisterResponse> maiusculo = auth.register(new RegisterRequest(
                base.toUpperCase(),
                UserFaker.nickname(),
                UserFaker.email(),
                UserFaker.password(),
                AppConfig.DEFAULT_LANGUAGE,
                AppConfig.DEFAULT_CURRENCY,
                1));
        assertThat(maiusculo.isSuccess()).isTrue();
        assertThat(maiusculo.getResult().getUser().getUsername()).isEqualTo(base.toUpperCase());
    }

    @Test
    @DisplayName("CT-006 - Cadastro com email duplicado")
    public void deveRejeitarCadastroComEmailJaCadastrado() {
        RegisterRequest request = new RegisterRequest(
                UserFaker.username(),
                UserFaker.nickname(),
                TestUsers.mainEmail(),
                UserFaker.password(),
                AppConfig.DEFAULT_LANGUAGE,
                AppConfig.DEFAULT_CURRENCY,
                1);

        assertErro(auth.registerError(request), 201013, "email already exists");
    }

    @Test
    @DisplayName("CT-007 - Moeda padrao invalida")
    public void deveRejeitarMoedaPadraoInvalida() {
        RegisterRequest request = new RegisterRequest(
                UserFaker.username(),
                UserFaker.nickname(),
                UserFaker.email(),
                UserFaker.password(),
                AppConfig.DEFAULT_LANGUAGE,
                "ABC",
                1);

        assertErro(auth.registerError(request), 200000,
                "parameter \"defaultCurrency\" is invalid currency");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("cenariosCampoObrigatorio")
    @DisplayName("CT-009 - Campos obrigatorios ausentes")
    public void deveRejeitarCadastroComCampoObrigatorioAusenteOuVazio(String cenario, RegisterRequest request, String mensagemEsperada) {
        assertErro(auth.registerError(request), 200000, mensagemEsperada);
    }

    private static Stream<Arguments> cenariosCampoObrigatorio() {
        String u = UserFaker.username();
        String n = UserFaker.nickname();
        String e = UserFaker.email();
        String p = UserFaker.password();
        String lang = AppConfig.DEFAULT_LANGUAGE;
        String cur = AppConfig.DEFAULT_CURRENCY;

        return Stream.of(
                Arguments.of("username ausente", request(null, n, e, p, lang, cur),
                        "parameter \"username\" is required"),
                Arguments.of("username vazio", request("", n, e, p, lang, cur),
                        "parameter \"username\" is required"),
                Arguments.of("email ausente", request(u, n, null, p, lang, cur),
                        "parameter \"email\" is required"),
                Arguments.of("email vazio", request(u, n, "", p, lang, cur),
                        "parameter \"email\" is required"),
                Arguments.of("nickname ausente", request(u, null, e, p, lang, cur),
                        "parameter \"nickname\" is required"),
                Arguments.of("nickname vazio", request(u, "", e, p, lang, cur),
                        "parameter \"nickname\" is required"),
                Arguments.of("password ausente", request(u, n, e, null, lang, cur),
                        "parameter \"password\" is required"),
                Arguments.of("password vazio", request(u, n, e, "", lang, cur),
                        "parameter \"password\" is required"),
                Arguments.of("language ausente", request(u, n, e, p, null, cur),
                        "parameter \"language\" is required"),
                Arguments.of("language vazio", request(u, n, e, p, "", cur),
                        "parameter \"language\" is required"),
                Arguments.of("defaultCurrency ausente", request(u, n, e, p, lang, null),
                        "parameter \"defaultCurrency\" is required"),
                Arguments.of("defaultCurrency vazio", request(u, n, e, p, lang, ""),
                        "parameter \"defaultCurrency\" is required"));
    }

    private static RegisterRequest request(String username, String nickname, String email,
                                           String password, String language, String defaultCurrency) {
        return new RegisterRequest(username, nickname, email, password, language, defaultCurrency, 1);
    }

    @Test
    @DisplayName("CT-011 - Senha com menos de 6 caracteres")
    public void deveRejeitarCadastroComSenhaInvalida() {
        RegisterRequest request = new RegisterRequest(
                UserFaker.username(),
                UserFaker.nickname(),
                UserFaker.email(),
                "12345",
                AppConfig.DEFAULT_LANGUAGE,
                AppConfig.DEFAULT_CURRENCY,
                1);

        assertErro(auth.registerError(request), 200000,
                "parameter \"password\" must be more than 6 characters");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("cenariosUsernameInvalido")
    @DisplayName("CT-012 - Formato de username invalido")
    public void deveRejeitarCadastroComUsernameFormatoInvalido(String cenario, RegisterRequest request, String mensagemEsperada) {
        assertErro(auth.registerError(request), 200000, mensagemEsperada);
    }

    private static Stream<Arguments> cenariosUsernameInvalido() {
        String n = UserFaker.nickname();
        String e = UserFaker.email();
        String p = UserFaker.password();
        String lang = AppConfig.DEFAULT_LANGUAGE;
        String cur = AppConfig.DEFAULT_CURRENCY;

        return Stream.of(
                Arguments.of("espaco em branco", request("user name", n, e, p, lang, cur),
                        "parameter \"username\" is invalid username format"),
                Arguments.of("caractere especial @", request("user@name", n, e, p, lang, cur),
                        "parameter \"username\" is invalid username format"),
                Arguments.of("acentuacao", request("usu\u00e1rio", n, e, p, lang, cur),
                        "parameter \"username\" is invalid username format"),
                Arguments.of("33 caracteres", request("a".repeat(33), n, e, p, lang, cur),
                        "parameter \"username\" must be less than 32 characters"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("cenariosEmailInvalido")
    @DisplayName("CT-013 - Formato de email invalido")
    public void deveRejeitarCadastroComEmailFormatoInvalido(String cenario, RegisterRequest request, String mensagemEsperada) {
        assertErro(auth.registerError(request), 200000, mensagemEsperada);
    }

    private static Stream<Arguments> cenariosEmailInvalido() {
        String u = UserFaker.username();
        String n = UserFaker.nickname();
        String p = UserFaker.password();
        String lang = AppConfig.DEFAULT_LANGUAGE;
        String cur = AppConfig.DEFAULT_CURRENCY;

        return Stream.of(
                Arguments.of("sem arroba", request(u, n, "usuario.example.com", p, lang, cur),
                        "parameter \"email\" is invalid email format"),
                Arguments.of("sem dominio", request(u, n, "usuario@", p, lang, cur),
                        "parameter \"email\" is invalid email format"),
                Arguments.of("102 caracteres", request(u, n, "a".repeat(90) + "@example.com", p, lang, cur),
                        "parameter \"email\" must be less than 100 characters"));
    }

    @Test
    @DisplayName("CT-014 - Trim de nickname no cadastro")
    public void deveAplicarTrimNoNicknameAoCadastrar() {
        RegisterRequest request = new RegisterRequest(
                UserFaker.username(),
                "  Novo Usuario  ",
                UserFaker.email(),
                UserFaker.password(),
                AppConfig.DEFAULT_LANGUAGE,
                AppConfig.DEFAULT_CURRENCY,
                1);

        ApiResponse<RegisterResponse> response = auth.register(request);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getResult().getUser().getNickname()).isEqualTo("Novo Usuario");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("cenariosTrimRejeitado")
    @DisplayName("CT-014 - Campos com espacos rejeitados antes do trim")
    public void deveRejeitarUsernameEmailComEspacos(String cenario, RegisterRequest request, String mensagemEsperada) {
        assertErro(auth.registerError(request), 200000, mensagemEsperada);
    }

    private static Stream<Arguments> cenariosTrimRejeitado() {
        String n = UserFaker.nickname();
        String p = UserFaker.password();
        String lang = AppConfig.DEFAULT_LANGUAGE;
        String cur = AppConfig.DEFAULT_CURRENCY;

        return Stream.of(
                Arguments.of("username com espacos", request("  novo_user  ", n, UserFaker.email(), p, lang, cur),
                        "parameter \"username\" is invalid username format"),
                Arguments.of("email com espacos", request(UserFaker.username(), n, "  novo@example.com  ", p, lang, cur),
                        "parameter \"email\" is invalid email format"));
    }

    @Test
    @DisplayName("CT-015 - Senha cadastrada autentica login pos-cadastro")
    public void devePermitirLoginComSenhaCadastrada() {
        String senha = UserFaker.password();
        RegisterRequest request = new RegisterRequest(
                UserFaker.username(),
                UserFaker.nickname(),
                UserFaker.email(),
                senha,
                AppConfig.DEFAULT_LANGUAGE,
                AppConfig.DEFAULT_CURRENCY,
                1);

        String registerBody = auth.registerRaw(request)
                .statusCode(200)
                .extract()
                .body()
                .asString();

        assertThat(registerBody).doesNotContain(senha);

        ExtractableResponse<Response> login = auth.loginRaw(request.getUsername(), senha).extract();
        assertThat(login.statusCode()).isEqualTo(200);
        assertThat(login.body().jsonPath().getBoolean("success")).isTrue();
        assertThat(login.body().jsonPath().getString("result.token")).isNotBlank();
    }

    @Test
    @DisplayName("CT-016 - firstDayOfWeek fora do intervalo 0-6")
    public void deveRejeitarCadastroComFirstDayOfWeekInvalido() {
        RegisterRequest request = new RegisterRequest(
                UserFaker.username(),
                UserFaker.nickname(),
                UserFaker.email(),
                UserFaker.password(),
                AppConfig.DEFAULT_LANGUAGE,
                AppConfig.DEFAULT_CURRENCY,
                10);

        assertErro(auth.registerError(request), 200000,
                "parameter \"firstDayOfWeek\" must be less than 6");
    }

    @Test
    @DisplayName("CT-017 - Categorias iniciais em lote")
    public void deveSalvarCategoriasIniciaisEmLote() {
        RegisterRequest request = new RegisterRequest(
                UserFaker.username(),
                UserFaker.nickname(),
                UserFaker.email(),
                UserFaker.password(),
                AppConfig.DEFAULT_LANGUAGE,
                AppConfig.DEFAULT_CURRENCY,
                1);
        request.setCategories(List.of(
                new CategoryDraft("Alimentacao", 2, "1", "ff6b22",
                        List.of(new CategoryDraft("Supermercado", 2, "2", "ff6b22", List.of()),
                                new CategoryDraft("Restaurantes", 2, "70", "ff6b22", List.of()))),
                new CategoryDraft("Transporte", 2, "300", "009688",
                        List.of(new CategoryDraft("Transporte Publico", 2, "310", "009688", List.of()))),
                new CategoryDraft("Salario", 1, "2000", "ff6b22",
                        List.of(new CategoryDraft("Renda Principal", 1, "2010", "ff6b22", List.of())))));

        ApiResponse<RegisterResponse> response = auth.register(request);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getResult().isPresetCategoriesSaved()).isTrue();

        String token = response.getResult().getToken();
        String categoriesBody = RestAssured.given()
                .header("Authorization", "Bearer " + token)
                .header("X-Timezone-Name", AppConfig.DEFAULT_TIMEZONE)
                .when()
                .get("/api/v1/transaction/categories/list.json")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .asString();

        assertThat(categoriesBody).contains("Alimentação", "Transporte", "Salário");
    }

    @Test
    @DisplayName("CT-018 - Cadastro com valores no limite maximo")
    public void deveCadastrarUsuarioComValoresNoLimiteMaximo() {
        String username = UserFaker.lowercaseAlpha(32);
        String email = UserFaker.lowercaseAlpha(88) + "@example.com";
        String nickname = UserFaker.alphanumeric(64);
        String password = UserFaker.alphanumeric(128);
        String language = UserFaker.lowercaseAlpha(16);

        ApiResponse<RegisterResponse> response = auth.register(new RegisterRequest(
                username, nickname, email, password, language,
                AppConfig.DEFAULT_CURRENCY, 1));

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getResult().getUser().getUsername()).isEqualTo(username);
        assertThat(response.getResult().getUser().getNickname()).isEqualTo(nickname);
        assertThat(response.getResult().getUser().getEmail()).isEqualTo(email);
        assertThat(response.getResult().getUser().getLanguage()).isEqualTo(language);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("cenariosAcimaDoLimite")
    @DisplayName("CT-018 - Valores acima do limite maximo")
    public void deveRejeitarValoresAcimaDoLimite(String cenario, RegisterRequest request, String mensagemEsperada) {
        assertErro(auth.registerError(request), 200000, mensagemEsperada);
    }

    private static Stream<Arguments> cenariosAcimaDoLimite() {
        String language = AppConfig.DEFAULT_LANGUAGE;
        String currency = AppConfig.DEFAULT_CURRENCY;
        return Stream.of(
                Arguments.of("username 33", request(
                        UserFaker.lowercaseAlpha(33), UserFaker.nickname(), UserFaker.email(), UserFaker.password(), language, currency),
                        "parameter \"username\" must be less than 32 characters"),
                Arguments.of("email 101", request(
                        UserFaker.username(), UserFaker.nickname(), UserFaker.lowercaseAlpha(89) + "@example.com", UserFaker.password(), language, currency),
                        "parameter \"email\" must be less than 100 characters"),
                Arguments.of("nickname 65", request(
                        UserFaker.username(), UserFaker.alphanumeric(65), UserFaker.email(), UserFaker.password(), language, currency),
                        "parameter \"nickname\" must be less than 64 characters"),
                Arguments.of("password 129", request(
                        UserFaker.username(), UserFaker.nickname(), UserFaker.email(), UserFaker.alphanumeric(129), language, currency),
                        "parameter \"password\" must be less than 128 characters"),
                Arguments.of("language 17", request(
                        UserFaker.username(), UserFaker.nickname(), UserFaker.email(), UserFaker.password(), UserFaker.lowercaseAlpha(17), currency),
                        "parameter \"language\" must be less than 16 characters"));
    }

    private void assertErro(ApiError erro, int code, String mensagem) {
        assertThat(erro.isSuccess()).isFalse();
        assertThat(erro.getErrorCode()).isEqualTo(code);
        assertThat(erro.getErrorMessage()).isEqualTo(mensagem);
    }
}