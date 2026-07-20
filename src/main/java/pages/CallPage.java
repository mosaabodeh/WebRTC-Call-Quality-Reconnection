package pages;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.connection.ConnectionStateBuilder;
import org.openqa.selenium.WebElement;

import pages.locators.ElementKey;
import pages.locators.ElementRegistry;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class CallPage extends BasePage {

    public CallPage(AppiumDriver driver) {
        super(driver);
    }


    public void acceptIncomingCall() {
        waitClickable(ElementRegistry.get(ElementKey.ACCEPT_CALL_BUTTON)).click();

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
    protected static void WaiteForTime(double durationOfSecond) throws InterruptedException {
        Thread.sleep(Duration.ofSeconds((long) durationOfSecond));
    }

    public String getCallStatus() {
        try {

            WebElement statusElement = waitVisible(ElementRegistry.get(ElementKey.CALL_STATUS_TEXT));
            String statusText = statusElement.getText();
            System.out.println("Current Call Status: " + statusText);
            return statusText;
        } catch (Exception e) {
            System.out.println("Could not fetch call status text: " + e.getMessage());
            return "";
        }
    }
    public static void toggleWifi(String deviceId, boolean turnOn) {
        Process process = null;
        try {
            String state = turnOn ? "enabled" : "disabled";
            String[] command = {"adb", "-s", deviceId, "shell", "cmd", "wifi", "set-wifi-enabled", state};

            process = Runtime.getRuntime().exec(command);
            process.waitFor(1005, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            System.err.println("⚠️ WiFi Toggle Command failed: " + e.getMessage());
        } finally {
            if (process != null) {
                process.destroyForcibly();
                try {
                 process.getInputStream().close();
                 process.getOutputStream().close();
                 process.getErrorStream().close();
                }
                catch (Exception ignored) {}
            }
        }
    }


    public void endCallSilently() {
        try {
            org.openqa.selenium.By hangUpBtnCriteria = ElementRegistry.get(ElementKey.HANG_UP_BUTTON);
            if (!driver.findElements(hangUpBtnCriteria).isEmpty() && driver.findElement(hangUpBtnCriteria).isDisplayed()) {
                System.out.println("☎️ Active call detected. Clicking Hang Up...");
                driver.findElement(hangUpBtnCriteria).click();
            }
        } catch (Exception e) {
            System.out.println("Skipped silent hang up: " + e.getMessage());
        }
    }

    public void rejectIncomingCall() {
      waitClickable ( ElementRegistry.get(ElementKey.REJECT_CALL_BUTTON)).click();
    }
    public void toggleAirplaneMode(boolean turnOn) {
        try {
            WaiteForTime(1.2);
            AndroidDriver androidDriver = (AndroidDriver) this.driver;

            if (turnOn) {
                androidDriver.setConnection(new ConnectionStateBuilder().withAirplaneModeEnabled().build());
                System.out.println("✈️ Airplane Mode turned ON successfully.");

                String status = getCallStatus();
                System.out.println("The Result Of Airplane Mode On Is: " + status.toLowerCase().contains("connection in progress"));

            } else {
                androidDriver.setConnection(new ConnectionStateBuilder().withWiFiEnabled().withDataEnabled().build());
                System.out.println("🌐 Airplane Mode turned OFF. Network restored.");
                waitVisible(ElementRegistry.get(ElementKey.CALL_TIMER));
                System.out.println("✅ Call timer is visible, network re-established.");
            }

        } catch (Exception e) {
            System.out.println("⚠️ Error during Airplane Mode toggle or waiting: " + e.getMessage());
        }
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
        boolean isHangUpVisible;
        boolean isNetworkQuilityAppear;
        boolean isMoreOptionVisible;
        try {
            isHangUpVisible = waitVisible(ElementRegistry.get(ElementKey.HANG_UP_BUTTON)).isDisplayed();
            isNetworkQuilityAppear = waitVisible(ElementRegistry.get(ElementKey.QUALITY_SIGN)).isDisplayed();
            isMoreOptionVisible = waitVisible(ElementRegistry.get(ElementKey.MOREOPTION)).isDisplayed();
        } catch (Exception e) {
            isHangUpVisible = false;
            isNetworkQuilityAppear = false;
            isMoreOptionVisible = false;
            System.out.println(e);
        }
        return !isHangUpVisible && !isNetworkQuilityAppear && !isMoreOptionVisible;

    }

    public boolean isNetworkAppear() {
    return isDisplayed(ElementRegistry.get(ElementKey.QUALITY_SIGN));
    }
}