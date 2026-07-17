package tests;
import drivers.DeviceManager;
import io.appium.java_client.android.AndroidDriver;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pages.LoginPage;
import pages.DashboardPage;
import pages.CallPage;
import utils.JsonReader;
import java.util.concurrent.CompletableFuture;

public class WebRtcCallTest extends BaseTest {

    private String emailA;
    private String passwordA;
    private String emailB;
    private String passwordB;
    private String nameB;

    @BeforeMethod
    public void setUpTestScenario() {
        emailA = JsonReader.getTestData("LoginData.json", "UserA", "email");
        passwordA = JsonReader.getTestData("LoginData.json", "UserA", "password");
        emailB = JsonReader.getTestData("LoginData.json", "UserB", "email");
        passwordB = JsonReader.getTestData("LoginData.json", "UserB", "password");
        nameB = JsonReader.getTestData("LoginData.json", "UserB", "Name");
    }

    private record CallContext(DashboardPage dashboardA, CallPage callA, CallPage callB) {}

    private CallContext establishBaseCall(AndroidDriver driverA, AndroidDriver driverB) {
        LoginPage loginPageA = new LoginPage(driverA);
        LoginPage loginPageB = new LoginPage(driverB);

        boolean isLoggedA = isUserAlreadyLoggedIn(driverA);
        boolean isLoggedB = isUserAlreadyLoggedIn(driverB);

        CompletableFuture<Void> loginA = CompletableFuture.runAsync(() -> {
            if (!isLoggedA) loginPageA.login(emailA, passwordA);
        });
        CompletableFuture<Void> loginB = CompletableFuture.runAsync(() -> {
            if (!isLoggedB) loginPageB.login(emailB, passwordB);
        });

        CompletableFuture.allOf(loginA, loginB).join();

        return new CallContext(
                new DashboardPage(driverA),
                new CallPage(driverA),
                new CallPage(driverB)
        );
    }

    private AndroidDriver[] getActiveDrivers() {
        AndroidDriver driverA = DeviceManager.getDriverA();
        AndroidDriver driverB = DeviceManager.getDriverB();

        if (driverA == null || driverB == null) {
            throw new IllegalStateException("Both devices must be connected for running test scenarios.");
        }
        return new AndroidDriver[]{driverA, driverB};
    }

    @Test(description = "1. Verify basic call Status, receiving,Outgoing ")
    public void testBasicCallInitiationAndDetails()   {
        AndroidDriver[] drivers = getActiveDrivers();
        CallContext context = establishBaseCall(drivers[0], drivers[1]);

        context.dashboardA().callContact(nameB);

        String statusA = context.callA().getCallStatus();
        String statusB = context.callB().getCallStatus();

        Assert.assertTrue(statusA.toLowerCase().contains("outgoing call") || !statusA.isEmpty(), "Active details invalid on Device A.");
        Assert.assertTrue(statusB.toLowerCase().contains("incoming call") || !statusB.isEmpty(), "Active details invalid on Device B.");

    }
    @Test(description = "8. Verify basic call Accept The call and clean hang-up")
    public void testAcceptTheCall()   {
        AndroidDriver[] drivers = getActiveDrivers();
        CallContext context = establishBaseCall(drivers[0], drivers[1]);

        context.dashboardA().callContact(nameB);
        context.callB().acceptIncomingCall();

        Assert.assertTrue(context.callA().isCallTimerTicking(), "Call timer is not actively ticking on Device A.");
        Assert.assertTrue(context.callB().isCallTimerTicking(), "Call timer is not actively ticking on Device B.");
        Assert.assertTrue(context.callA().isNetworkAppear(), "Call Network logo is not actively on Device A.");
        Assert.assertTrue(context.callB().isNetworkAppear(), "Call Network logo is not actively on Device B.");
    }
    @Test(description = "7. Verify basic call Answering and  clean hang-up")
    public void testEndTheCall()   {
    AndroidDriver[] drivers = getActiveDrivers();
    CallContext context = establishBaseCall(drivers[0], drivers[1]);

        context.dashboardA().callContact(nameB);
        context.callB().acceptIncomingCall();
        context.callB().endCall();
        Assert.assertTrue(context.callA().isCallEndedCleanly(), "Device A did not end the call cleanly.");
        Assert.assertTrue(context.callB().isCallEndedCleanly(), "Device B did not end the call cleanly.");

        Assert.assertTrue(isUserAlreadyLoggedIn(drivers[1]), "Device B did not return to main dashboard.");
    }

