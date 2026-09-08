package com.ezbookkeeping.qa.tests.ui;

import com.ezbookkeeping.qa.config.AppConfig;
import com.ezbookkeeping.qa.utils.DriverFactory;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Disabled;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("ui")
@Tag("smoke")
public class LoginUiTest {

    private static final By RESUMO_ATIVOS =
            By.xpath("//span[normalize-space()='Resumo de Ativos']");

    private WebDriver driver;

    @BeforeEach
    void setUp() {
        driver = DriverFactory.createChrome();
        driver.get(AppConfig.UI_URL + "/desktop#/login");
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    private void preencherCredenciais(String username, String password) {
        var wait = DriverFactory.wait(driver);

        wait.until(ExpectedConditions.elementToBeClickable(
                        By.cssSelector("input[autocomplete='username']")))
                .clear();
        driver.findElement(By.cssSelector("input[autocomplete='username']"))
                .sendKeys(username);

        driver.findElement(By.cssSelector("input[type='password']")).clear();
        driver.findElement(By.cssSelector("input[type='password']"))
                .sendKeys(password);
    }

    private void clicarLogin() {
        var wait = DriverFactory.wait(driver);
        wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[normalize-space()='Fazer Login']")));
        driver.findElement(By.xpath("//button[normalize-space()='Fazer Login']")).click();
    }

    @Test
    @DisplayName("CT-001 - Login com credenciais validas")
    void loginComCredenciaisValidas() {
        var wait = DriverFactory.wait(driver);

        preencherCredenciais(AppConfig.USERNAME, AppConfig.PASSWORD);
        clicarLogin();

        var resumo = wait.until(ExpectedConditions.visibilityOfElementLocated(RESUMO_ATIVOS));
        assertThat(resumo.isDisplayed()).isTrue();
        assertThat(driver.getCurrentUrl())
                .contains("desktop#/")
                .doesNotContain("/login");
    }

    @Test
    @DisplayName("CT-002 - Enter no campo senha submete o login")
    void loginComCredenciaisValidasEApertarEnter() {
        var wait = DriverFactory.wait(driver);

        preencherCredenciais(AppConfig.USERNAME, AppConfig.PASSWORD);
        driver.findElement(By.cssSelector("input[type='password']")).sendKeys(Keys.ENTER);

        var resumo = wait.until(ExpectedConditions.visibilityOfElementLocated(RESUMO_ATIVOS));
        assertThat(resumo.isDisplayed()).isTrue();
        assertThat(driver.getCurrentUrl())
                .contains("desktop#/")
                .doesNotContain("/login");
    }

    @Test
    @DisplayName("CT-003 - Tentar fazer login com campo Username vazio")
    void loginComCampoUsernameVazio() {
        var wait = DriverFactory.wait(driver);

        preencherCredenciais("", AppConfig.PASSWORD);
        driver.findElement(By.cssSelector("input[type='password']")).sendKeys(Keys.ENTER);

        var snackbar = wait.until(ExpectedConditions.
                visibilityOfElementLocated(
                        By.xpath("//div[@role='status']")));

        assertThat(snackbar.isDisplayed());
        assertThat(snackbar.getText())
                .containsIgnoringCase("O nome de usuário não pode estar em branco");
        assertThat(driver.getCurrentUrl()).contains("#/login");

    }

    @Test
    @DisplayName("CT-004 - Tentar fazer login com campo senha vazio")
    void loginComCampoPasswordVazio() {
        var wait = DriverFactory.wait(driver);

        preencherCredenciais(AppConfig.USERNAME, "");
        driver.findElement(By.cssSelector("input[type='password']")).sendKeys(Keys.ENTER);

        var snackbar = wait.until(ExpectedConditions.
                visibilityOfElementLocated(
                        By.xpath("//div[@role='status']")));

        assertThat(snackbar.isDisplayed());
        assertThat(snackbar.getText())
                .containsIgnoringCase("A senha não pode estar em branco");
        assertThat(driver.getCurrentUrl()).contains("#/login");

    }

    @Test
    @DisplayName("CT-005 - O Botão 'Fazer Login' deve estar desabilitado quando os campos de login estão vazios")
    void loginBotaoLoginDesabilitadoQuandoCamposVazios() {
        var wait = DriverFactory.wait(driver);

        var loginButton = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//button[normalize-space()='Fazer Login']")));

        assertThat(loginButton.isEnabled()).isFalse();
    }

    @Test
    @DisplayName("CT-006 - Login com senha errada exibe mensagem de erro")
    void loginComSenhaErrada() {
        var wait = DriverFactory.wait(driver);

        preencherCredenciais(AppConfig.USERNAME, "senha_errada_123");
        clicarLogin();

        var snackbar = wait.until(ExpectedConditions.
                visibilityOfElementLocated(
                        By.xpath("//div[@role='status']")));

                                assertThat(snackbar.getText())
                                        .containsIgnoringCase("Nome de login ou senha está errado");

        assertThat(driver.getCurrentUrl()).contains("#/login");
    }

@Test
    @DisplayName("CT-007 - Duplo clique no botao Log In")
    void loginComDuploCliqueNoBotaoLogIn() {
        var wait = DriverFactory.wait(driver);

        preencherCredenciais(AppConfig.USERNAME, AppConfig.PASSWORD);
        var botao = wait.until(ExpectedConditions.
                elementToBeClickable(By.xpath("//button[normalize-space()='Fazer Login']")));
        new Actions(driver).doubleClick(botao).perform();

        var resumo = wait.until(ExpectedConditions.visibilityOfElementLocated(RESUMO_ATIVOS));
        assertThat(resumo.isDisplayed()).isTrue();
        assertThat(driver.getCurrentUrl())
                .contains("desktop#/")
                .doesNotContain("/login");
    }

    @Test
    @DisplayName("CT-008 - Link 'Forget Password?' navega para o fluxo de recuperacao de senha")
    void loginLinkForgetPassword() {
        var wait = DriverFactory.wait(driver);

        var link = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("a[href$='forgetpassword']")));

        assertThat(link.getText()).containsIgnoringCase("esqueceu a senha");

        link.click();

        wait.until(ExpectedConditions.urlContains("#/forgetpassword"));
        assertThat(driver.getCurrentUrl()).contains("#/forgetpassword");
    }

    @Test
    @DisplayName("CT-009 - Link 'Create an account' navega para /signup")
    void loginLinkCreateAccount() {
        var wait = DriverFactory.wait(driver);

        var link = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("a[href$='signup']")));

        assertThat(link.getText()).containsIgnoringCase("criar uma conta");

        link.click();

        wait.until(ExpectedConditions.urlContains("#/signup"));
        assertThat(driver.getCurrentUrl()).contains("#/signup");
    }

    @Test
    @Disabled("Nao testavel no momento — fluxo de email nao verificado depende de enableUserForceVerifyEmail ativo e usuario com email nao verificado; essa configuracao do servidor nao e controlavel no ambiente de teste.")
    @DisplayName("CT-010 - Email nao verificado — redireciona para /verify_email")
    void loginComEmailNaoVerificado() {
        var wait = DriverFactory.wait(driver);

        preencherCredenciais(AppConfig.USERNAME, AppConfig.PASSWORD);
        clicarLogin();

        wait.until(ExpectedConditions.urlContains("/verify_email"));

        assertThat(driver.getCurrentUrl()).contains("#/verify_email");
    }
}
