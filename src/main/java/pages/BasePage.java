package pages;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.HasOnScreenKeyboard;
import io.appium.java_client.HidesKeyboard;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.nativekey.AndroidKey;
import io.appium.java_client.android.nativekey.KeyEvent;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import pages.locators.ElementKey;
import pages.locators.ElementRegistry;
import utils.ConfigReader;
import utils.ToastOcrHandler;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class BasePage {

    protected final AppiumDriver driver;
    protected final WebDriverWait wait;

    public BasePage(AppiumDriver driver) {
        this.driver = driver;
        long timeout = Long.parseLong(ConfigReader.getProperty("timeout.explicit", "10"));
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(timeout));
    }
    protected static void waiteForTime(double durationOfSecond)   {
        try { Thread.sleep(Duration.ofSeconds((long) durationOfSecond)); } catch (InterruptedException ignored) {}


    }

    protected boolean isDisplayed(By locator) {
        try {
            return waitVisible(locator).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    public void scrollToBottom() {
        driver.findElement((ElementRegistry.get(ElementKey.SCROLL_TO_BUTTON)));
    }
    public void returnToActiveCall() {
        AndroidDriver androidDriver = (AndroidDriver) driver;

        try {
            androidDriver.openNotifications();
            By callNotificationLocator = AppiumBy.xpath(
                    "//*[@package='com.ale.rainbow' or contains(@text, 'Audio call') or contains(@text, 'Return to call')]"
            );
            WebElement notification = waitClickable(callNotificationLocator);
            int centerX = notification.getLocation().getX() + (notification.getSize().getWidth() / 2);
            int centerY = notification.getLocation().getY() + (notification.getSize().getHeight() / 2);
            Map<String, Object> tapParams = new HashMap<>();
            tapParams.put("x", centerX);
            tapParams.put("y", centerY);
            androidDriver.executeScript("mobile: clickGesture", tapParams);

        } catch (Exception e) {
            androidDriver.pressKey(new KeyEvent(AndroidKey.BACK));
            throw new RuntimeException("Failed to click the active call notification.", e);
        }
    }
    protected WebElement waitVisible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }
    public void clickNavigationBack() {
        AndroidDriver androidDriver = (AndroidDriver) driver;
        androidDriver.pressKey(new KeyEvent(AndroidKey.BACK));
    }
    protected WebElement waitClickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    protected void click(By locator) {
        waitClickable(locator).click();
    }

    protected void type(By locator, String text) {
        WebElement el = waitVisible(locator);
        el.clear();
        el.sendKeys(text);
        hideKeyboardIfShown();
    }


    public void clickJoinButton() {
        Dimension size = driver.manage().window().getSize();

        int x = (int) (size.width * 0.83);
        int y = (int) (size.height * 0.135);

        Map<String, Object> tapParams = new HashMap<>();
        tapParams.put("x", x);
        tapParams.put("y", y);

        driver.executeScript("mobile: clickGesture", tapParams);
        System.out.println("Clicked Join button at: X=" + x + ", Y=" + y);
    }

    public void clickButtonByCoordinates(By ele) {
        waiteForTime(2);
        WebElement clickedElementButton = waitVisible(ele);

        Point location = clickedElementButton.getLocation();
        Dimension size = clickedElementButton.getSize();

        int centerX = location.getX() + (size.getWidth() / 2);
        int centerY = location.getY() + (size.getHeight() / 2);

        System.out.println("Calculated Center X: " + centerX + " | Center Y: " + centerY);

        Map<String, Object> tapParams = new HashMap<>();
        tapParams.put("x", centerX);
        tapParams.put("y", centerY);

        driver.executeScript("mobile: clickGesture", tapParams);
    }


    public String getToastMessage() {
        By toastLocator = AppiumBy.xpath("//android.widget.Toast");

        WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(5));
        WebElement toastElement = shortWait.until(
                ExpectedConditions.presenceOfElementLocated(toastLocator));
        String toastText = toastElement.getText();
        if (toastText == null || toastText.isEmpty()) {
            toastText = toastElement.getAttribute("text");
        }

        return toastText;
    }

    protected void hideKeyboardIfShown() {
        try {
            if (driver instanceof HasOnScreenKeyboard && driver instanceof HidesKeyboard) {
                boolean shown = ((HasOnScreenKeyboard) driver).isKeyboardShown();
                System.out.println("Keyboard status: " + shown);

                if (shown) {
                    ((HidesKeyboard) driver).hideKeyboard();
                    System.out.println("hideKeyboard() executed successfully.");
                }
            } else {
                System.out.println("Driver implementation context does not support native keyboard APIs.");
            }
        } catch (Exception e) {
            System.out.println("hideKeyboardIfShown() execution skipped: " + e.getMessage());
        }
    }
}




