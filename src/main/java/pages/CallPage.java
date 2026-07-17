package pages;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.connection.ConnectionStateBuilder;
import org.openqa.selenium.WebElement;
import pages.locators.ElementKey;
import pages.locators.ElementRegistry;

import java.time.Duration;

public class CallPage extends BasePage {

    public CallPage(AppiumDriver driver) {
        super(driver);
    }

    public void acceptIncomingCall() {
       waitClickable((ElementRegistry.get(ElementKey.ACCEPT_CALL_BUTTON))).click();

    }
    public void acceptIncomingVideoCall() {
        waitClickable(ElementRegistry.get(ElementKey.ANSWER_VIDEO)).click();
    }

    public boolean isCallTimerTicking() {
        if (!isDisplayed(ElementRegistry.get(ElementKey.CALL_TIMER))) return false;

        try {
            String timeOne = waitVisible(ElementRegistry.get(ElementKey.CALL_TIMER)).getText();
            WaiteForTime(1.2);
            String timeTwo = waitVisible(ElementRegistry.get(ElementKey.CALL_TIMER)).getText();
            return !timeOne.equals(timeTwo);
        } catch (Exception e) {
            // Call may have ended between the two samples — treat as "not ticking" instead of crashing the test.
            return false;
        }
    }
    protected void WaiteForTime(double durationOfSecond) throws InterruptedException {
        Thread.sleep(Duration.ofSeconds((long) durationOfSecond));
    }

    public String getCallStatus() {
        try {

            WebElement statusElement = waitVisible(ElementRegistry.get(ElementKey.CALL_STATUS_TEXT));
            String statusText = statusElement.getText();
            System.out.println("📱 Current Call Status: " + statusText);
            return statusText;
        } catch (Exception e) {
            System.out.println("⚠️ Could not fetch call status text: " + e.getMessage());
            return "";
        }
    }
    public void toggleWiFi() throws InterruptedException {
        WaiteForTime(1.2);
        AndroidDriver androidDriver = (AndroidDriver) this.driver;
        androidDriver.toggleWifi();
        System.out.println("🔄 Wi-Fi state toggled.");
        WaiteForTime(3.3);

    }
    public void rejectIncomingCall() {
      waitClickable ( ElementRegistry.get(ElementKey.REJECT_CALL_BUTTON)).click();
    }
    public void toggleAirplaneMode(boolean turnOn) throws InterruptedException {
        WaiteForTime(1.2);
        AndroidDriver androidDriver = (AndroidDriver) this.driver;
        if (turnOn) {
            androidDriver.setConnection(new ConnectionStateBuilder().withAirplaneModeEnabled().build());
            System.out.println("✈️ Airplane Mode turned ON successfully.");
        } else {
            androidDriver.setConnection(new ConnectionStateBuilder().withWiFiEnabled().withDataEnabled().build());
            System.out.println("📡 Airplane Mode turned OFF. Network restored.");
        }
        WaiteForTime(3.1);
    }

    public void upgradeToVideo() {
        waitClickable(ElementRegistry.get(ElementKey.VIDEO_UPGRADE_BUTTON)).click();
    }

    public boolean isVideoFeedReceived() {
        return isDisplayed(ElementRegistry.get(ElementKey.REMOTE_VIDEO_CONTAINER));
    }

    public void endCall() {
        waitClickable(ElementRegistry.get(ElementKey.HANG_UP_BUTTON)).click();
    }

    public boolean isCallEndedCleanly() {
        try {
            boolean isHangUpVisible ;
            boolean isNetworkQuilityAppear ;
            boolean isMoreOptionVisible;

            try {
                isHangUpVisible = waitVisible(ElementRegistry.get(ElementKey.HANG_UP_BUTTON)).isDisplayed();
            } catch (Exception e) {
                isHangUpVisible = false;
            }
            try {
                isNetworkQuilityAppear=waitVisible(ElementRegistry.get(ElementKey.QUALITY_SIGN)).isDisplayed();
            } catch (Exception e) {
                isNetworkQuilityAppear = false;
            }
            try {
                isMoreOptionVisible=waitVisible(ElementRegistry.get(ElementKey.MOREOPTION)).isDisplayed();
            } catch (Exception e) {
                isMoreOptionVisible = false;
            }
            return !isHangUpVisible && !isNetworkQuilityAppear &&!isMoreOptionVisible;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isNetworkAppear() {
    return isDisplayed(ElementRegistry.get(ElementKey.QUALITY_SIGN));
    }
}