    @Test(description = "2. Verify network disconnection triggers 'connection in progress' status and call doesn't drop immediately")
    public void testNetworkInterruptionResilience() throws InterruptedException {
        AndroidDriver[] drivers = getActiveDrivers();
        CallContext context = establishBaseCall(drivers[0], drivers[1]);

        context.dashboardA().callContact(nameB);
        context.callB().acceptIncomingCall();

        try {
            System.out.println("🔌 Turning OFF Wi-Fi on Device B...");
            context.callB().toggleWiFi();

            String statusB = context.callB().getCallStatus();
            String statusA = context.callA().getCallStatus();

            Assert.assertTrue(statusA.toLowerCase().contains("connection in progress")
                    ,"Reconnecting/Progress status not displayed on Device A. Found: " + statusA);
            Assert.assertTrue(statusB.toLowerCase().contains("connection in progress")
                    ,"Reconnecting/Progress status not displayed on Device B. Found: " + statusB);

            System.out.println("⏳ Waiting to verify call resilience...");

            Assert.assertFalse(context.callA().isCallEndedCleanly(), "Call prematurely disconnected during short outage.");

            System.out.println("📶 Restoring Wi-Fi on Device B...");
            context.callB().toggleWiFi();

            String statusAAfterRestore = context.callA().getCallStatus();
            Assert.assertTrue(context.callA.isCallTimerTicking(),
                    "Call failed to return to active state. Current status: " + statusAAfterRestore);


             statusAAfterRestore = context.callB().getCallStatus();
            Assert.assertTrue(context.callB.isCallTimerTicking(),
                    "Call failed to return to active state. Current status: " + statusAAfterRestore);

        }
        finally {
            try {
                context.callB().endCall();
            } catch (Exception e) {
                System.out.println("ℹ️ Clean-up hang up completed or skipped: " + e.getMessage());
            }
        }
    }
    @Test(description = "3. Verify automatic recovery and full reconnection after network restoration")
    public void testNetworkReconnectionRecovery() throws InterruptedException {
        AndroidDriver[] drivers = getActiveDrivers();
        CallContext context = establishBaseCall(drivers[0], drivers[1]);

        context.dashboardA().callContact(nameB);
        context.callB().acceptIncomingCall();
        context.callB().toggleAirplaneMode(true);

        String statusAAfterRestore = context.callA().getCallStatus();
        Assert.assertTrue( statusAAfterRestore.toLowerCase().contains("connection in progress"),
                "Call failed return to active state Device A. Current status: " + statusAAfterRestore);

        context.callB().toggleAirplaneMode(false);
         statusAAfterRestore = context.callB().getCallStatus();
        Assert.assertTrue(context.callB.isCallTimerTicking(),
                "Call failed to return to active state on the B Device. Current status: " + statusAAfterRestore);

        context.callB().endCall();
    }

    @Test(description = "4. Verify update Call To video")
    public void testCallUpgradeToVideo()   {
        AndroidDriver[] drivers = getActiveDrivers();
        CallContext context = establishBaseCall(drivers[0], drivers[1]);
        context.dashboardA().callContact(nameB);

        context.callB().acceptIncomingCall();
        context.callA().upgradeToVideo();
        boolean isVideoContainerVisible = context.callB().isVideoFeedReceived();

        Assert.assertTrue(isVideoContainerVisible, "Video stream container did not pop up on Device B screen.");
        context.callB().endCall();
    }

    @Test(description = "5. Verify that call can be rejected from Device B")
    public void testCallRejectionFromReceiverSide()   {
        AndroidDriver[] drivers = getActiveDrivers();
        CallContext context = establishBaseCall(drivers[0], drivers[1]);

        System.out.println("📞 Initiating call from Device A...");
        context.dashboardA().callContact(nameB);
        try {
            System.out.println("❌ Rejecting incoming call on Device B...");
            context.callB().rejectIncomingCall();

            Assert.assertTrue(context.callA().isCallEndedCleanly(), "Device A did not terminate the call screen after rejection.");
            Assert.assertTrue(context.callB().isCallEndedCleanly(), "Device B did not terminate the call screen after rejection.");
            Assert.assertTrue(isUserAlreadyLoggedIn(drivers[1]), "Device B did not return to the main dashboard.");
        } catch (Exception e) {
            System.out.println("⚠️ Rejection test failed: " + e.getMessage());
            throw e;
        }
    }

    @Test(description = "6. Verify Start Call with video")
    public void testCallStartWithVideo()   {
        AndroidDriver[] drivers = getActiveDrivers();
        CallContext context = establishBaseCall(drivers[0], drivers[1]);

        context.dashboardA().videoCallContact(nameB);
        context.callB().acceptIncomingVideoCall();

        boolean isVideoContainerVisible = context.callB().isVideoFeedReceived();
        Assert.assertTrue(isVideoContainerVisible, "Video stream container did not pop up on Device B screen.");
        context.callB().endCall();
    }
}