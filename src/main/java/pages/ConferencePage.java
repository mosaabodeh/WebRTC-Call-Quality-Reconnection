package pages;
import drivers.DeviceManager;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.clipboard.HasClipboard;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import pages.locators.ElementKey;
import pages.locators.ElementRegistry;
import utils.NotificationUtils;
import utils.SummaryValidator;
import utils.ToastOcrHandler;

import java.time.Duration;
import java.util.List;

import static pages.CallPage.WaiteForTime;

public class ConferencePage extends BasePage{
    public ConferencePage(AppiumDriver driver) {
        super(driver);
    }
    public void sharingScreen() throws InterruptedException {
        WaiteForTime(3);
        waitClickable(ElementRegistry.get(ElementKey.MORE_OPTION)).click();
        waitClickable(ElementRegistry.get(ElementKey.SHARE_SCREEN_BUTTON)).click();
        waitClickable(ElementRegistry.get(ElementKey.START_OK_APPLY)).click();
    }
    public boolean isSharingScreen()  {
        System.out.println("inside the is sharing function");
         waitVisible(ElementRegistry.get(ElementKey.SHARE_SCREEN_APPEAR)).isDisplayed();
        try {
            waitClickable(ElementRegistry.get(ElementKey.VIEW_FULL_SHARING_SCREEN)).click();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    public void muteAllUsersInTheConference() {
        waitClickable(ElementRegistry.get(ElementKey.MORE_OPTION)).click();
        waitClickable(ElementRegistry.get(ElementKey.MUTE_ALL)).click();
        waitClickable(ElementRegistry.get(ElementKey.START_OK_APPLY)).click();
    }

    public boolean isMutedToastAppear() {
        return getToastMessage().contains("You are muted");
    }
    public void participantList(){
        waitClickable(ElementRegistry.get(ElementKey.MORE_OPTION)).click();
        waitClickable(ElementRegistry.get(ElementKey.PARTICIPANT_LIST)).click();
    }
    public boolean verifyUserRoleInRainbow(String name, String expectedRole) {

        WebElement participantsList = driver.findElement(
                AppiumBy.id("com.ale.rainbow:id/room_participants_recyclerview")
        );

        List<WebElement> participantButtons = participantsList.findElements(
                AppiumBy.className("android.widget.Button")
        );

        String expectedName = name.trim();
        String normalizedExpectedRole = expectedRole.trim();

        for (WebElement participantButton : participantButtons) {

            String contentDesc = participantButton.getAttribute("content-desc");

            if (contentDesc == null || contentDesc.isBlank()) {
                continue;
            }

            System.out.println("Participant content-desc: " + contentDesc);

            String actualName = extractParticipantName(contentDesc);

            // Exact name comparison
            if (!actualName.equalsIgnoreCase(expectedName)) {
                continue;
            }

            String actualRole = determineParticipantRole(
                    participantButton,
                    contentDesc
            );

            boolean roleMatches =
                    actualRole.equalsIgnoreCase(normalizedExpectedRole);

            System.out.println("Participant name: " + actualName);
            System.out.println("Expected role: " + normalizedExpectedRole);
            System.out.println("Actual role: " + actualRole);
            System.out.println("Role matches: " + roleMatches);

            return roleMatches;
        }

        System.out.println("Participant not found: " + expectedName);
        return false;
    }
    private String extractParticipantName(String contentDesc) {

        int firstCommaIndex = contentDesc.indexOf(",");

        if (firstCommaIndex == -1) {
            return contentDesc.trim();
        }

        return contentDesc.substring(0, firstCommaIndex).trim();
    }

    private String determineParticipantRole(
            WebElement participantButton,
            String contentDesc) {

        if (containsExactRole(contentDesc, "Owner")) {
            return "Owner";
        }

        if (containsExactRole(contentDesc, "Organizer")) {
            return "Organizer";
        }

        // A member has no Owner or Organizer role icon
        return "Member";
    }
    private boolean containsExactRole(String contentDesc, String role) {

        String[] parts = contentDesc.split(",");

        for (String part : parts) {
            if (part.trim().equalsIgnoreCase(role)) {
                return true;
            }
        }

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
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
            return wait.until(ExpectedConditions.visibilityOfElementLocated(AppiumBy.xpath(xpath))).isDisplayed();

        } catch (Exception e) {
            return false;
        }
}
   public void SpeakerOnlyMode(){
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
    return res.contains("You have started a recording and a transcript. Make sure to inform all participants.")&&isRecordingStarted();
    }

    public void stopRecording() {
        waitClickable(ElementRegistry.get(ElementKey.MORE_OPTION)).click();
        waitClickable(ElementRegistry.get(ElementKey.STOP_RECORDING)).click();
    }

    public boolean isTranscriptTextAccurate() {
        NotificationUtils.clickFirstNotification((AndroidDriver) this.driver);
        waitClickable(ElementRegistry.get(ElementKey.SUMMARY_COBY_BUTTON)).click();
        String summaryResult = ((HasClipboard) driver).getClipboardText();
        System.out.println("the Summary Result is : "+summaryResult);
        return isSummaryCorrect(summaryResult);
    }
    public boolean isSummaryCorrect(String summary) {
        return SummaryValidator.isSummaryValid(summary);
    }
    public boolean lockTheMeeting() {
        System.out.println("Inside The Making meeting Locked");
        waitClickable(ElementRegistry.get(ElementKey.MORE_OPTION)).click();
        waitClickable(ElementRegistry.get(ElementKey.LOCK_MEETING)).click();
        waitClickable(ElementRegistry.get(ElementKey.START_OK_APPLY)).click();

        boolean flag = waitVisible(ElementRegistry.get(ElementKey.MEETING_LOCK)).isDisplayed();
        String res = getToastMessage();
        return flag && res.toLowerCase().contains("you have locked the meeting");
    }
    public boolean isConferenceLocked() throws InterruptedException {
        clickButtonByCoordinates(AppiumBy.androidUIAutomator("new UiSelector().text(\"LIVE\")"));
        clickJoinButton();
        WaiteForTime(1.5);
        String res=getErrorMessage();
        System.out.println("[" + res + "]");
        return res.contains(
                "this conference has been locked, you are not allowed to enter the meeting.");
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
    }
    public void verifySharingBubbleWithCustomSetting(boolean WaitingRoom,boolean protectedWithPassword){
        openMeetingSettings();

        if(WaitingRoom){
            enableWaitingRoom();
        }
        if(protectedWithPassword){
            enablePassword();
            scrollToBottom();
            String old=clickAndCopyPassword();
            generatePassword();
            String newPass=clickAndCopyPassword();
            if(old.equals(newPass))
                System.out.println("The Password change result is : " + false);
            else
            System.out.println("The Password change result is : "+true);
        }
    }
    public void verifyTurnedOffIncomingVideo(){
        waitClickable(ElementRegistry.get(ElementKey.MORE_OPTION)).click();
        waitClickable(ElementRegistry.get(ElementKey.TURN_OFF_INCOMING_VIDEO)).click();
    }
    public boolean isShareScreenAppear(){
        try {
            return waitVisible(ElementRegistry.get(ElementKey.SHARE_SCREEN_GRID)).isDisplayed();
        } catch (Exception e) {
            return false;
        }    }
    public void verifyTurnedOffIncomingSharing()   {
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

        WebElement muteToggle = waitClickable(ElementRegistry.get(ElementKey.MUTE_COMPARTMENT_UPON_ENTRY));
        boolean isMuteCurrentlyChecked = Boolean.parseBoolean(muteToggle.getAttribute("checked"));
        if (isMuteCurrentlyChecked != muteWhenEnter) {
            muteToggle.click();
        }

        WebElement soundToggle = waitClickable(ElementRegistry.get(ElementKey.PLAY_SOUND_ENTRY));
        boolean isSoundCurrentlyChecked = Boolean.parseBoolean(soundToggle.getAttribute("checked"));
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

        clickButtonByCoordinates(AppiumBy.accessibilityId("Call"));
    }
public String shareLink(){
    waitClickable(ElementRegistry.get(ElementKey.SHARE_BUTTON)).click();
    waitClickable(ElementRegistry.get(ElementKey.SHARE_LINK)).click();
    waitClickable(ElementRegistry.get(ElementKey.COPY_BUTTON)).click();
    return DeviceManager.getDriverA().getClipboardText();
}
    public void muteFor(double duration) throws InterruptedException {
        waitClickable(ElementRegistry.get(ElementKey.MUTE_BUTTON)).click();
        WaiteForTime(duration);
    }
    public void speakFor(double duration) throws InterruptedException {
        waitClickable(ElementRegistry.get(ElementKey.UNMUTE_BUTTON)).click();
        WaiteForTime(duration);
        muteFor(0)  ;
    }

    public boolean verifyUserRuleInRaibow(String name, String rule) {
        WebElement participant = driver.findElement(ElementRegistry.getParticipantNameLocator(name));
        String contentDesc = participant.getAttribute("content-desc");
        System.out.println("The Real Text is : "+contentDesc);
        return contentDesc.contains(name) && contentDesc.contains(rule);

    }
    public void missCall() {
        waitClickable(ElementRegistry.get(ElementKey.CANCEL_CALL_BUTTON)).click();
    }
    public boolean isMissedCallNotificationAppear() {
        return ToastOcrHandler.waitForMissedCallBanner(driver, 8, 500);

    }


}
