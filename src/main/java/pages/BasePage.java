package pages;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.HasOnScreenKeyboard;
import io.appium.java_client.HidesKeyboard;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import utils.ConfigReader;

import java.time.Duration;

public class BasePage {

    protected final AppiumDriver driver;
    protected final WebDriverWait wait;

    public BasePage(AppiumDriver driver) {
        this.driver = driver;
        long timeout = Long.parseLong(ConfigReader.getProperty("timeout.explicit", "10"));
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(timeout));
    }

    protected boolean isDisplayed(By locator) {
        try {
            return waitVisible(locator).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    protected WebElement waitVisible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
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

    public void clickIfElementAppears(By locator) {
        try {
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(3));
            shortWait.until(ExpectedConditions.elementToBeClickable(locator)).click();
        } catch (Exception e) {
            System.out.println("ℹ️ System element did not appear. Proceeding execution context...");
        }
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