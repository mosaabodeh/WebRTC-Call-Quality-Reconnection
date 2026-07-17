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
        mobile.put(ElementKey.MOREOPTION,
                AppiumBy.id(
                        "More options"));
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

        mobile.put(ElementKey.VIDEO_UPGRADE_BUTTON,
               By.id("com.ale.rainbow:id/buttonAddVideo"));
        mobile.put(ElementKey.ALLOW_ACCESS_CAMERA,
                By.id("com.ale.rainbow:id/buttonAddVideo"));
        mobile.put(
                ElementKey.EMAIL_FIELD,
                AppiumBy.androidUIAutomator("new UiSelector().className(\"android.widget.EditText\").instance(0)")
        );

        mobile.put(
                ElementKey.PASSWORD_FIELD,
                By.xpath("//android.widget.EditText[.//android.widget.TextView[@text='Password'] or @text='Password']"));
        mobile.put(
                ElementKey.CONTINUE_BUTTON,
                AppiumBy.androidUIAutomator("new UiSelector().className(\"android.view.View\").instance(7)")
        );

        mobile.put(
                ElementKey.LOGIN_SUBMIT_BUTTON,
                AppiumBy.androidUIAutomator("new UiSelector().className(\"android.view.View\").instance(10)")
        );

        mobile.put(ElementKey.USER_PROFILE_AVATAR,
                AppiumBy.androidUIAutomator( "new UiSelector().className(\"android.view.View\").instance(1)"));

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
}