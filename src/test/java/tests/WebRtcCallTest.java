package tests;
import org.testng.Assert;
import org.testng.annotations.Test;

public class WebRtcCallTest extends BaseTest {


    @Test(priority = 1, description = "user should be able to see correct call status for receiving and outgoing calls")
    public void testBasicCallInitiationAndDetails() {
        establishBaseCall();

        dashboardAInstance.callContact(nameB);

        String statusA = callA.get().getCallStatus();
        String statusB = callB.get().getCallStatus();

        Assert.assertTrue(statusA.toLowerCase().contains("outgoing call") , "Active details invalid on Device A.");
        Assert.assertTrue(statusB.toLowerCase().contains("incoming call") , "Active details invalid on Device B.");
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
            callB.get().toggleWifi(udidB, false);

            String statusB = callB.get().getCallStatus();
            Assert.assertTrue(statusB.toLowerCase().contains("connection in progress"),
                    "connection in progress status not displayed on Device B. Found: " + statusB);

            System.out.println("📶 Restoring Wi-Fi on Device B...");
            callB.get().toggleWifi(udidB, true);

            String statusAAfterRestore = callA.get().getCallStatus();
            Assert.assertTrue(callA.get().isCallTimerTicking(),
                    "Call failed to return to active state. Current status: " + statusAAfterRestore);

            String statusBAfterRestore = callB.get().getCallStatus();
            Assert.assertTrue(callB.get().isCallTimerTicking(),
                    "Call failed to return to active state. Current status: " + statusBAfterRestore);

            callB.get().endCall();
            Assert.assertTrue(callB.get().isCallEndedCleanly(), "Device B did not end the call cleanly.");

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
        Assert.assertTrue(callB.get().isCallEndedCleanly(), "Device B did not end the call cleanly.");
    }

    @Test(priority = 6, description = "user should be able to upgrade an active audio call to a video call")
    public void testCallUpgradeToVideo() {
        establishBaseCall();
        audioCall();

        callA.get().upgradeToVideo();
        boolean isVideoContainerVisible = callB.get().isVideoFeedReceived();

        Assert.assertTrue(isVideoContainerVisible, "Video stream container did not pop up on Device B screen.");
        callB.get().endCall();
        Assert.assertTrue(callB.get().isCallEndedCleanly(), "Device B did not end the call cleanly.");

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
        Assert.assertTrue(callB.get().isCallEndedCleanly(), "Device B did not end the call cleanly.");

    }

}