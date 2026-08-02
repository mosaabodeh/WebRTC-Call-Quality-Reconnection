package pages;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.clipboard.HasClipboard;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import pages.locators.ElementKey;
import pages.locators.ElementRegistry;
import utils.*;

import java.util.List;


public class ConferencePage extends BasePage{
    public ConferencePage(AppiumDriver driver) {
        super(driver);
    }
    public void sharingScreen()   {
        waiteForTime(3);
        waitClickable(ElementRegistry.get(ElementKey.MORE_OPTION)).click();
        waitClickable(ElementRegistry.get(ElementKey.SHARE_SCREEN_BUTTON)).click();
        waitClickable(ElementRegistry.get(ElementKey.START_OK_APPLY)).click();
    }
    public boolean isSharingScreen() {
        System.out.println("inside the is sharing function");
        try {
            boolean isDisplayed = waitVisible(ElementRegistry.get(ElementKey.SHARE_SCREEN_APPEAR)).isDisplayed();
            if (!isDisplayed) {
                return false;
            }
            waitClickable(ElementRegistry.get(ElementKey.VIEW_FULL_SHARING_SCREEN)).click();
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }
    public void muteAllUsersInTheConference() {
        waitClickable(ElementRegistry.get(ElementKey.MORE_OPTION)).click();
        waitClickable(ElementRegistry.get(ElementKey.MUTE_ALL)).click();
        waitClickable(ElementRegistry.get(ElementKey.START_OK_APPLY)).click();
    }

    public boolean isMutedToastAppear() {
        return ToastOcrHandler.waitForToastContaining(driver, JsonReader.getTestData("ConferenceData.json", "userMuted"), 5, 500);
    }
    public void participantList(){
        waitClickable(ElementRegistry.get(ElementKey.MORE_OPTION)).click();
        waitClickable(ElementRegistry.get(ElementKey.PARTICIPANT_LIST)).click();
    }
    public boolean verifyUserRuleInRainbowUsingList(String name, String expectedRole) {

        WebElement participantsList = waitVisible(ElementRegistry.get(ElementKey.PARTICIPANTS_RECYCLER_VIEW));

        List<WebElement> participantButtons = participantsList.findElements(
                AppiumBy.className("android.widget.Button"));

        String expectedName = name.trim();
        String normalizedExpectedRole = expectedRole.trim();
        for (WebElement participantButton : participantButtons) {
            String contentDesc = participantButton.getAttribute("content-desc");

            if (contentDesc == null || contentDesc.isBlank()) {
                continue;
            }
            System.out.println("Participant content-desc: " + contentDesc);

            String actualName = ParticipantParserUtils.extractParticipantName(contentDesc);
            // Exact name comparison
            if (!actualName.equalsIgnoreCase(expectedName)) {
                continue;
            }
            String actualRole = ParticipantParserUtils.determineParticipantRole(participantButton,contentDesc);
            boolean roleMatches =actualRole.equalsIgnoreCase(normalizedExpectedRole);

            return roleMatches;
        }

        System.out.println("Participant not found: " + expectedName);
        return false;
    }

    public void addRoomToConference(){
        click(ElementRegistry.get(ElementKey.ADD_PARTICIPANTS_BUTTON));
        waitClickable(ElementRegistry.get(ElementKey.CALL_RAINBOW_ROOM)).click();
        waitClickable(ElementRegistry.get(ElementKey.RAINBOW_TEST_ROOM)).click();
        click(ElementRegistry.get(ElementKey.CONTINUE));

    }
    public boolean verifyAddedUserInParticipantList (String userName ) {
        participantList();
        if (userName == null || userName.isBlank()) return false;

        try {
            String nameLower = userName.trim().toLowerCase();
            String xpath = String.format(
                    "//*[contains(translate(@content-desc, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), '%s') " +
                            "or contains(translate(@text, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), '%s')]",
                    nameLower, nameLower);
            return waitVisible((AppiumBy.xpath(xpath))).isDisplayed();

        } catch (TimeoutException | NoSuchElementException  e) {
            return false;
        }
}
   public void speakerOnlyMode(){
        waitClickable(ElementRegistry.get(ElementKey.MORE_OPTION)).click();
        waitClickable(ElementRegistry.get(ElementKey.SPEAKER_ONLY_MODE)).click();

    }
    public boolean isGridViewExist(){
        waitClickable(ElementRegistry.get(ElementKey.MORE_OPTION)).click();
        String res=waitVisible(ElementRegistry.get(ElementKey.GRID_VIEW)).getText();
        System.out.println("The Result is : "+res);
        return res.contains("Grid view");
    }
    void openRecording(){
        waitClickable(ElementRegistry.get(ElementKey.MORE_OPTION)).click();
        waitClickable(ElementRegistry.get(ElementKey.RECODE_WITH_TRANSCRIPT)).click();
    }

    void enableTranscript(){
        waitClickable(ElementRegistry.get(ElementKey.TRANSCRIPTION_SWITCH)).click();
    }

   void enableSummary(){
       waitClickable(ElementRegistry.get(ElementKey.SUMMARY_SWITCH)).click();

   }
   void enableDelete(){
        scrollToBottom();
       waitClickable(ElementRegistry.get(ElementKey.DELETE_RECORD_SWITCH)).click();
   }

    private void startRecording(){
        waitClickable(ElementRegistry.get(ElementKey.START_OK_APPLY)).click();
    }

   private  boolean isRecordingStarted(){
        return waitVisible(ElementRegistry.get(ElementKey.RECORD_INDICATOR)).isDisplayed();
   }

  private String getRecordingMessage(){
       return waitVisible(ElementRegistry.get(ElementKey.RECORD_INFORMATION_MESSAGE)).getText();
   }
    public boolean recordWithTranscript(){
        openRecording();
        enableTranscript();
        enableSummary();
        enableDelete();
        startRecording();
    String res=getRecordingMessage();
    return res.contains(JsonReader.getTestData("ConferenceData.json", "recordWithTranscript"))&&isRecordingStarted();
    }

    public void stopRecording() {
        waitClickable(ElementRegistry.get(ElementKey.MORE_OPTION)).click();
        waitClickable(ElementRegistry.get(ElementKey.STOP_RECORDING)).click();
        waitClickable(ElementRegistry.get(ElementKey.START_OK_APPLY)).click();
    }

    public boolean isTranscriptTextAccurate()   {
          waiteForTime(10);
        new NotificationUtils((AndroidDriver) driver).clickFirstNotification();
        waitClickable(ElementRegistry.get(ElementKey.SUMMARY_COBY_BUTTON)).click();
        String summaryResult = ((HasClipboard) this.driver).getClipboardText();
        System.out.println("the Summary Result is : "+summaryResult);
        return isSummaryCorrect(summaryResult);
    }
    public boolean isSummaryCorrect(String summary) {
        return SummaryValidator.isSummaryValid(summary);
    }
    public boolean lockTheMeeting() {
        waitClickable(ElementRegistry.get(ElementKey.MORE_OPTION)).click();
        waitClickable(ElementRegistry.get(ElementKey.LOCK_MEETING)).click();
        waitClickable(ElementRegistry.get(ElementKey.START_OK_APPLY)).click();

        boolean flag = waitVisible(ElementRegistry.get(ElementKey.MEETING_LOCK)).isDisplayed();
        String res = getToastMessage();
        return flag && res.toLowerCase().contains(JsonReader.getTestData("ConferenceData.json", "meetingLocked").toLowerCase());
    }
    public boolean isConferenceLocked()   {
        clickButtonByCoordinates(ElementRegistry.get(ElementKey.LIVE_BUTTON_COORDINATE));
        clickJoinButton();
        return ToastOcrHandler.waitForToastContaining(driver, JsonReader.getTestData("ConferenceData.json", "userLocked").toLowerCase(),
                5, 500);
    }
    public String verifySharingLinkStander(){
        meetingOption();
       return shareLink();
    }

  private void openMeetingSettings(){
      meetingOption();
       waitClickable(ElementRegistry.get(ElementKey.SHARE_BUTTON)).click();
       waitClickable(ElementRegistry.get(ElementKey.SHARE_WITH_EVERYONE)).click();
   }

  private void enableWaitingRoom(){
        waitClickable(ElementRegistry.get(ElementKey.WAITING_ROOM)).click();

    }

   private void enablePassword(){
       scrollToBottom();
        waitClickable(ElementRegistry.get(ElementKey.ROOM_PASSWORD)).click();
    }

   private void generatePassword(){
        waitClickable(ElementRegistry.get(ElementKey.REGENERATE_NEW_PASSWORD)).click();
       waitClickable(ElementRegistry.get(ElementKey.START_OK_APPLY)).click();


   }
    public boolean verifySharingBubbleWithCustomSetting(boolean waitingRoom,boolean protectedWithPassword)   {
        openMeetingSettings();
        if(waitingRoom){
            enableWaitingRoom();
        }
        if(protectedWithPassword){
            enablePassword();
           scrollToBottom();
            String oldPass=clickAndCopyPassword();
             wait.until(ExpectedConditions.invisibilityOfElementLocated(ElementRegistry.get(ElementKey.COPY_STATUS_MESSAGE)));
            generatePassword();
            String newPass=clickAndCopyPassword();
            return !oldPass.equals(newPass);
        }
        return true;
    }
    public void turnedOffIncomingVideo(){
        waitClickable(ElementRegistry.get(ElementKey.MORE_OPTION)).click();
        waitClickable(ElementRegistry.get(ElementKey.TURN_OFF_INCOMING_VIDEO)).click();

    }
    public boolean isShareScreenAppear(){
        try {
            return waitVisible(ElementRegistry.get(ElementKey.SHARE_SCREEN_GRID)).isDisplayed();
        } catch (Exception e) {
            return false;
        }    }
    public void turnedOffIncomingSharing()   {
        waitClickable(ElementRegistry.get(ElementKey.MORE_OPTION)).click();
        waitClickable(ElementRegistry.get(ElementKey.TURNOFF_INCOMING_SHARING)).click();
    }
    private String clickAndCopyPassword() {
        waitClickable(ElementRegistry.get(ElementKey.COPY_MEETING_PASSWORD)).click();
        String actualClipboardText = ((HasClipboard) driver).getClipboardText();
        System.out.println(actualClipboardText);
        return actualClipboardText;
    }
    private void meetingOption(){
        waitClickable(ElementRegistry.get(ElementKey.MORE_OPTION)).click();
        waitClickable(ElementRegistry.get(ElementKey.MEETING_OPTION)).click();
    }
    public void configureMeetingOptions(boolean muteWhenEnter, boolean playSound)  {
        meetingOption();
       String checked= "checked";
        WebElement muteToggle = waitClickable(ElementRegistry.get(ElementKey.MUTE_COMPARTMENT_UPON_ENTRY));
        boolean isMuteCurrentlyChecked = Boolean.parseBoolean(muteToggle.getAttribute(checked));
        if (isMuteCurrentlyChecked != muteWhenEnter) {
            muteToggle.click();
        }

        WebElement soundToggle = waitClickable(ElementRegistry.get(ElementKey.PLAY_SOUND_ENTRY));
        boolean isSoundCurrentlyChecked = Boolean.parseBoolean(soundToggle.getAttribute(checked));
        if (isSoundCurrentlyChecked != playSound) {
            soundToggle.click();
        }
    }

    public boolean areMeetingOptionsApplied(boolean expectedMute, boolean expectedPlaySound) {
        meetingOption();

        WebElement muteToggle = waitClickable(ElementRegistry.get(ElementKey.MUTE_COMPARTMENT_UPON_ENTRY));
        WebElement soundToggle = waitClickable(ElementRegistry.get(ElementKey.PLAY_SOUND_ENTRY));

        boolean actualMuteStatus = Boolean.parseBoolean(muteToggle.getAttribute("checked"));
        boolean actualPlaySoundStatus = Boolean.parseBoolean(soundToggle.getAttribute("checked"));
        return (expectedMute==actualMuteStatus&&actualPlaySoundStatus==expectedPlaySound);
    }

    public void startLockedConference() {
        waitClickable(ElementRegistry.get(ElementKey.BUBBLES_TAB)).click();
        waitClickable(ElementRegistry.get(ElementKey.LAST_BUBBLE_CREATED)).click();

        clickButtonByCoordinates(ElementRegistry.get(ElementKey.CALL_BUTTON));
    }
    public String shareLink(){
    waitClickable(ElementRegistry.get(ElementKey.SHARE_BUTTON)).click();
    waitClickable(ElementRegistry.get(ElementKey.SHARE_LINK)).click();
    waitClickable(ElementRegistry.get(ElementKey.COPY_BUTTON)).click();
    return ((HasClipboard) this.driver).getClipboardText();
}
    public boolean isCurrentlyMuted() {
        return isDisplayed(ElementRegistry.get(ElementKey.UNMUTE_BUTTON));
    }
    public void muteFor(double duration) {
        if (!isCurrentlyMuted()) {
            waitClickable(ElementRegistry.get(ElementKey.MUTE_BUTTON)).click();
        }
        waiteForTime(duration);
    }

    public void speakFor(double duration) {
        if (isCurrentlyMuted()) {
            waitClickable(ElementRegistry.get(ElementKey.UNMUTE_BUTTON)).click();
        }
        waiteForTime(duration);
        muteFor(0);
    }

    public boolean verifyUserRuleInRaibow(String name, String rule) {
        WebElement participant = driver.findElement(ElementRegistry.getParticipantNameLocator(name));
        String contentDesc = participant.getAttribute("content-desc");
        System.out.println("The Real Text is : "+contentDesc);
        return contentDesc.contains(name) && contentDesc.contains(rule);

    }

    public boolean isMissedCallNotificationAppear() {
        return ToastOcrHandler.waitForMissedCallBanner(driver, 8, 500);

    }

}
