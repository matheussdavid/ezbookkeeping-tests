package com.ezbookkeeping.qa.ui.pages;

import com.ezbookkeeping.qa.ui.driver.DriverFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public abstract class BasePage {

    protected final WebDriver driver;
    protected final WebDriverWait wait;

    protected BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = DriverFactory.wait(driver);
    }

    public String getUrl() {
        return driver.getCurrentUrl();
    }

    public void esperarUrlContendo(String fragmento) {
        wait.until(ExpectedConditions.urlContains(fragmento));
    }

    protected WebElement esperarVisivel(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected WebElement esperarClicavel(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    protected void clicar(By locator) {
        esperarClicavel(locator).click();
    }

    protected void preencher(By locator, String texto) {
        WebElement campo = esperarClicavel(locator);
        campo.clear();
        campo.sendKeys(texto);
    }

    protected boolean isMarcado(By locator) {
        return wait.until(ExpectedConditions.
                presenceOfElementLocated(locator)).isSelected();
    }

    protected boolean isHabilitado(By locator) {
        return esperarVisivel(locator).isEnabled();
    }
}