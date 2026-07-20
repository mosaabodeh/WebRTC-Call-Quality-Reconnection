package pages;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import pages.locators.ElementKey;
import pages.locators.ElementRegistry;

import java.time.Duration;

public class LoginPage extends BasePage {

    public LoginPage(AppiumDriver driver) {
        super(driver);
    }
    public void login(String email, String password) {
        type(ElementRegistry.get(ElementKey.EMAIL_FIELD), email);
        click(ElementRegistry.get(ElementKey.CONTINUE_BUTTON));
        passwordStage(password);
    }
   void passwordStage(String password){
        type(ElementRegistry.get(ElementKey.PASSWORD_FIELD), password);
        hideKeyboardIfShown();
        click(ElementRegistry.get(ElementKey.LOGIN_SUBMIT_BUTTON));
    }
    public boolean isUserAlreadyLoggedIn() {
        try {
            WebDriverWait quickWait = new WebDriverWait(driver, Duration.ofSeconds(4));
            quickWait.until(ExpectedConditions.visibilityOfElementLocated(ElementRegistry.get(ElementKey.SEARCHBAR)));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}