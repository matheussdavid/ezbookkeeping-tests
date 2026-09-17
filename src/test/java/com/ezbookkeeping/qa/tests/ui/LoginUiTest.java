package com.ezbookkeeping.qa.tests.ui;

import com.ezbookkeeping.qa.fixtures.TestUsers;
import com.ezbookkeeping.qa.ui.pages.HomePage;
import com.ezbookkeeping.qa.ui.pages.LoginPage;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("ui")
public class LoginUiTest extends BaseUiTest {

    private LoginPage abrirLogin() {
        return new LoginPage(driver).abrir();
    }

    @Test
    @Tag("smoke")
    @DisplayName("CT-001 - Login com credenciais validas")
    void deveAutenticarUsuarioComCredenciaisValidas() {
        LoginPage login = abrirLogin();

        login.preencherCredenciais(TestUsers.mainUsername(), TestUsers.mainPassword())
                .clicarLogin();

        HomePage home = new HomePage(driver);
        assertThat(home.isResumoDeAtivosVisivel()).isTrue();
        assertThat(home.getUrl())
                .contains("desktop#/")
                .doesNotContain("/login");
    }

    @Test
    @DisplayName("CT-002 - Enter no campo senha submete o login")
    void deveAutenticarUsuarioComCredenciaisValidasAoPressionarEnter() {
        LoginPage login = abrirLogin();

        login.preencherCredenciais(TestUsers.mainUsername(), TestUsers.mainPassword())
                .submeterComEnter();

        HomePage home = new HomePage(driver);
        assertThat(home.isResumoDeAtivosVisivel()).isTrue();
        assertThat(home.getUrl())
                .contains("desktop#/")
                .doesNotContain("/login");
    }

    @Test
    @DisplayName("CT-003 - Tentar fazer login com campo Username vazio")
    void deveRejeitarLoginComCampoUsuarioVazio() {
        LoginPage login = abrirLogin();

        login.preencherCredenciais("", TestUsers.mainPassword())
                .submeterComEnter();

        assertThat(login.temSnackbar()).isTrue();
        assertThat(login.getMensagemErro())
                .containsIgnoringCase("O nome de usuário não pode estar em branco");
        assertThat(login.getUrl()).contains("#/login");
    }

    @Test
    @DisplayName("CT-004 - Tentar fazer login com campo senha vazio")
    void deveRejeitarLoginComCampoSenhaVazio() {
        LoginPage login = abrirLogin();

        login.preencherCredenciais(TestUsers.mainUsername(), "")
                .submeterComEnter();

        assertThat(login.temSnackbar()).isTrue();
        assertThat(login.getMensagemErro())
                .containsIgnoringCase("A senha não pode estar em branco");
        assertThat(login.getUrl()).contains("#/login");
    }

    @Test
    @DisplayName("CT-005 - O Botão 'Fazer Login' deve estar desabilitado quando os campos de login estão vazios")
    void deveDesabilitarBotaoLoginQuandoCamposVazios() {
        LoginPage login = abrirLogin();

        assertThat(login.isBotaoLoginHabilitado()).isFalse();
    }

    @Test
    @DisplayName("CT-006 - Login com senha errada exibe mensagem de erro")
    void deveRejeitarLoginComSenhaIncorreta() {
        LoginPage login = abrirLogin();

        login.preencherCredenciais(TestUsers.mainUsername(), "senha_errada_123")
                .clicarLogin();

        assertThat(login.temSnackbar()).isTrue();
        assertThat(login.getMensagemErro())
                .containsIgnoringCase("Nome de login ou senha está errado");
        assertThat(login.getUrl()).contains("#/login");
    }

    @Test
    @DisplayName("CT-007 - Duplo clique no botao Log In")
    void deveAutenticarUsuarioComDuploCliqueNoBotaoLogin() {
        LoginPage login = abrirLogin();

        login.preencherCredenciais(TestUsers.mainUsername(), TestUsers.mainPassword())
                .duploCliqueLogin();

        HomePage home = new HomePage(driver);
        assertThat(home.isResumoDeAtivosVisivel()).isTrue();
        assertThat(home.getUrl())
                .contains("desktop#/")
                .doesNotContain("/login");
    }

    @Test
    @DisplayName("CT-008 - Link 'Forget Password?' navega para o fluxo de recuperacao de senha")
    void deveNavegarParaRecuperacaoDeSenhaAoClicarNoLink() {
        LoginPage login = abrirLogin();

        assertThat(login.getTextoLinkEsqueciSenha()).containsIgnoringCase("esqueceu a senha");

        login.clicarEsqueciSenha()
                .esperarUrlContendo("#/forgetpassword");

        assertThat(login.getUrl()).contains("#/forgetpassword");
    }

    @Test
    @DisplayName("CT-009 - Link 'Create an account' navega para /signup")
    void deveNavegarParaCriarContaAoClicarNoLink() {
        LoginPage login = abrirLogin();

        assertThat(login.getTextoLinkCriarConta()).containsIgnoringCase("criar uma conta");

        login.clicarCriarConta()
                .esperarUrlContendo("#/signup");

        assertThat(login.getUrl()).contains("#/signup");
    }

    @Test
    @Disabled("Nao testavel no momento — fluxo de email nao verificado depende de enableUserForceVerifyEmail ativo e usuario com email nao verificado; essa configuracao do servidor nao e controlavel no ambiente de teste.")
    @DisplayName("CT-010 - Email nao verificado — redireciona para /verify_email")
    void deveRedirecionarParaVerificacaoDeEmailComContaNaoVerificada() {
        LoginPage login = abrirLogin();

        login.preencherCredenciais(TestUsers.mainUsername(), TestUsers.mainPassword())
                .clicarLogin()
                .esperarUrlContendo("#/verify_email");

        assertThat(login.getUrl()).contains("#/verify_email");
    }
}