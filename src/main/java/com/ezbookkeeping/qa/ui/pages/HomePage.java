package com.ezbookkeeping.qa.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class HomePage extends BasePage {

    private static final By RESUMO_ATIVOS = By.xpath("//span[normalize-space()='Resumo de Ativos']");

    public HomePage(WebDriver driver) {
        super(driver);
    }

    public boolean isResumoDeAtivosVisivel() {
        return esperarVisivel(RESUMO_ATIVOS).isDisplayed();
    }
}