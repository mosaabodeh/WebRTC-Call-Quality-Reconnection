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

        // Capabilities loaded dynamically from ConfigReader with default fallbacks
        options.setCapability("appium:shouldTerminateApp",
                getBooleanProperty("appium.shouldTerminateApp", false));
        options.setCapability("appium:noReset",
                getBooleanProperty("appium.noReset", true));
        options.setCapability("appium:dontStopAppOnReset",
                getBooleanProperty("appium.dontStopAppOnReset", true));
        options.setCapability("appium:newCommandTimeout",
                getIntProperty("appium.newCommandTimeout", 300));
        options.setCapability("appium:autoGrantPermissions",
                getBooleanProperty("appium.autoGrantPermissions", true));

        AndroidDriver driver = new AndroidDriver(serverUrl, options);
        driver.manage().timeouts().implicitlyWait(implicitWait);
        return driver;
    }

    private static boolean getBooleanProperty(String key, boolean defaultValue) {
        String value = ConfigReader.getProperty(key);
        return (value != null && !value.isEmpty()) ? Boolean.parseBoolean(value.trim()) : defaultValue;
    }

    private static int getIntProperty(String key, int defaultValue) {
        String value = ConfigReader.getProperty(key);
        if (value != null && !value.isEmpty()) {
            try {
                return Integer.parseInt(value.trim());
            } catch (NumberFormatException e) {
                System.err.println("Invalid integer format for key '" + key + "', falling back to default: " + defaultValue);
            }
        }
        return defaultValue;
    }
}