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
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;

public class NotificationUtils extends BasePage {

    private static final By NOTIFICATION_TITLE = AppiumBy.id("android:id/title");
    private static final By NOTIFICATION_TEXT = AppiumBy.id("android:id/text");

    private final AndroidDriver driver;

    public NotificationUtils(AndroidDriver driver) {
        super(driver);
        this.driver = driver;
    }


    private static class NotificationEntry {
        final String title;
        final String body;

        NotificationEntry(String title, String body) {
            this.title = title;
            this.body = body;
        }

        String combined() {
            return title + " " + body;
        }
    }


    private boolean pollNotificationShade(int timeoutSeconds, int pollIntervalMillis,
                                          BiPredicate<AndroidDriver, List<NotificationEntry>> matcher) {
        Wait<AndroidDriver> wait = new FluentWait<>(driver)
                .withTimeout(Duration.ofSeconds(timeoutSeconds))
                .pollingEvery(Duration.ofMillis(pollIntervalMillis))
                .ignoring(NoSuchElementException.class)
                .ignoring(StaleElementReferenceException.class);

        try {
            return wait.until(d -> {
                try {
                    d.openNotifications();

                    new WebDriverWait(d, Duration.ofSeconds(3))
                            .until(ExpectedConditions.presenceOfElementLocated(NOTIFICATION_TITLE));

                    List<WebElement> titleElements = d.findElements(NOTIFICATION_TITLE);
                    List<WebElement> textElements = d.findElements(NOTIFICATION_TEXT);

                    List<NotificationEntry> entries = new ArrayList<>();
                    for (int i = 0; i < titleElements.size(); i++) {
                        String title = titleElements.get(i).getText().toLowerCase();
                        String body = (i < textElements.size()) ? textElements.get(i).getText().toLowerCase() : "";
                        entries.add(new NotificationEntry(title, body));
                    }

                    boolean matched = matcher.test(d, entries);
                    if (!matched) {
                        d.navigate().back();
                    }
                    return matched;

                } catch (Exception e) {
                    try {
                        d.navigate().back();
                    } catch (Exception ignored) {
                    }
                    return false;
                }
            });
        } catch (TimeoutException e) {
            return false;
        }
    }


    public void clickFirstNotification() {
        boolean clicked = pollNotificationShade(10, 500, (d, entries) -> {
            if (entries.isEmpty()) {
                return false;
            }
            List<WebElement> titleElements = d.findElements(NOTIFICATION_TITLE);
            if (!titleElements.isEmpty()) {
                titleElements.get(0).click();
                return true;
            }
            return false;
        });

        if (!clicked) {
            System.out.println("No notifications found in the shade within timeout.");
        }
    }


    public boolean waitForNotificationPresent(int timeoutSeconds, int pollIntervalMillis) {
        boolean found = pollNotificationShade(timeoutSeconds, pollIntervalMillis, (d, entries) -> !entries.isEmpty());
        if (!found) {
            System.out.println("⚠️ No notification appeared within " + timeoutSeconds + "s.");
        }
        return found;
    }


    public boolean isMissedCallNotificationDisplayed(String expectedCallerName) {
        final String target = expectedCallerName.toLowerCase();

        boolean found = pollNotificationShade(40, 2000, (d, entries) -> {
            for (NotificationEntry entry : entries) {
                System.out.printf("Notification -> [%s] | [%s]%n", entry.title, entry.body);
                if (entry.combined().contains("missed call") && entry.combined().contains(target)) {
                    System.out.println("✅ Missed call notification found.");
                    return true;
                }
            }
            return false;
        });

        if (!found) {
            System.out.println("❌ Missed call notification not received within timeout.");
        }
        return found;
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