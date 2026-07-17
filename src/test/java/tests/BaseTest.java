package tests;

import drivers.DeviceManager;
import drivers.DriverFactory;
import drivers.DriverFactory.DeviceConfig;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.service.local.AppiumDriverLocalService;
import io.appium.java_client.service.local.AppiumServiceBuilder;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.*;
import pages.locators.ElementKey;
import pages.locators.ElementRegistry;
import utils.ConfigReader;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

public class BaseTest {

    private static final Logger logger = LoggerFactory.getLogger(BaseTest.class);
    private static final int ADB_COMMAND_TIMEOUT_SECONDS = 5;
    protected static AppiumDriverLocalService appiumServer;
    private static String appPackage;

    @BeforeClass
    public void setUpDevices() {
        appPackage = ConfigReader.getProperty("app.package");
        List<String> connectedUdids = getPhysicallyConnectedDevices();
        logger.info("Devices physically connected to host: {}", connectedUdids);

        List<DeviceConfig> allConfigs = List.of(
                DriverFactory.loadConfig("A"),
                DriverFactory.loadConfig("B"));

        List<DeviceConfig> activeConfigs = allConfigs.stream()
                .filter(c -> connectedUdids.contains(c.udid()))
                .toList();

        if (activeConfigs.isEmpty()) {
            throw new IllegalStateException("No configured devices are physically connected! Please connect at least one device.");
        }

        activeConfigs.forEach(c -> logger.info("Device {} ({}) will be used.", c.label(), c.udid()));
        allConfigs.stream()
                .filter(c -> !activeConfigs.contains(c))
                .forEach(c -> logger.warn("Device {} ({}) is configured but NOT physically connected. Skipping.", c.label(), c.udid()));

        logger.info("Running pre-test port and device cleanup for connected devices...");
        activeConfigs.parallelStream().forEach(c -> cleanDevicePorts(c.udid()));

        startAppiumServer();

        final URL serverUrl = appiumServer.getUrl();
        final Duration implicitWait = Duration.ofSeconds(
                Long.parseLong(ConfigReader.getProperty("implicit.wait", "10")));
        final long staggerMs = Long.parseLong(ConfigReader.getProperty("device.init.staggerMs", "4000"));
        final long bootTimeoutSec = Long.parseLong(ConfigReader.getProperty("device.init.timeoutSeconds", "60"));

        boolean isAActive = activeConfigs.stream().anyMatch(c -> c.label().equals("A"));
        boolean isBActive = activeConfigs.stream().anyMatch(c -> c.label().equals("B"));

        List<CompletableFuture<AndroidDriver>> activeFutures = new ArrayList<>();

        if (isAActive) {
            DeviceConfig configA = allConfigs.stream().filter(c -> c.label().equals("A")).findFirst().orElseThrow();
            CompletableFuture<AndroidDriver> futureA = initializeDevice(configA, serverUrl, implicitWait, 0);

            futureA.whenComplete((driver, ex) -> {
                if (driver != null) {
                    DeviceManager.setDriverA(driver);
                    logger.info("Device A registered successfully in DeviceManager.");
                }
            });
            activeFutures.add(futureA);
        }

        if (isBActive) {
            DeviceConfig configB = allConfigs.stream().filter(c -> c.label().equals("B")).findFirst().orElseThrow();
            long delay = isAActive ? staggerMs : 0;
            CompletableFuture<AndroidDriver> futureB = initializeDevice(configB, serverUrl, implicitWait, delay);
            futureB.whenComplete((driver, ex) -> {
                if (driver != null) {
                    DeviceManager.setDriverB(driver);
                    logger.info("Device B registered successfully in DeviceManager.");
                }
            });
            activeFutures.add(futureB);
        }

        try {
            CompletableFuture.allOf(activeFutures.toArray(new CompletableFuture[0]))
                    .get(bootTimeoutSec, TimeUnit.SECONDS);
            logger.info("Connected devices registered successfully! Test suite ready.");
        } catch (Exception e) {
            logger.error("Device startup sequence failed. Aborting test execution.", e);

            DeviceManager.unload();
            throw new RuntimeException("Could not boot devices successfully.", e);
        }
    }

    @BeforeMethod
    public void startAppBeforeTest() {
        logger.info("♻️ Ensuring application is launched in foreground before test...");
        launchApp(DeviceManager.getDriverA());
        launchApp(DeviceManager.getDriverB());
    }

