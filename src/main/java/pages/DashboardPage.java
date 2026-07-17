package pages;

import io.appium.java_client.AppiumDriver;
import pages.locators.ElementKey;
import pages.locators.ElementRegistry;

public class DashboardPage extends BasePage {

    public DashboardPage(AppiumDriver driver) {
        super(driver);
    }
    public void callContact(String contactName) {
        searchForContact(contactName);
        clickCallButton();
        click(ElementRegistry.get(ElementKey.AUDIO_CALL_BUTTON));
        clickIfElementAppears(ElementRegistry.get(ElementKey.ALLOW_BUTTON));
    }

        public void searchForContact(String contactName) {
        click(ElementRegistry.get(ElementKey.SEARCHBAR));
        type(ElementRegistry.get(ElementKey.SEARCHBAR_Field), contactName);
        click(ElementRegistry.get(ElementKey.FIRST_Search_RESULT));

    }
    public void clickCallButton(){
        click(ElementRegistry.get(ElementKey.CALL_BUTTON));

    }
    public void videoCallContact(String contactName){
       searchForContact(contactName);
        clickCallButton();
       click(ElementRegistry.get(ElementKey.VIDEO_CALL_BUTTON));
       clickIfElementAppears(ElementRegistry.get(ElementKey.ALLOW_ACCESS_CAMERA));
   }
}