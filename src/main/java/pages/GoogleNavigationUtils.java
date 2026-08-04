package pages;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.nativekey.AndroidKey;
import io.appium.java_client.android.nativekey.KeyEvent;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;

import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GoogleNavigationUtils extends BasePage {

    public GoogleNavigationUtils(AppiumDriver driver) {
        super(driver);
    }

    public void openChromeAndNavigateToFirstLink(String fullMessage) {
        AndroidDriver androidDriver = (AndroidDriver) driver;
        WebDriverWait wait = new WebDriverWait(androidDriver, Duration.ofSeconds(10));

        System.out.println("Full text from clipboard: " + fullMessage);

        if (fullMessage == null || fullMessage.isEmpty()) {
            throw new IllegalStateException("Clipboard is empty! Make sure text was copied before calling this method.");
        }
        Pattern pattern = Pattern.compile("https?://\\S+");
        Matcher matcher = pattern.matcher(fullMessage);

        if (!matcher.find()) {
            throw new IllegalArgumentException("No URL found inside copied text: " + fullMessage);
        }

        String firstLink = matcher.group(0);
        System.out.println("Extracted first link to navigate: " + firstLink);
        androidDriver.activateApp("com.android.chrome");

        By initialSearchBoxLocator = AppiumBy.androidUIAutomator(
                "new UiSelector().resourceId(\"com.android.chrome:id/search_box_text\")");
        WebElement initialSearchBox = wait.until(ExpectedConditions.elementToBeClickable(initialSearchBoxLocator));
        initialSearchBox.click();
        By activeUrlBarLocator = AppiumBy.androidUIAutomator(
                "new UiSelector().resourceId(\"com.android.chrome:id/url_bar\")");
        WebElement activeUrlBar = wait.until(ExpectedConditions.visibilityOfElementLocated(activeUrlBarLocator));

        activeUrlBar.sendKeys(firstLink);
        androidDriver.pressKey(new KeyEvent(AndroidKey.ENTER));
    }
    private String extractUrl(String text) {

        Pattern pattern = Pattern.compile(
                "(https?://\\S+)"
        );

        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            return matcher.group(1);
        }

        throw new RuntimeException("No meeting URL found in invitation");
    }
    public void openMeetingLinkInRainbow(String meetingLink) {

        AndroidDriver androidDriver = (AndroidDriver) driver;
        String link = extractUrl(meetingLink);
        WebDriverWait wait = new WebDriverWait(androidDriver, Duration.ofSeconds(15));

        // Open Google Search app
        androidDriver.activateApp("com.google.android.googlequicksearchbox");
        WebElement searchText = wait.until(ExpectedConditions.presenceOfElementLocated(
                        AppiumBy.androidUIAutomator("new UiSelector().text(\"Search\").instance(0)")));
        Point location = searchText.getLocation();
        Dimension size = searchText.getSize();

        int centerX = location.getX() + (size.getWidth() / 2);
        int centerY = location.getY() + (size.getHeight() / 2);

        System.out.println("Google Search coordinates X=" + centerX + " Y=" + centerY);

        Map<String, Object> click = new HashMap<>();
        click.put("x", centerX);
        click.put("y", centerY);

        androidDriver.executeScript("mobile: clickGesture", click);
        WebElement searchBox = wait.until(
                ExpectedConditions.elementToBeClickable(
                        AppiumBy.id(
                                "com.google.android.googlequicksearchbox:id/googleapp_search_box")));
        searchBox.click();
        searchBox.sendKeys(link);
        androidDriver.pressKey(
                new KeyEvent(AndroidKey.ENTER)
        );
    }


}