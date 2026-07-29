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

    /**
     * Original method: Opens notification shade and clicks the first notification.
     */
    public static void clickFirstNotification(AndroidDriver driver) {
        driver.openNotifications();
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

    /**
     * Sends the application to the background indefinitely until explicitly restored.
     */
    public static void sendAppToBackground(AndroidDriver driver) {
        driver.runAppInBackground(Duration.ofSeconds(-1));
    }

    /**
     * Terminates the target application package completely.
     */
    public static void terminateApp(AndroidDriver driver, String appPackage) {
        driver.terminateApp(appPackage);
    }

    /**
     * Brings the application back to the foreground / launches it.
     */
    public static void activateApp(AndroidDriver driver, String appPackage) {
        driver.activateApp(appPackage);
    }

    /**
     * Opens the notification shade, verifies if a missed call notification exists,
     * and automatically closes the notification shade before returning.
     */
    public static boolean isMissedCallNotificationDisplayed(AndroidDriver driver, String expectedCallerName) {
        try {
            driver.openNotifications();
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            By titleLocator = AppiumBy.id("android:id/title");
            By textLocator = AppiumBy.id("android:id/text");

            wait.until(ExpectedConditions.presenceOfElementLocated(titleLocator));

            List<WebElement> titles = driver.findElements(titleLocator);
            List<WebElement> texts = driver.findElements(textLocator);

            for (WebElement title : titles) {
                String titleText = title.getText().toLowerCase();
                if (titleText.contains("missed call") || titleText.contains(expectedCallerName.toLowerCase())) {
                    driver.navigate().back(); // Close notification shade
                    return true;
                }
            }

            for (WebElement text : texts) {
                String subText = text.getText().toLowerCase();
                if (subText.contains("missed call") || subText.contains(expectedCallerName.toLowerCase())) {
                    driver.navigate().back(); // Close notification shade
                    return true;
                }
            }
        } catch (Exception e) {
            System.err.println("⚠️ Error finding missed call notification: " + e.getMessage());
        }
        driver.navigate().back(); // Close notification shade if search fails
        return false;
    }

    /**
     * Attempts to read the badge counter number on the launcher app icon.
     */
    public static int getAppIconBadgeCount(AndroidDriver driver) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));

            // 1. Samsung One UI specific badge text locator
            By samsungBadge = AppiumBy.id("com.sec.android.app.launcher:id/badge_text");
            List<WebElement> samsungElements = driver.findElements(samsungBadge);
            if (!samsungElements.isEmpty()) {
                return Integer.parseInt(samsungElements.get(0).getText().trim());
            }

            // 2. Look for the Rainbow app icon and check its content-desc (e.g., "Rainbow, 1 unread notification")
            By rainbowIcon = AppiumBy.xpath("//*[contains(@content-desc, 'Rainbow') or contains(@content-desc, 'rainbow')]");
            List<WebElement> iconElements = driver.findElements(rainbowIcon);
            if (!iconElements.isEmpty()) {
                String desc = iconElements.get(0).getAttribute("content-desc");
                if (desc != null) {
                    // Extracts numbers from strings like "Rainbow, 1 new notification"
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