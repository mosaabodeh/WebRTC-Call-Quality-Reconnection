package utils;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class NotificationUtils {

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
}