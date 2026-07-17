package pages;

import io.appium.java_client.AppiumDriver;
import pages.locators.ElementKey;
import pages.locators.ElementRegistry;

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

}