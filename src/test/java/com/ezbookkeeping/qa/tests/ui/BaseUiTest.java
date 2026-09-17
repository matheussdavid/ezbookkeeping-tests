package com.ezbookkeeping.qa.tests.ui;

import com.ezbookkeeping.qa.ui.driver.DriverFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.WebDriver;

public class BaseUiTest {

    protected WebDriver driver;

    @BeforeEach
    void setUp() {
        driver = DriverFactory.createChrome();
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}