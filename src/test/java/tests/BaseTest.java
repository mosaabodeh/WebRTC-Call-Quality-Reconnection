package tests;

import drivers.DeviceManager;
import drivers.DriverFactory;
import drivers.DriverFactory.DeviceConfig;
import drivers.NetworkHelper;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.service.local.AppiumDriverLocalService;
import io.appium.java_client.service.local.AppiumServiceBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.*;

import pages.*;
import utils.ConfigReader;
import utils.JsonReader;
import utils.NotificationUtils;

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
    protected static AppiumDriverLocalService appiumServer;
    private static String appPackage;
    NetworkHelper helperMethodeNetwork = new NetworkHelper();

    protected final ThreadLocal<CallPage> callA = new ThreadLocal<>();
    protected final ThreadLocal<CallPage> callB = new ThreadLocal<>();
    ConferencePage conA;
    ConferencePage conB;
    RaseHandPage resA;
    RaseHandPage rasB;
    TalkingTimePage talkA;
    NotificationUtils notification;
    ChromeNavigationUtils chromeNavigation;

    protected String emailA, passwordA, emailB, passwordB, nameB, nameC, udidB;
    protected DashboardPage dashboardAInstance;

    private static long endCallTimeoutSec;
    private static long terminateTimeoutSec;
    private static long overallCleanupTimeoutSec;

    @BeforeClass
    public void setUpDevices() {
        appPackage = ConfigReader.getProperty("app.package");

        endCallTimeoutSec = Long.parseLong(ConfigReader.getProperty("cleanup.callEnd.timeoutSeconds", "3"));
        terminateTimeoutSec = Long.parseLong(ConfigReader.getProperty("cleanup.appTerminate.timeoutSeconds", "7"));
        overallCleanupTimeoutSec = Long.parseLong(ConfigReader.getProperty("cleanup.overall.timeoutSeconds", "10"));

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
        activeConfigs.parallelStream().forEach(c -> helperMethodeNetwork.cleanDevicePorts(c.udid()));

        startAppiumServer();

        final URL serverUrl = appiumServer.getUrl();
        final Duration implicitWait = Duration.ofSeconds(
                Long.parseLong(ConfigReader.getProperty("implicit.wait", "0")));
        final long staggerMs = Long.parseLong(ConfigReader.getProperty("device.init.staggerMs", "4000"));
        final long bootTimeoutSec = Long.parseLong(ConfigReader.getProperty("device.init.timeoutSeconds", "60"));

        boolean isAActive = activeConfigs.stream().anyMatch(c -> c.label().equals("A"));
        boolean isBActive = activeConfigs.stream().anyMatch(c -> c.label().equals("B"));

        List<CompletableFuture<AndroidDriver>> activeFutures = new ArrayList<>();

        CompletableFuture<AndroidDriver> futureA = null;
        CompletableFuture<AndroidDriver> futureB = null;

        if (isAActive) {
            DeviceConfig configA = allConfigs.stream().filter(c -> c.label().equals("A")).findFirst().orElseThrow();
            futureA = initializeDevice(configA, serverUrl, implicitWait, 0);
            activeFutures.add(futureA);
        }

        if (isBActive) {
            DeviceConfig configB = allConfigs.stream().filter(c -> c.label().equals("B")).findFirst().orElseThrow();
            long delay = isAActive ? staggerMs : 0;
            futureB = initializeDevice(configB, serverUrl, implicitWait, delay);
            activeFutures.add(futureB);
        }

        try {
            CompletableFuture.allOf(activeFutures.toArray(new CompletableFuture[0]))
                    .get(bootTimeoutSec, TimeUnit.SECONDS);

            if (futureA != null) {
                DeviceManager.setDriverA(futureA.join());
                logger.info("Device A registered successfully in DeviceManager.");
            }
            if (futureB != null) {
                DeviceManager.setDriverB(futureB.join());
                logger.info("Device B registered successfully in DeviceManager.");
            }

            logger.info("Connected devices registered successfully! Test suite ready.");
        } catch (Exception e) {
            logger.error("Device startup sequence failed. Aborting test execution.", e);
            DeviceManager.unload();
            throw new RuntimeException("Could not boot devices successfully.", e);
        }
    }

    @BeforeMethod
    public void startAppBeforeTest() {

        AndroidDriver driverA = ensureLiveDriver(DeviceManager.getDriverA(), "A");
        AndroidDriver driverB = ensureLiveDriver(DeviceManager.getDriverB(), "B");

        if (driverA != null) {
            launchApp(driverA);
            callA.set(new CallPage(driverA));
            logger.debug("CallPage A mapped into active ThreadLocal context.");
        }

        if (driverB != null) {
            launchApp(driverB);
            callB.set(new CallPage(driverB));
            logger.debug("CallPage B mapped into active ThreadLocal context.");
        }
    }

    private AndroidDriver ensureLiveDriver(AndroidDriver driver, String label) {
        if (driver == null) {
            return null;
        }
        try {
            driver.getPageSource(); // cheap call; throws if instrumentation is dead
            return driver;
        } catch (Exception e) {
            logger.error("Instrumentation appears dead on Device {}. Recreating session...", label, e);
            try {
                driver.quit();
            } catch (Exception ignored) {
                // already dead; nothing to clean up
            }

            DeviceConfig config = DriverFactory.loadConfig(label);
            Duration implicitWait = Duration.ofSeconds(
                    Long.parseLong(ConfigReader.getProperty("implicit.wait", "0")));

            try {
                AndroidDriver fresh = DriverFactory.create(config, appiumServer.getUrl(), implicitWait);
                if (label.equals("A")) {
                    DeviceManager.setDriverA(fresh);
                } else {
                    DeviceManager.setDriverB(fresh);
                }
                logger.info("Device {} session successfully recreated after instrumentation crash.", label);
                return fresh;
            } catch (Exception recreateFailure) {
                logger.error("Failed to recreate Device {} after instrumentation crash. Test will proceed without it.",
                        label, recreateFailure);
                if (label.equals("A")) {
                    DeviceManager.setDriverA(null);
                } else {
                    DeviceManager.setDriverB(null);
                }
                return null;
            }
        }
    }

    @BeforeMethod
    public void loadCommonTestData() {
        emailA = JsonReader.getTestData("LoginData.json", "UserA", "email");
        passwordA = JsonReader.getTestData("LoginData.json", "UserA", "password");
        emailB = JsonReader.getTestData("LoginData.json", "UserB", "email");
        passwordB = JsonReader.getTestData("LoginData.json", "UserB", "password");
        nameB = JsonReader.getTestData("LoginData.json", "UserB", "Name");
        nameC = JsonReader.getTestData("LoginData.json", "UserA", "Name");

        udidB = ConfigReader.getProperty("device.B.udid");
    }

    protected void establishBaseCall() {
        AndroidDriver driverA = DeviceManager.getDriverA();
        AndroidDriver driverB = DeviceManager.getDriverB();

        if (driverA == null || driverB == null) {
            throw new IllegalStateException("❌ No devices are connected in the current thread context.");
        }
        conA = new ConferencePage(driverA);
        conB = new ConferencePage(driverB);
        resA = new RaseHandPage(driverA);
        rasB = new RaseHandPage(driverB);
        talkA = new TalkingTimePage(driverA);
        chromeNavigation = new ChromeNavigationUtils(driverB);
        notification = new NotificationUtils(driverB);

        List<CompletableFuture<Void>> loginTasks = new ArrayList<>();

        loginTasks.add(CompletableFuture.runAsync(() -> {
            LoginPage loginPageA = new LoginPage(driverA);
            if (!loginPageA.isUserAlreadyLoggedIn()) {
                logger.info("Device A is not logged in. Initiating login procedure...");
                loginPageA.login(emailA, passwordA);
            }
        }));

        loginTasks.add(CompletableFuture.runAsync(() -> {
            LoginPage loginPageB = new LoginPage(driverB);
            if (!loginPageB.isUserAlreadyLoggedIn()) {
                logger.info("Device B is not logged in. Initiating login procedure...");
                loginPageB.login(emailB, passwordB);
            }
        }));

        CompletableFuture.allOf(loginTasks.toArray(new CompletableFuture[0])).join();
        dashboardAInstance = new DashboardPage(driverA);
    }

    protected void audioCall() {
        dashboardAInstance.callContact(nameB);
        callB.get().answerCallAndConfirmStable();
    }

    @AfterMethod(alwaysRun = true)
    public void cleanUpAfterTestMethod() {
        logger.info("Test method execution finished. Resetting application states...");

        List<CompletableFuture<Void>> endCallTasks = new ArrayList<>();

        if (callB.get() != null) {
            endCallTasks.add(CompletableFuture.runAsync(() -> {
                try {
                    callB.get().endCallSilently();
                } catch (Exception ignored) {
                }
            }).orTimeout(endCallTimeoutSec, TimeUnit.SECONDS).exceptionally(ex -> null));
        }
        if (callA.get() != null) {
            endCallTasks.add(CompletableFuture.runAsync(() -> {
                try {
                    callA.get().endCallSilently();
                } catch (Exception ignored) {
                }
            }).orTimeout(endCallTimeoutSec, TimeUnit.SECONDS).exceptionally(ex -> null));
        }

        if (!endCallTasks.isEmpty()) {
            try {
                CompletableFuture.allOf(endCallTasks.toArray(new CompletableFuture[0]))
                        .get(endCallTimeoutSec + 1, TimeUnit.SECONDS);
            } catch (Exception e) {
                logger.warn("End-call cleanup did not complete cleanly, proceeding to termination anyway: {}", e.getMessage());
            }
        }

        AndroidDriver driverA = DeviceManager.getDriverA();
        AndroidDriver driverB = DeviceManager.getDriverB();
        List<CompletableFuture<Void>> cleanupTasks = new ArrayList<>();

        if (driverA != null) {
            cleanupTasks.add(CompletableFuture.runAsync(() -> {
                try {
                    terminateAppSafely(driverA);
                    String udidA = getUdidFromDriver(driverA);
                    helperMethodeNetwork.ensureWifiEnabled(udidA);
                } catch (Exception e) {
                    logger.warn("Error during Device A cleanup thread: {}", e.getMessage());
                }
            }).orTimeout(terminateTimeoutSec, TimeUnit.SECONDS));
        }

        if (driverB != null) {
            cleanupTasks.add(CompletableFuture.runAsync(() -> {
                try {
                    terminateAppSafely(driverB);
                    String udidB = getUdidFromDriver(driverB);
                    helperMethodeNetwork.ensureWifiEnabled(udidB);
                } catch (Exception e) {
                    logger.warn("Error during Device B cleanup thread: {}", e.getMessage());
                }
            }).orTimeout(terminateTimeoutSec, TimeUnit.SECONDS));
        }

        if (!cleanupTasks.isEmpty()) {
            try {
                CompletableFuture.allOf(cleanupTasks.toArray(new CompletableFuture[0]))
                        .get(overallCleanupTimeoutSec, TimeUnit.SECONDS);
            } catch (Exception e) {
                logger.warn("Some cleanup tasks timed out or failed, forcing continuation: {}", e.getMessage());
            }
        }
        callA.remove();
        callB.remove();
        logger.info("Cleanup finished.");
    }

    protected void prepareConference() {
        establishBaseCall();
        audioCall();
        dashboardAInstance.addParticipantToCall(nameC);

        Assert.assertTrue(
                conA.verifyAddedUserInParticipantList(nameC),
                "Participant " + nameC + " was not added.");

        conA.clickNavigationBack();
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        logger.info("🛑 Destroying active driver sessions...");
        if (DeviceManager.getDriverA() != null) {
            quitDriverSafely(DeviceManager.getDriverA());
        }
        if (DeviceManager.getDriverB() != null) {
            quitDriverSafely(DeviceManager.getDriverB());
        }
        DeviceManager.clear();

        logger.info("Shutting down Appium server...");
        stopAppiumServer();
    }

    private String getUdidFromDriver(AndroidDriver driver) {
        try {
            Object udidObj = driver.getCapabilities().getCapability("udid");
            return udidObj != null ? udidObj.toString() : null;
        } catch (Exception e) {
            logger.warn("Could not extract UDID from active driver session: {}", e.getMessage());
            return null;
        }
    }

    void launchApp(AndroidDriver driver) {
        if (driver != null) {
            try {
                driver.activateApp(appPackage);
            } catch (Exception e) {
                logger.warn("Failed to activate app on device: {}", e.getMessage());
            }
        }
    }

    void terminateAppSafely(AndroidDriver driver) {
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
        appiumServer = null;
    }
}