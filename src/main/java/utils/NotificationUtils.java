package utils;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;
import pages.BasePage;

import java.time.Duration;
import java.util.List;

public class NotificationUtils extends BasePage {

    private final AndroidDriver driver;

    public NotificationUtils(AndroidDriver driver) {
        super(driver);
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

        final String target = expectedCallerName.toLowerCase();
        Wait<AndroidDriver> wait = new FluentWait<>(driver)
                .withTimeout(Duration.ofSeconds(40))
                .pollingEvery(Duration.ofSeconds(2))
                .ignoring(NoSuchElementException.class)
                .ignoring(StaleElementReferenceException.class);
        try {
            return wait.until(d -> {

                try {
                    d.openNotifications();
                    Thread.sleep(700);
                    List<WebElement> titles = d.findElements(AppiumBy.id("android:id/title"));
                    List<WebElement> texts = d.findElements(AppiumBy.id("android:id/text"));
                    for (int i = 0; i < titles.size(); i++) {
                        String title = titles.get(i).getText().toLowerCase();
                        String body = "";
                        if (i < texts.size()) {
                            body = texts.get(i).getText().toLowerCase();
                        }
                        System.out.printf("Notification -> [%s] | [%s]%n", title, body);
                        String notification = title + " " + body;
                        if (notification.contains("missed call") && notification.contains(target)) {
                            System.out.println("✅ Missed call notification found.");
                            d.navigate().back();
                            return true;
                        }
                    }
                    d.navigate().back();
                } catch (Exception e) {
                    try {
                        d.navigate().back();
                    } catch (Exception ignored) {
                    }
                    System.out.println("Waiting for notification...");
                }
                return false;
            });

        } catch (TimeoutException e) {
            System.out.println("❌ Missed call notification not received within timeout.");
            return false;
        }
    }
    public int getAppIconBadgeCount() {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(25));

            By samsungBadge = AppiumBy.id("com.sec.android.app.launcher:id/badge_text");
            List<WebElement> samsungElements = driver.findElements(samsungBadge);
            if (!samsungElements.isEmpty()) {
                return Integer.parseInt(samsungElements.get(0).getText().trim());
            }
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

            By genericBadge = AppiumBy.xpath("//*[contains(@resource-id, 'badge') or contains(@resource-id, 'unread')]");
            WebElement badge = wait.until(ExpectedConditions.visibilityOfElementLocated(genericBadge));
            return Integer.parseInt(badge.getText().trim());

        } catch (Exception e) {
            System.err.println("⚠️ Could not read home screen app icon badge count: " + e.getMessage());
            return 0;
        }
    }
}