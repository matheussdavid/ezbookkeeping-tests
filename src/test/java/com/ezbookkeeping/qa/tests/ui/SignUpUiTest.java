package com.ezbookkeeping.qa.tests.ui;

import com.ezbookkeeping.qa.api.client.AuthClient;
import com.ezbookkeeping.qa.api.model.RegisterRequest;
import com.ezbookkeeping.qa.config.AppConfig;
import com.ezbookkeeping.qa.ui.driver.DriverFactory;
import com.ezbookkeeping.qa.ui.pages.HomePage;
import com.ezbookkeeping.qa.ui.pages.LoginPage;
import com.ezbookkeeping.qa.ui.pages.SignUpPage;
import com.ezbookkeeping.qa.utils.UserFaker;
import io.restassured.RestAssured;
import org.junit.jupiter.api.*;
import org.openqa.selenium.WebDriver;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("ui")
@Tag("smoke")
public class SignUpUiTest {

    private final AuthClient auth = new AuthClient();

    private WebDriver driver;
    private SignUpPage signup;

    @BeforeAll
    static void configurarRestAssured() {
        RestAssured.baseURI = AppConfig.BASE_URL;
    }

    @BeforeEach
    void setUp() {
        driver = DriverFactory.createChrome();
        new LoginPage(driver).abrir().clicarCriarConta();
        signup = new SignUpPage(driver);
        signup.esperarUrlContendo("#/signup");
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @DisplayName("CT-001 - Cadastro com sucesso e login implicito")
    void devePreencherFluxoPrincipalDeCadastroDeUsuario() {
        String senha = UserFaker.password();
        signup.preencherUsername(UserFaker.username())
                .preencherNickname(UserFaker.nickname())
                .preencherEmail(UserFaker.email())
                .preencherSenha(senha)
                .preencherConfirmarSenha(senha);

        signup.selecionarIdioma("Português (Brasil)");
        signup.selecionarMoedaPadrao("CNY");
        signup.selecionarPrimeiroDiaDaSemana("Domingo");

        assertThat(signup.getTextoIdiomaSelecionado()).isEqualTo("Português (Brasil)");
        assertThat(signup.getTextoMoedaSelecionada()).isEqualTo("Yuan Chinês");
        assertThat(signup.getTextoPrimeiroDiaSelecionado()).isEqualTo("Domingo");

        signup.clicarProximo();
        signup.marcarUsarCategoriasPredefinidas();
        assertThat(signup.isBotaoAnteriorHabilitado()).isTrue();
        assertThat(signup.isUsarCategoriasPredefinidasMarcado()).isTrue();

        signup.clicarEnviar();

        HomePage home = new HomePage(driver);
        assertThat(home.isResumoDeAtivosVisivel()).isTrue();
        assertThat(home.getUrl())
                .contains("desktop#/")
                .doesNotContain("/login");
    }

    @Test
    @DisplayName("CT-002 - Password diferente da confirmacao")
    void  devePreencherConfirmarSenhaComValorDiferenteDeSenha() {
        signup.preencherUsername(UserFaker.username())
                .preencherNickname(UserFaker.nickname())
                .preencherEmail(UserFaker.email())
                .preencherSenha(UserFaker.password())
                .preencherConfirmarSenha("Teste123");

        signup.clicarProximo();

        assertThat(signup.temSnackbar()).isTrue();
        assertThat(signup.getMensagemErro())
                .containsIgnoringCase("A senha e a confirmação da senha não coincidem");
    }

    @Test
    @DisplayName("CT-003 - Campos obrigatorios em branco mostram mensagens em sequencia")
    void deveExibirMensagensDeCamposObrigatoriosEmOrdem() {
        String senha = UserFaker.password();

        signup.clicarProximo();
        signup.esperarSnackbarComMensagem("O nome de usuário não pode estar em branco");

        signup.preencherUsername(UserFaker.username()).clicarProximo();
        signup.esperarSnackbarComMensagem("A senha não pode estar em branco");

        signup.preencherSenha(senha).clicarProximo();
        signup.esperarSnackbarComMensagem("A confirmação da senha não pode estar em branco");

        signup.preencherConfirmarSenha(senha).clicarProximo();
        signup.esperarSnackbarComMensagem("O endereço de e-mail não pode estar em branco");

        signup.preencherEmail(UserFaker.email()).clicarProximo();
        signup.esperarSnackbarComMensagem("O nome de exibição não pode estar em branco");

        signup.preencherNickname(UserFaker.nickname()).clicarProximo();

        // POST /api/register.json so dispara no botao "Enviar" do segundo passo, nunca acionado aqui
        assertThat(signup.isUsarCategoriasPredefinidasMarcado()).isFalse();
    }

    @Test
    @DisplayName("CT-005a - Cadastro via UI com username ja em uso exibe conflito")
    void deveExibirConflitoQuandoUsernameJaExiste() {
        RegisterRequest existente = UserFaker.randomRegister();
        auth.register(existente);

        String senha = UserFaker.password();
        signup.preencherUsername(existente.getUsername())
                .preencherNickname(UserFaker.nickname())
                .preencherEmail(UserFaker.email())
                .preencherSenha(senha)
                .preencherConfirmarSenha(senha);

        signup.clicarProximo().clicarEnviar();

        signup.esperarSnackbarComMensagem("O nome de usuário já existe");
        assertThat(signup.getUrl()).contains("#/signup");
    }

    @Test
    @DisplayName("CT-005b - Cadastro via UI com email ja em uso exibe conflito")
    void deveExibirConflitoQuandoEmailJaExiste() {
        RegisterRequest existente = UserFaker.randomRegister();
        auth.register(existente);

        String senha = UserFaker.password();
        signup.preencherUsername(UserFaker.username())
                .preencherNickname(UserFaker.nickname())
                .preencherEmail(existente.getEmail())
                .preencherSenha(senha)
                .preencherConfirmarSenha(senha);

        signup.clicarProximo().clicarEnviar();

        signup.esperarSnackbarComMensagem("O e-mail já existe");
        assertThat(signup.getUrl()).contains("#/signup");
    }

    @Test
    @DisplayName("CT-006 - Troca de idioma atualiza moeda e primeiro dia da semana")
    void deveAlterarMoedaEPrimeiroDiaAoAlterarAMoeda() {
        assertThat(signup.getTextoIdiomaSelecionado()).isEqualTo("Português (Brasil)");
        assertThat(signup.getTextoMoedaSelecionada()).isEqualTo("Real Brasileiro");
        assertThat(signup.getTextoPrimeiroDiaSelecionado()).isEqualTo("Segunda-feira");

        signup.selecionarIdioma("English");

        assertThat(signup.getTextoIdiomaSelecionado()).isEqualTo("English");
        assertThat(signup.getTextoMoedaSelecionada()).isEqualTo("United States Dollar");
        assertThat(signup.getTextoPrimeiroDiaSelecionado()).isEqualTo("Sunday");
    }
}
