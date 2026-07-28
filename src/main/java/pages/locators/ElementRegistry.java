package pages.locators;

import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;

import java.util.HashMap;
import java.util.Map;

public class ElementRegistry {

    private static final Map<String, Map<ElementKey, By>> REGISTRY = new HashMap<>();
    static {
        Map<ElementKey, By> mobile = new HashMap<>();

        mobile.put(ElementKey.CALL_BUTTON,
                AppiumBy.androidUIAutomator("new UiSelector().description(\"Call\")"));
        //For select audio call from popup
        mobile.put(ElementKey.AUDIO_CALL_BUTTON,
                AppiumBy.androidUIAutomator("new UiSelector().text(\"Audio call\")"));
        mobile.put(ElementKey.FIRST_Search_RESULT,
                By.xpath(
                        "//android.view.View[@resource-id=\"conversation_list\"]/android.view.View[1]/android.view.View[2]"));
        mobile.put(ElementKey.FIRST_SEARCH_CALL_RESULT,
                AppiumBy.androidUIAutomator(
                        "new UiSelector().className(\"com.google.android.material.button.MaterialButton\").instance(0)"));
        mobile.put(ElementKey.ALLOW_BUTTON,
                By.id("com.android.permissioncontroller:id/permission_allow_foreground_only_button"));

        mobile.put(ElementKey.SEARCHBAR,
                By.xpath(
                        "//android.widget.Button[@content-desc=\"Do a global search in Rainbow\"]"));
        mobile.put(ElementKey.SEARCHBAR_Field,
                AppiumBy.androidUIAutomator(
                        "new UiSelector().resourceId(\"com.ale.rainbow:id/search_src_text\")"));

        mobile.put(ElementKey.QUALITY_SIGN,
                AppiumBy.id("com.ale.rainbow:id/qualityIndicatorImageView"));

        mobile.put(ElementKey.CALL_STATUS_TEXT,
                AppiumBy.androidUIAutomator(
                        "new UiSelector().textMatches(\"^(Outgoing Call|Incoming Call|Connection in progress.*|Connected|Calling)$\")"  ));

        mobile.put(ElementKey.VIDEO_CALL_BUTTON,
                AppiumBy.androidUIAutomator("new UiSelector().className(\"android.widget.LinearLayout\").instance(3)"));

        mobile.put(ElementKey.CALL_TIMER,
                AppiumBy.xpath(
                        "//android.widget.TextView[@package='com.ale.rainbow' " +
                                "and contains(@text, ':') " +
                                "and not(contains(@text, ' ')) " +
                                "and string-length(@text) <= 5]"
                ));
        mobile.put(ElementKey.ANSWER_VIDEO,
                AppiumBy.id(
                        "com.ale.rainbow:id/button_answer_video"));
        mobile.put(ElementKey.MORE_OPTION,
                AppiumBy.androidUIAutomator(
                        "new UiSelector().description(\"More options\")"));
        mobile.put(ElementKey.BACK_TO_CALL,
                AppiumBy.id(
                        "com.ale.rainbow:id/backToCallButton"));
        mobile.put(ElementKey.ACCEPT_CALL_BUTTON,
                AppiumBy.id(
                        "com.ale.rainbow:id/button_answer"));

        mobile.put(ElementKey.HANG_UP_BUTTON,
                AppiumBy.androidUIAutomator("new UiSelector().resourceId(\"com.ale.rainbow:id/button_hangup\")"));
        mobile.put(ElementKey.REJECT_CALL_BUTTON,
                AppiumBy.id("com.ale.rainbow:id/button_reject"));

        mobile.put(ElementKey.REMOTE_VIDEO_CONTAINER,
                By.id(
                        "com.ale.rainbow:id/videoContainer"));//enable,display
        mobile.put(ElementKey.SHARE_SCREEN_GRID,//enable ,display
                By.id("com.ale.rainbow:id/surfaceview"));
        mobile.put(ElementKey.VIDEO_UPGRADE_BUTTON,
               By.id("com.ale.rainbow:id/buttonAddVideo"));

        mobile.put(ElementKey.ALLOW_ACCESS_CAMERA,
                By.id("com.ale.rainbow:id/buttonAddVideo"));

        mobile.put(ElementKey.ADD_PARTICIPANTS_BUTTON,
                AppiumBy.accessibilityId("Add participants"));
        mobile.put(
                ElementKey.CALL_PARTICIPANT_OPTION_BUTTON,
                AppiumBy.androidUIAutomator("new UiSelector().className(\"android.widget.LinearLayout\").instance(0)")
        );
        mobile.put(
                ElementKey.EMAIL_FIELD,
                AppiumBy.androidUIAutomator("new UiSelector().className(\"android.widget.EditText\").instance(0)")
        );

        mobile.put(
                ElementKey.NAVIGATE_BACK,
                AppiumBy.accessibilityId("Navigate up")
        );
        mobile.put(
                ElementKey.NAVIGATE_BACK_CONVERSATION,
                AppiumBy.androidUIAutomator("new UiSelector().className(\"android.view.View\").instance(24)")
        );
        mobile.put(
                ElementKey.ACTIVE_CONFERENCE,
                AppiumBy.androidUIAutomator("new UiSelector().className(\"android.view.View\").instance(17)")
        );
        mobile.put(
                ElementKey.LAST_BUBBLE_CREATED,
                AppiumBy.xpath("//androidx.recyclerview.widget.RecyclerView[@resource-id=\"com.ale.rainbow:id/rooms_list_recyclerview\"]/android.widget.Button[1]")
        );
        mobile.put(ElementKey.MUTE_BUTTON, AppiumBy.xpath("//android.widget.Button[@content-desc=\"Mute\"]"));
        mobile.put(ElementKey.UNMUTE_BUTTON, AppiumBy.xpath("//android.widget.Button[@content-desc=\"Unmute\"]"));
        mobile.put(
                ElementKey.START_CONFERENCE,
                AppiumBy.androidUIAutomator("new UiSelector().className(\"android.view.View\").instance(24)")
        );
        mobile.put(
                ElementKey.SUMMARY_COBY_BUTTON,
                AppiumBy.androidUIAutomator("new UiSelector().className(\"android.view.View\").instance(3)")
        );
        mobile.put(
                ElementKey.TURNOFF_INCOMING_SHARING,
                AppiumBy.androidUIAutomator("new UiSelector().className(\"android.widget.LinearLayout\").instance(8)")
        );
        mobile.put(
                ElementKey.TURN_OFF_INCOMING_VIDEO,
                AppiumBy.androidUIAutomator("new UiSelector().className(\"android.widget.LinearLayout\").instance(6)")
        );
        mobile.put(
                ElementKey.PARTICIPANT_TALKING_TIME_VALUE,
                AppiumBy.androidUIAutomator("new UiSelector().resourceId(\"com.ale.rainbow:id/durationTextView\").instance(0)"));
        mobile.put(ElementKey.BUBBLES_TAB, AppiumBy.accessibilityId("Your bubbles"));

        mobile.put(
                ElementKey.CONFERENCE_SETTINGS,
                AppiumBy.androidUIAutomator("new UiSelector().className(\"android.view.View\").instance(19)")
        );
        mobile.put(
                ElementKey.MEMBER_LOCATOR,
                AppiumBy.accessibilityId("Mosaab Odeh, Member")
        );
        //com.ale.rainbow:id/surfaceview

        mobile.put(
                ElementKey.SHARE_SCREEN_APPEAR,
                AppiumBy.androidUIAutomator("new UiSelector().className(\"android.view.ViewGroup\").instance(4)")
        );
        mobile.put(
                ElementKey.VIEW_FULL_SHARING_SCREEN,
                AppiumBy.accessibilityId("Show the sharing only")
        );
        mobile.put(
                ElementKey.SHARE_SCREEN_BUTTON,
                AppiumBy.androidUIAutomator("new UiSelector().className(\"android.widget.LinearLayout\").instance(4)")
        );
        mobile.put(
                ElementKey.START_OK_APPLY,
                AppiumBy.androidUIAutomator("new UiSelector().resourceId(\"android:id/button1\")")
        );
        mobile.put(
                ElementKey.GRID_VIEW,
                By.xpath("//android.widget.TextView[@resource-id=\"com.ale.rainbow:id/title\" and @text=\"Grid view\"]"));
        mobile.put(
                ElementKey.PASSWORD_FIELD,
                By.xpath("//android.widget.EditText[.//android.widget.TextView[@text='Password'] or @text='Password']"));
        mobile.put(
                ElementKey.FIRST_TIMER,
                By.xpath(  "(//android.widget.TextView[@resource-id='com.ale.rainbow:id/durationTextView' " +
                        "and contains(@text, ':') " +
                        "and string-length(@text)=5])[1]"));

        mobile.put(
                ElementKey.SECOND_TIMER,
                By.xpath(     "(//android.widget.TextView[@resource-id='com.ale.rainbow:id/durationTextView' " +
                        "and contains(@text, ':') " +
                        "and string-length(@text)=5])[2]"));

        mobile.put(
                ElementKey.CONTINUE_BUTTON,
                AppiumBy.androidUIAutomator("new UiSelector().className(\"android.view.View\").instance(7)")
        );
        mobile.put(ElementKey.CONTINUE, AppiumBy.accessibilityId("Continue"));
        mobile.put(ElementKey.LOGIN_SUBMIT_BUTTON,
                AppiumBy.androidUIAutomator("new UiSelector().className(\"android.view.View\").instance(10)"));
        mobile.put(ElementKey.RAISE_HAND,
                AppiumBy.androidUIAutomator("new UiSelector().resourceId(\"com.ale.rainbow:id/content\").instance(0)"));
        mobile.put(ElementKey.SPEAKER_ONLY_MODE,
                AppiumBy.androidUIAutomator("new UiSelector().resourceId(\"com.ale.rainbow:id/content\").instance(1)"));
        mobile.put(ElementKey.MUTE_ALL,
                AppiumBy.androidUIAutomator("new UiSelector().className(\"android.widget.LinearLayout\").instance(6)"));
        mobile.put(ElementKey.RECODE_WITH_TRANSCRIPT,
                AppiumBy.androidUIAutomator("new UiSelector().className(\"android.widget.LinearLayout\").instance(14)"));
        mobile.put(ElementKey.LOCK_MEETING,
                AppiumBy.androidUIAutomator("new UiSelector().resourceId(\"com.ale.rainbow:id/content\").instance(6)"));
        mobile.put(ElementKey.PARTICIPANT_LIST,
                AppiumBy.androidUIAutomator("new UiSelector().className(\"android.widget.LinearLayout\").instance(18)"));
        mobile.put(ElementKey.TALKING_TIME,
                AppiumBy.androidUIAutomator("new UiSelector().className(\"android.widget.LinearLayout\").instance(20)"));
        mobile.put(ElementKey.MEETING_OPTION,
                AppiumBy.androidUIAutomator("new UiSelector().className(\"android.widget.LinearLayout\").instance(22)"));
        mobile.put(ElementKey.TRANSCRIPTION_SWITCH,
                AppiumBy.androidUIAutomator("new UiSelector().resourceId(\"com.ale.rainbow:id/transcription_switch\")"));
        mobile.put(
                ElementKey.RECORD_INDICATOR,
                AppiumBy.androidUIAutomator("new UiSelector().resourceId(\"com.ale.rainbow:id/recordingIndicatorText\")"));
        mobile.put(
                ElementKey.STOP_RECORDING,
                AppiumBy.androidUIAutomator("new UiSelector().className(\"android.widget.LinearLayout\").instance(16)"));
        mobile.put(
                ElementKey.RECORD_INFORMATION_MESSAGE,
                AppiumBy.androidUIAutomator("new UiSelector().resourceId(\"com.ale.rainbow:id/disclaimer_text\")"));
        mobile.put(
                ElementKey.SUMMARY_SWITCH,
                AppiumBy.androidUIAutomator("new UiSelector().resourceId(\"com.ale.rainbow:id/summary_switch\")"));
        mobile.put(
                ElementKey.DELETE_RECORD_SWITCH,
                AppiumBy.androidUIAutomator("new UiSelector().resourceId(\"com.ale.rainbow:id/auto_delete_recording_switch\")"));
        mobile.put(
                ElementKey.MUTE_COMPARTMENT_UPON_ENTRY,
                AppiumBy.androidUIAutomator("new UiSelector().text(\"Mute participants upon entry\")"));
        mobile.put(
                ElementKey.PLAY_SOUND_ENTRY,
                AppiumBy.androidUIAutomator("new UiSelector().text(\"Play an entry tone\")"));
        mobile.put(
                ElementKey.SHARE_LINK, AppiumBy.accessibilityId("Share"));
        mobile.put(ElementKey.COPY_BUTTON,
                AppiumBy.androidUIAutomator("new UiSelector().resourceId(\"android:id/sem_chooser_chip_button1\")"));
        mobile.put(ElementKey.ROOM_PASSWORD,
                AppiumBy.androidUIAutomator("new UiSelector().resourceId(\"com.ale.rainbow:id/enable_password\")"));
        mobile.put(ElementKey.COPY_MEETING_PASSWORD, AppiumBy.id("com.ale.rainbow:id/password_copy"));
        mobile.put(ElementKey.REGENERATE_NEW_PASSWORD, AppiumBy.accessibilityId("Reset password"));
        mobile.put(ElementKey.WAITING_ROOM,
                AppiumBy.androidUIAutomator("new UiSelector().resourceId(\"com.ale.rainbow:id/enable_lobby\")"));

        mobile.put(ElementKey.SHARE_BUTTON,
                AppiumBy.androidUIAutomator("new UiSelector().className(\"android.view.ViewGroup\").instance(2)"));
        mobile.put(ElementKey.SHARE_WITH_EVERYONE,
                AppiumBy.accessibilityId("With everyone, Anyone with this link can join this bubble."));

        mobile.put(ElementKey.SELECT_LANGUAGE, AppiumBy.androidUIAutomator("new UiSelector().resourceId(\"com.ale.rainbow:id/drop_down\")"));
                
        mobile.put(
                ElementKey.MANAGE_RASE_HAND,
                AppiumBy.accessibilityId("Manage raised hands"));
        mobile.put(
                ElementKey.LOWER_ALL_HANDS,
                AppiumBy.androidUIAutomator("new UiSelector().className(\"com.google.android.material.button.MaterialButton\").instance(1)"));
        mobile.put(
                ElementKey.SHOW_RAISED_HANDS,
                AppiumBy.androidUIAutomator("new UiSelector().className(\"com.google.android.material.button.MaterialButton\").instance(0)"));
        mobile.put(ElementKey.MEETING_LOCK,
                AppiumBy.accessibilityId("Meeting locked"));

        mobile.put(ElementKey.PARTICIPANT_NAME,
                AppiumBy.xpath   ("//android.widget.TextView[@resource-id='com.ale.rainbow:id/participant_display_name' and @text='%s']"));


        REGISTRY.put("android", mobile);
    }

    public static By get(ElementKey key) {
        Map<ElementKey, By> map = REGISTRY.get("android");

        if (map == null) {
            throw new IllegalArgumentException(
                    "No locator registry found for platform: android");
        }
        By locator = map.get(key);
        if (locator == null) {
            throw new IllegalArgumentException(
                    "Locator not found for key: " + key + " on platform: android");
        }

        return locator;
    }
    public static By getRaisedHandCountLocator(int count) {
        return AppiumBy.xpath(String.format(
                "//android.widget.TextView[@resource-id='com.ale.rainbow:id/participant_hand_raised_number' and @text='%d']",
                count
        ));
    }


}