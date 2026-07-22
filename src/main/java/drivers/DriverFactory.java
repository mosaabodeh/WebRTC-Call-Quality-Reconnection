package drivers;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import utils.ConfigReader;

import java.net.URL;
import java.time.Duration;

public class DriverFactory {

    public record DeviceConfig(String label, String udid, String systemPort) {}

    public static DeviceConfig loadConfig(String label) {
        String udid = ConfigReader.getProperty("device." + label + ".udid");
        String systemPort = ConfigReader.getProperty("device." + label + ".systemPort");
        return new DeviceConfig(label, udid, systemPort);
    }

    public static AndroidDriver create(DeviceConfig config, URL serverUrl, Duration implicitWait) {
        UiAutomator2Options options = new UiAutomator2Options();
        options.setUdid(config.udid());
        options.setSystemPort(Integer.parseInt(config.systemPort()));

        String appPackage = ConfigReader.getProperty("app.package");
        options.setAppPackage(appPackage);

        String appActivity = ConfigReader.getProperty("app.activity");
        if (appActivity != null && !appActivity.isEmpty()) {
            options.setAppActivity(appActivity);
        }

        options.setCapability("appium:shouldTerminateApp", false);
        options.setCapability("appium:noReset", true);
        options.setCapability("appium:dontStopAppOnReset", true);
        options.setCapability("appium:newCommandTimeout", 300);
        options.setCapability("appium:autoGrantPermissions", true);

        AndroidDriver driver = new AndroidDriver(serverUrl, options);
        driver.manage().timeouts().implicitlyWait(implicitWait);
        return driver;
    }
}