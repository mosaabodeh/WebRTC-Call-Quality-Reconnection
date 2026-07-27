package pages;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.nativekey.AndroidKey;
import io.appium.java_client.android.nativekey.KeyEvent;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChromeNavigationUtils extends BasePage {

    public ChromeNavigationUtils(AppiumDriver driver) {
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

   public void reopenRainbow(){
        AndroidDriver androidDriver = (AndroidDriver) driver;
        androidDriver.activateApp("com.ale.rainbow");
    }
}