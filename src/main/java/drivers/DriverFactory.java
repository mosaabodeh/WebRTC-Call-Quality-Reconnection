package drivers;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import utils.ConfigReader;

import java.net.URL;
import java.time.Duration;


public class DriverFactory {

    public record DeviceConfig(String label, String udid, int systemPort,
                               int chromedriverPort, int mjpegServerPort) {}

    public static DeviceConfig loadConfig(String label) {
        return new DeviceConfig(
                label,
                ConfigReader.getProperty("device." + label.toLowerCase() + ".udid"),
                Integer.parseInt(ConfigReader.getProperty("device." + label.toLowerCase() + ".systemPort")),
                Integer.parseInt(ConfigReader.getProperty("device." + label.toLowerCase() + ".chromedriverPort")),
                Integer.parseInt(ConfigReader.getProperty("device." + label.toLowerCase() + ".mjpegPort")));
    }

    public static AndroidDriver create(DeviceConfig config, URL serverUrl, Duration implicitWait) {
        AndroidDriver driver = new AndroidDriver(serverUrl, buildOptions(config));
        driver.manage().timeouts().implicitlyWait(implicitWait);
        return driver;
    }

    public static UiAutomator2Options buildOptions(DeviceConfig config) {
        String appPackage = ConfigReader.getProperty("app.package");

        return new UiAutomator2Options()
                .setUdid(config.udid())
                .setAutomationName(ConfigReader.getProperty("automation.name", "UiAutomator2"))
                .setAppPackage(appPackage)
                .setAppActivity(ConfigReader.getProperty("app.activity"))
                .setSystemPort(config.systemPort())
                .setChromedriverPort(config.chromedriverPort())
                .setMjpegServerPort(config.mjpegServerPort())
                .setClearDeviceLogsOnStart(true)

                .amend("appium:uiautomator2ServerInstallTimeout", 30000)
                .amend("appium:adbExecTimeout", 20000)
                .amend("appium:amStartAsHome", true)

                .amend("appium:skipServerCleanup", false)
                .amend("appium:shouldTerminateApp", true)
                .setNoReset(Boolean.parseBoolean(ConfigReader.getProperty("appium.noReset", "true")))
                .setFullReset(Boolean.parseBoolean(ConfigReader.getProperty("appium.fullReset", "false")));
    }
}