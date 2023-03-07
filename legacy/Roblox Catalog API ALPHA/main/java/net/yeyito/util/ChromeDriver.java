package net.yeyito.util;

import org.jsoup.Jsoup;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.*;
import java.time.Duration;
import java.util.*;

@Deprecated public class ChromeDriver { // Will soon be out of use
    WebDriver currentDriver;

    public ChromeDriver(boolean headless) {
        System.setProperty("webdriver.chrome.driver", "C:\\Users\\aline\\OneDrive\\Desktop\\CodeHelpers\\ChromeDriver\\chromedriver.exe");

        if (headless) {
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless");
            currentDriver = new org.openqa.selenium.chrome.ChromeDriver(options);
        } else {
            currentDriver = new org.openqa.selenium.chrome.ChromeDriver();
        }
    }

    public void openURL(String URL) throws IOException {
        this.currentDriver.get(URL);
        WebDriverWait wait = new WebDriverWait(this.currentDriver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.jsReturnsValue("return document.readyState==\"complete\";"));
    }

    public void scrapeOpenedURL() {
        String html = this.currentDriver.getPageSource();
        System.out.println(Jsoup.parse(html));
    }

    @Deprecated public void getCatalogIDsFromOpenedURL() { // Deprecated, plans to auto-update <3 Rolimon
        String html = this.currentDriver.getPageSource();
        List<String> IDs = StringFilter.parseStringUsingRegexMatchAllDigits(html, "catalog/(.*?)/");
        TextFile textFile = new TextFile("src/main/resources/Limiteds.txt");

        for (String ID: IDs) {
            if (!textFile.findString(ID)) {
                textFile.writeString(ID + "\n");
                System.out.println("Wrote ID: " + ID);
            }
        }
    }
}
