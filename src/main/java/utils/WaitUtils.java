package utils;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import pages.BasePage;
import pages.locators.ElementKey;
import pages.locators.ElementRegistry;

import java.time.Duration;

public class WaitUtils extends BasePage {
    public WaitUtils(AppiumDriver driver) {
        super(driver);
    }



    public void waitForParticipantJoined(String participantName) {

        String xpath = String.format(
                "//*[contains(@text,'%s') or contains(@content-desc,'%s')]",
                participantName,
                participantName);

        new WebDriverWait(driver, Duration.ofSeconds(20))
                .until(ExpectedConditions.visibilityOfElementLocated(
                        AppiumBy.xpath(xpath)));
    }

    public void waitForTranscriptReady() {

        new WebDriverWait(driver, Duration.ofMinutes(3))
                .until(driver -> {
                    try {
                        NotificationUtils.clickFirstNotification((AndroidDriver) driver);
                        return true;
                    } catch (Exception e) {
                        return false;
                    }
                });
    }

}
