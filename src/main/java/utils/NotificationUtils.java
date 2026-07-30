package utils;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class NotificationUtils {

    private final AndroidDriver driver;

    public NotificationUtils(AndroidDriver driver) {
        this.driver = driver;
    }

    public  void clickFirstNotification() {
        this.driver.openNotifications();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        AppiumBy notificationTitleLocator = (AppiumBy) AppiumBy.id("android:id/title");

        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(notificationTitleLocator));
            List<WebElement> notifications = driver.findElements(notificationTitleLocator);
            if (!notifications.isEmpty()) {
                notifications.get(0).click();
            } else {
                System.out.println("No notifications found in the shade.");
            }
        } catch (Exception e) {
            System.err.println("Timed out waiting for notifications: " + e.getMessage());
            driver.navigate().back();
        }
    }

    public void sendAppToBackground() {
        driver.runAppInBackground(Duration.ofSeconds(-1));
    }

    public void terminateApp(String appPackage) {
        driver.terminateApp(appPackage);
    }

    public void activateApp(String appPackage) {
        driver.activateApp(appPackage);
    }

    public boolean isMissedCallNotificationDisplayed(String expectedCallerName) {
        try {
            Thread.sleep(5000);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            driver.openNotifications();
            By titleLocator = AppiumBy.id("android:id/title");
            By textLocator = AppiumBy.id("android:id/text");

            wait.until(ExpectedConditions.presenceOfElementLocated(titleLocator));

            List<WebElement> titles = driver.findElements(titleLocator);
            List<WebElement> texts = driver.findElements(textLocator);

            for (WebElement title : titles) {
                String titleText = title.getText().toLowerCase();
                System.out.println("the current notification title is : " + titleText);
                if (titleText.contains("missed call") || titleText.contains(expectedCallerName.toLowerCase())) {
                    driver.navigate().back();
                    return true;
                }
            }

            for (WebElement text : texts) {
                String subText = text.getText().toLowerCase();
                System.out.println("the current notification Text is : " + subText);

                if (subText.contains("missed call") || subText.contains(expectedCallerName.toLowerCase())) {
                    driver.navigate().back();
                    return true;
                }
            }
        } catch (Exception e) {
            System.err.println("⚠️ Error finding missed call notification: " + e.getMessage());
        }
        driver.navigate().back();
        return false;
    }

    public int getAppIconBadgeCount() {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));

            // 1. Samsung One UI specific badge text locator
            By samsungBadge = AppiumBy.id("com.sec.android.app.launcher:id/badge_text");
            List<WebElement> samsungElements = driver.findElements(samsungBadge);
            if (!samsungElements.isEmpty()) {
                return Integer.parseInt(samsungElements.get(0).getText().trim());
            }

            // 2. Rainbow app icon content-desc locator
            By rainbowIcon = AppiumBy.xpath("//*[contains(@content-desc, 'Rainbow') or contains(@content-desc, 'rainbow')]");
            List<WebElement> iconElements = driver.findElements(rainbowIcon);
            if (!iconElements.isEmpty()) {
                String desc = iconElements.get(0).getAttribute("content-desc");
                if (desc != null) {
                    String numericOnly = desc.replaceAll("[^0-9]", "");
                    if (!numericOnly.isEmpty()) {
                        return Integer.parseInt(numericOnly);
                    }
                }
            }

            // 3. Generic Android badge locator fallback
            By genericBadge = AppiumBy.xpath("//*[contains(@resource-id, 'badge') or contains(@resource-id, 'unread')]");
            WebElement badge = wait.until(ExpectedConditions.visibilityOfElementLocated(genericBadge));
            return Integer.parseInt(badge.getText().trim());

        } catch (Exception e) {
            System.err.println("⚠️ Could not read home screen app icon badge count: " + e.getMessage());
            return 0;
        }
    }
}