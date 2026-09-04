package com.ezbookkeeping.qa.tests.ui;

import com.ezbookkeeping.qa.config.AppConfig;
import com.ezbookkeeping.qa.utils.DriverFactory;
import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("ui")
@Tag("smoke")
public class LoginUiTest {

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
    @DisplayName("Login com credenciais válidas navega para a home")
    void loginComCredenciaisValidas() {
        var wait = DriverFactory.wait(driver);

        preencherCredenciais(AppConfig.USERNAME, AppConfig.PASSWORD);
        clicarLogin();

        wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("/desktop#/"),
                ExpectedConditions.urlContains("/desktop#/")
        ));

        assertThat(driver.getCurrentUrl()).contains("#/");
        assertThat(driver.findElements(
                By.cssSelector("input[autocomplete='username']"))).isEmpty();
    }

    @Test
    @DisplayName("Login com senha errada exibe mensagem de erro")
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
}
