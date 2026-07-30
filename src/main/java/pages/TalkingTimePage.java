package pages;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import pages.locators.ElementKey;
import pages.locators.ElementRegistry;

import java.util.List;

public class TalkingTimePage extends BasePage {

    private static final String DURATION_ID = "com.ale.rainbow:id/durationTextView";

    public TalkingTimePage(AppiumDriver driver) {
        super(driver);
    }

    public void openTalkingTimeScreen() {
        waitClickable(ElementRegistry.get(ElementKey.MORE_OPTION)).click();
        waitClickable(ElementRegistry.get(ElementKey.TALKING_TIME)).click();
    }

    private List<WebElement> getParticipantTimers() {
        return wait.until(
                ExpectedConditions.visibilityOfAllElementsLocatedBy(
                        AppiumBy.id(DURATION_ID)));
    }


    public String getParticipantTime(int participantIndex) {

        List<WebElement> timers = getParticipantTimers();

        if (participantIndex < 1 || participantIndex > timers.size()) {
            throw new IllegalArgumentException(
                    String.format(
                            "Participant index %d is out of range. Found %d participant timer(s).",
                            participantIndex,
                            timers.size()));
        }

        return timers.get(participantIndex - 1).getText().trim();
    }

}