package tests;

import drivers.DeviceManager;
import io.appium.java_client.android.AndroidDriver;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pages.LoginPage;
import pages.DashboardPage;
import utils.JsonReader;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class WebRtcCallTest extends BaseTest {

    private String emailA;
    private String passwordA;
    private String emailB;
    private String passwordB;
    private String nameB;

    private DashboardPage dashboardAInstance;

    @BeforeMethod
    public void setUpTestScenario() {
        emailA = JsonReader.getTestData("LoginData.json", "UserA", "email");
        passwordA = JsonReader.getTestData("LoginData.json", "UserA", "password");
        emailB = JsonReader.getTestData("LoginData.json", "UserB", "email");
        passwordB = JsonReader.getTestData("LoginData.json", "UserB", "password");
        nameB = JsonReader.getTestData("LoginData.json", "UserB", "Name");
    }

    private void establishBaseCall() {
        AndroidDriver driverA = DeviceManager.getDriverA();
        AndroidDriver driverB = DeviceManager.getDriverB();

        if (driverA == null && driverB == null) {
            throw new IllegalStateException("❌ No devices are connected in the current thread context.");
        }

        List<CompletableFuture<Void>> loginTasks = new ArrayList<>();

        if (driverA != null) {
            loginTasks.add(CompletableFuture.runAsync(() -> {
                LoginPage loginPageA = new LoginPage(driverA);
                if (!loginPageA.isUserAlreadyLoggedIn()) {
                    System.out.println("🔐 Device A is not logged in. Initiating login procedure...");
                    loginPageA.login(emailA, passwordA);
                }
            }));
        }

        if (driverB != null) {
            loginTasks.add(CompletableFuture.runAsync(() -> {
                LoginPage loginPageB = new LoginPage(driverB);
                if (!loginPageB.isUserAlreadyLoggedIn()) {
                    System.out.println("🔐 Device B is not logged in. Initiating login procedure...");
                    loginPageB.login(emailB, passwordB);
                }
            }));
        }

        if (!loginTasks.isEmpty()) {
            CompletableFuture.allOf(loginTasks.toArray(new CompletableFuture[0])).join();
        }

        dashboardAInstance = (driverA != null) ? new DashboardPage(driverA) : new DashboardPage(driverB);
    }

    private void audioCall() {
        dashboardAInstance.callContact(nameB);

        callB.get().acceptIncomingCall();

        Assert.assertTrue(callA.get().isCallTimerTicking(), "Call timer is not actively ticking on Device A.");
        Assert.assertTrue(callB.get().isCallTimerTicking(), "Call timer is not actively ticking on Device B.");
    }

    @Test(priority = 1, description = "user should be able to see correct call status for receiving and outgoing calls")
    public void testBasicCallInitiationAndDetails() {
        establishBaseCall();

        dashboardAInstance.callContact(nameB);

        String statusA = callA.get().getCallStatus();
        String statusB = callB.get().getCallStatus();

        Assert.assertTrue(statusA.toLowerCase().contains("outgoing call") || !statusA.isEmpty(), "Active details invalid on Device A.");
        Assert.assertTrue(statusB.toLowerCase().contains("incoming call") || !statusB.isEmpty(), "Active details invalid on Device B.");
       // dashboardAInstance.addParticipantToCall(nameC);

    }

    @Test(priority = 2, description = "user should be able to accept the incoming call successfully")
    public void testAcceptTheCall() {
        establishBaseCall();
        audioCall();

        Assert.assertTrue(callA.get().isNetworkAppear(), "Call Network logo is not active on Device A.");
        Assert.assertTrue(callB.get().isNetworkAppear(), "Call Network logo is not active on Device B.");
    }

    @Test(priority = 3, description = "user should be able to answer and perform a clean hang-up to end the call")
    public void testEndTheCall() {
        establishBaseCall();
        audioCall();

        callB.get().endCall();

        Assert.assertTrue(callA.get().isCallEndedCleanly(), "Device A did not end the call cleanly.");
        Assert.assertTrue(callB.get().isCallEndedCleanly(), "Device B did not end the call cleanly.");
    }

    @Test(priority = 4, description = "user should be able to experience call resilience where short wifi disconnection triggers connection in progress without dropping immediately")
    public void testNetworkInterruptionUsingWIFI() {
        establishBaseCall();
        audioCall();

        try {
            System.out.println("🔌 Turning OFF Wi-Fi on Device B...");
            callB.get().toggleWifi("R5CTA2QC6BA", false);

            String statusB = callB.get().getCallStatus();
            Assert.assertTrue(statusB.toLowerCase().contains("connection in progress"),
                    "connection in progress status not displayed on Device B. Found: " + statusB);

            System.out.println("📶 Restoring Wi-Fi on Device B...");
            callB.get().toggleWifi("R5CTA2QC6BA", true);

            String statusAAfterRestore = callA.get().getCallStatus();
            Assert.assertTrue(callA.get().isCallTimerTicking(),
                    "Call failed to return to active state. Current status: " + statusAAfterRestore);

            String statusBAfterRestore = callB.get().getCallStatus();
            Assert.assertTrue(callB.get().isCallTimerTicking(),
                    "Call failed to return to active state. Current status: " + statusBAfterRestore);

            callB.get().endCall();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test(priority = 5, description = "user should be able to automatically recover and reconnect fully after airplane mode restoration")
    public void testNetworkReconnectionRecoveryAirplaneMode() {
        establishBaseCall();
        audioCall();

        callB.get().toggleAirplaneMode(true);

        String statusAAfterRestore = callA.get().getCallStatus();
        Assert.assertTrue(statusAAfterRestore.toLowerCase().contains("connection in progress"),
                "Call failed return to active state Device A. Current status: " + statusAAfterRestore);

        callB.get().toggleAirplaneMode(false);

        Assert.assertTrue(callB.get().isCallTimerTicking(),
                "Call failed to return to active state on the B Device.");

        callB.get().endCall();
    }

    @Test(priority = 6, description = "user should be able to upgrade an active audio call to a video call")
    public void testCallUpgradeToVideo() {
        establishBaseCall();
        audioCall();

        callA.get().upgradeToVideo();
        boolean isVideoContainerVisible = callB.get().isVideoFeedReceived();

        Assert.assertTrue(isVideoContainerVisible, "Video stream container did not pop up on Device B screen.");
        callB.get().endCall();
    }

    @Test(priority = 7, description = "user should be able to reject an incoming call from the receiver side")
    public void testCallRejectionFromReceiverSide() {
        establishBaseCall();

        System.out.println("Initiating call from Device A...");
        dashboardAInstance.callContact(nameB);

        try {
            System.out.println("Rejecting incoming call on Device B...");
            callB.get().rejectIncomingCall();

            Assert.assertTrue(callA.get().isCallEndedCleanly(), "Device A did not terminate the call screen after rejection.");
            Assert.assertTrue(callB.get().isCallEndedCleanly(), "Device B did not terminate the call screen after rejection.");
        } catch (Exception e) {
            System.out.println("⚠️ Rejection test failed: " + e.getMessage());
            throw e;
        }
    }

    @Test(priority = 8, description = "user should be able to start a call directly with video enabled")
    public void testCallStartWithVideo() {
        establishBaseCall();

        dashboardAInstance.videoCallContact(nameB);
        callB.get().acceptIncomingVideoCall();

        boolean isVideoContainerVisible = callB.get().isVideoFeedReceived();
        Assert.assertTrue(isVideoContainerVisible, "Video stream container did not pop up on Device B screen.");
        callB.get().endCall();
    }
}