package com.ezbookkeeping.qa.utils;

import com.ezbookkeeping.qa.config.AppConfig;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public final class DriverFactory {

    private static final Duration EXPLICIT_WAIT = Duration.ofSeconds(15);

    private DriverFactory() {
    }

    public static WebDriver createChrome() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--lang=pt-BR");
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");
        return new ChromeDriver(options);
    }

    public static WebDriver createFirefox() {
        WebDriverManager.firefoxdriver().setup();
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        options.addArguments("--no-sandbox");
        return new FirefoxDriver(options);
    }

    public static WebDriver create(String browser) {
        return switch (browser.toLowerCase()) {
            case "firefox", "ff" -> createFirefox();
            default -> createChrome();
        };
    }

    public static WebDriverWait wait(WebDriver driver) {
        return new WebDriverWait(driver, EXPLICIT_WAIT);
    }
}
