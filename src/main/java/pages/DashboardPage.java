package pages;

import io.appium.java_client.AppiumDriver;
import pages.locators.ElementKey;
import pages.locators.ElementRegistry;

public class DashboardPage extends BasePage {

    public DashboardPage(AppiumDriver driver) {
        super(driver);
    }

        public void searchForContact(String contactName) {
        click(ElementRegistry.get(ElementKey.SEARCHBAR));
        type(ElementRegistry.get(ElementKey.SEARCHBAR_Field), contactName);
        click(ElementRegistry.get(ElementKey.FIRST_Search_RESULT));
    }

    public void clickCallButton(){
        click(ElementRegistry.get(ElementKey.CALL_BUTTON));
    }

    void searchForUserAndClickCallButton(String userName){
        searchForContact(userName);
        clickCallButton();
    }

    public void callContact(String contactName) {
        searchForUserAndClickCallButton(contactName);
        clickIfElementAppears(ElementRegistry.get(ElementKey.ALLOW_BUTTON));
        click(ElementRegistry.get(ElementKey.AUDIO_CALL_BUTTON));
    }
    public void videoCallContact(String contactName){
        searchForUserAndClickCallButton(contactName);
        clickIfElementAppears(ElementRegistry.get(ElementKey.ALLOW_ACCESS_CAMERA));
        click(ElementRegistry.get(ElementKey.VIDEO_CALL_BUTTON));
    }

    public void addParticipantToCall(String userName){
        click(ElementRegistry.get(ElementKey.ADD_PARTICIPANTS_BUTTON));
        click(ElementRegistry.get(ElementKey.CALL_PARTICIPANT_OPTION_BUTTON));
        searchForContact(userName);
        click(ElementRegistry.get(ElementKey.CONTINUE_BUTTON));


    }


}