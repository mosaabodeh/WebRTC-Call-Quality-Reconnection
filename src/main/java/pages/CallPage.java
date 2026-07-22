package pages;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.connection.ConnectionStateBuilder;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import org.openqa.selenium.support.ui.FluentWait;
import pages.locators.ElementKey;
import pages.locators.ElementRegistry;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


public class CallPage extends BasePage {

    public CallPage(AppiumDriver driver) {
        super(driver);
    }


    public void acceptIncomingCall() {
        waitClickable(ElementRegistry.get(ElementKey.ACCEPT_CALL_BUTTON)).click();
    }
    public void clickAudioCall() {
        waitClickable(ElementRegistry.get(ElementKey.AUDIO_CALL_BUTTON)).click();
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

    public static boolean toggleWifi(String deviceId, boolean turnOn) {
        String state = turnOn ? "enable" : "disable";
        String[] command = {"adb", "-s", deviceId, "shell", "svc", "wifi", state};

        try {
            Process process = new ProcessBuilder(command)
                    .redirectErrorStream(true)
                    .start();

            String output;
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                output = reader.lines().collect(Collectors.joining("\n"));
            }

            boolean finished = process.waitFor(5, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                System.out.println("WiFi toggle command timed out for device {}"+ deviceId);
                return false;
            }

            int exitCode = process.exitValue();
            if (exitCode != 0) {
                System.out.println("WiFi toggle failed on {} (exit={}): {}"+ deviceId+ exitCode+ output);
                return false;
            }

            System.out.println("WiFi state changed to '{}' on {}"+ state+ deviceId);
            return true;

        } catch (IOException e) {
            System.out.println("ADB connection issue while toggling WiFi on {}: {}"+ deviceId+ e.getMessage());
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("WiFi toggle interrupted for device {}"+ deviceId+ e);
            return false;
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


    public void clickAnswer() {
        driver.findElement(By.id("com.ale.rainbow:id/button_answer")).click();
    }
    public void answerCallAndConfirmStable() {
        clickAnswer();

        boolean transitioned = new FluentWait<>(driver)
                .withTimeout(Duration.ofSeconds(8))
                .pollingEvery(Duration.ofMillis(250))
                .ignoring(NoSuchElementException.class)
                .until(d -> {
                    String status = getCallStatus();
                    return status != null && !status.equalsIgnoreCase("Incoming Call");
                });

        if (!transitioned) {
            String appState = queryAppState("com.ale.rainbow");
            throw new AssertionError(
                    "Call never left 'Incoming Call' state after accept click. "
                            + "App state at failure: " + appState
                            + " (4=foreground, 3=background, 1=not running — a value other than 4 means the app died/crashed)"
            );
        }
    }

    private String queryAppState( String appId) {
        Object result = driver.executeScript("mobile: queryAppState",
                Collections.singletonMap("appId", appId));
        return String.valueOf(result);
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