    @AfterMethod(alwaysRun = true)
    public void cleanUpAfterTestMethod() {
        logger.info("🧹 Test method execution finished. Resetting application states...");
        terminateAppSafely(DeviceManager.getDriverA());
        terminateAppSafely(DeviceManager.getDriverB());
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        logger.info("🛑 Destroying active driver sessions...");
        quitDriverSafely(DeviceManager.getDriverA());
        quitDriverSafely(DeviceManager.getDriverB());
        DeviceManager.clear();

        logger.info("Shutting down Appium server...");
        stopAppiumServer();
    }

    private void launchApp(AndroidDriver driver) {
        if (driver != null) {
            try {
                driver.activateApp(appPackage);
            } catch (Exception e) {
                logger.warn("Failed to activate app on device: {}", e.getMessage());
            }
        }
    }

    private void terminateAppSafely(AndroidDriver driver) {
        if (driver != null) {
            try {
                logger.info("Closing application: {}", appPackage);
                driver.terminateApp(appPackage);
            } catch (Exception e) {
                logger.warn("Failed to terminate app: {}", e.getMessage());
            }
        }
    }

    private void quitDriverSafely(AndroidDriver driver) {
        if (driver != null) {
            try {
                driver.quit();
            } catch (Exception e) {
                logger.warn("Error while quitting driver session: {}", e.getMessage());
            }
        }
    }

    protected boolean isUserAlreadyLoggedIn(AndroidDriver driver) {
        try {
            WebDriverWait quickWait = new WebDriverWait(driver, Duration.ofSeconds(4));
            quickWait.until(ExpectedConditions.visibilityOfElementLocated(ElementRegistry.get(ElementKey.SEARCHBAR)));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private List<String> getPhysicallyConnectedDevices() {
        List<String> connectedUdids = new ArrayList<>();
        try {
            Process process = new ProcessBuilder("adb", "devices")
                    .redirectErrorStream(true)
                    .start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.endsWith("device")) {
                        connectedUdids.add(line.split("\\s+")[0]);
                    }
                }
            }
            process.waitFor(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            logger.warn("Could not fetch physically connected devices via ADB: {}", e.getMessage());
        }
        return connectedUdids;
    }

    private CompletableFuture<AndroidDriver> initializeDevice(DeviceConfig config, URL serverUrl,
                                                              Duration implicitWait, long startDelayMs) {
        var executor = startDelayMs > 0
                ? CompletableFuture.delayedExecutor(startDelayMs, TimeUnit.MILLISECONDS)
                : ForkJoinPool.commonPool();

        return CompletableFuture.supplyAsync(() -> {
            logger.info("Starting boot for Device {} ({})...", config.label(), config.udid());
            try {
                AndroidDriver driver = DriverFactory.create(config, serverUrl, implicitWait);
                logger.info("Device {} is fully active and ready.", config.label());
                return driver;
            } catch (Exception e) {
                logger.error("Critical failure initializing Device {} ({})", config.label(), config.udid(), e);
                throw new RuntimeException("Device " + config.label() + " failed startup.", e);
            }
        }, executor);
    }

    private void cleanDevicePorts(String udid) {
        if (udid == null || udid.isBlank()) return;
        logger.debug("Clearing ADB forwards & killing orphaned servers on: {}", udid);
        executeCommand("adb", "-s", udid, "forward", "--remove-all");
        executeCommand("adb", "-s", udid, "shell", "am", "force-stop", "io.appium.uiautomator2.server");
        executeCommand("adb", "-s", udid, "shell", "am", "force-stop", "io.appium.uiautomator2.server.test");
    }

    private void executeCommand(String... command) {
        try {
            Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
            boolean finished = process.waitFor(ADB_COMMAND_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                logger.warn("Command timed out and was killed: {}", String.join(" ", command));
            } else if (process.exitValue() != 0) {
                logger.debug("Command exited with {}: {}", process.exitValue(), String.join(" ", command));
            }
        } catch (Exception e) {
            logger.warn("Non-fatal error executing command [{}]: {}", String.join(" ", command), e.getMessage());
        }
    }

    protected void startAppiumServer() {
        if (appiumServer == null) {
            appiumServer = new AppiumServiceBuilder()
                    .withArgument(() -> "--relaxed-security")
                    .build();
            appiumServer.start();
            logger.info("🚀 Appium Server started with --relaxed-security enabled.");
        }
    }

    protected void stopAppiumServer() {
        if (appiumServer != null && appiumServer.isRunning()) {
            appiumServer.stop();
            logger.info("🛑 Appium Server stopped.");
        }
        appiumServer = null; // critical: allows a fresh server for the next test class
    }
}