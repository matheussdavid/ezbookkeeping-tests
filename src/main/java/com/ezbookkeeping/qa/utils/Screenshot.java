package com.ezbookkeeping.qa.utils;

import com.ezbookkeeping.qa.config.AppConfig;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class Screenshot {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private Screenshot() {
    }

    public static String capture(WebDriver driver, String testName) {
        if (!(driver instanceof TakesScreenshot ts)) {
            return null;
        }

        File source = ts.getScreenshotAs(OutputType.FILE);
        String timestamp = LocalDateTime.now().format(FORMATTER);
        String fileName = testName + "_" + timestamp + ".png";

        Path dir = Paths.get(AppConfig.REPORT_DIR, "screenshots");
        try {
            Files.createDirectories(dir);
            Path destination = dir.resolve(fileName);
            Files.copy(source.toPath(), destination);
            return destination.toString();
        } catch (IOException e) {
            return source.getAbsolutePath();
        }
    }
}