package com.ezbookkeeping.qa.ui.pages;

import com.ezbookkeeping.qa.config.AppConfig;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

public class LoginPage extends BasePage {

    private static final By INPUT_USERNAME = By.cssSelector("input[autocomplete='username']");
    private static final By INPUT_PASSWORD = By.cssSelector("input[type='password']");
    private static final By BUTTON_LOGIN = By.xpath("//button[normalize-space()='Fazer Login']");
    private static final By SNACKBAR = By.xpath("//div[@role='status']");
    private static final By RESUMO_ATIVOS = By.xpath("//span[normalize-space()='Resumo de Ativos']");
    private static final By LINK_FORGET_PASSWORD = By.cssSelector("a[href$='forgetpassword']");
    private static final By LINK_CREATE_ACCOUNT = By.cssSelector("a[href$='signup']");

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    public LoginPage abrir() {
        driver.get(AppConfig.UI_URL + "/desktop#/login");
        return this;
    }

    public LoginPage preencherCredenciais(String username, String password) {
        preencher(INPUT_USERNAME, username);
        preencher(INPUT_PASSWORD, password);
        return this;
    }

    public LoginPage submeterComEnter() {
        driver.findElement(INPUT_PASSWORD).sendKeys(Keys.ENTER);
        return this;
    }

    public LoginPage clicarLogin() {
        clicar(BUTTON_LOGIN);
        return this;
    }

    public LoginPage duploCliqueLogin() {
        new Actions(driver).doubleClick(esperarClicavel(BUTTON_LOGIN)).perform();
        return this;
    }

    public boolean isBotaoLoginHabilitado() {
        return esperarVisivel(BUTTON_LOGIN).isEnabled();
    }

    public boolean isLogado() {
        esperarVisivel(RESUMO_ATIVOS);
        return true;
    }

    public boolean temSnackbar() {
        return esperarVisivel(SNACKBAR).isDisplayed();
    }

    public String getMensagemErro() {
        return esperarVisivel(SNACKBAR).getText();
    }

    public String getTextoLinkEsqueciSenha() {
        return esperarClicavel(LINK_FORGET_PASSWORD).getText();
    }

    public LoginPage clicarEsqueciSenha() {
        clicar(LINK_FORGET_PASSWORD);
        return this;
    }

    public String getTextoLinkCriarConta() {
        return esperarClicavel(LINK_CREATE_ACCOUNT).getText();
    }

    public LoginPage clicarCriarConta() {
        clicar(LINK_CREATE_ACCOUNT);
        return this;
    }
}