package tests;

import drivers.DeviceManager;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.ChromeNavigationUtils;
import utils.AudioUtils;


public class ConferenceCallTest extends BaseTest {
    @Test(priority = 1,description = "user should be able to Add another User to existence call, and update the call To Conference And the added user Appear in Conference List ")
    public void testConferenceCallEstablishment() {
        prepareConference();
    }

    @Test(priority = 2,description = "user should be able to Receive the incoming Video From the Conference users ")
    public void testSharingVideo() {
        prepareConference();

        callB.get().upgradeToVideo();
        boolean isVideoContainerVisible = callA.get().isVideoFeedReceived();
        Assert.assertTrue(isVideoContainerVisible, "Video stream container did not pop up on Device B screen.");

    }

    @Test(priority = 3,description = "user should be able to Receive the incoming sharing screen From the Conference users ")
    public void testSharingScreen() throws InterruptedException {
        prepareConference();

        conB.sharingScreen();
        Assert.assertTrue(conA.isSharingScreen());
          }
    @Test(priority = 4,description = "user should be able to Receive notification that he is muted From the Conference Organizer ")
    public void testMuteAllUser()   {
        prepareConference();

        conA.muteAllUsersInTheConference();
        Assert.assertTrue(conB.isMutedToastAppear(), "The Mute Toast Dosnt Appear yet");

    }
    @Test(priority = 5,description = "user should be able to See the Added User in the participant List Of the Conference ")
    public void testUserExistInParticipantList()   {
        prepareConference();

        callB.get().endCall();
        Assert.assertTrue(callB.get().isCallEndedCleanly(), "Device B did not end the call cleanly.");
    }
    @Test(priority = 6,description = "user should be able to See the actual count of user raised hand in the participant List Of the Conference ")
    public void testCountOfRaisingHand()   {
        prepareConference();

        rasB.raisHand();
        Assert.assertTrue(resA.isTheCounterOfRaisHAndAccurate(), "The Count of Rais hand dose not correct.");


    }
    @Test(priority = 7,description = "user should be able to See the actual count of user raised hand in the participant List Of the Conference ")
    public void testLowerRaisingHand()   {
        prepareConference();
        rasB.raisHand();
        Assert.assertTrue(resA.isAllHandLowered(), "Not All hand lowered Successfully");
    }
    @Test(priority = 8,description = "user should be able to Select The Speaker Only Mode And See changes ")
    public void testSpeakerMode()   {
        prepareConference();
        conA.SpeakerOnlyMode();
        Assert.assertTrue(conA.isGridViewExist(), "The Grid Dose not Exist Dose not Convert to Speaker Mode ");

    }
    @Test(priority = 9,description = "user should be able to Start/Active Record With Transcript ")
    public void testRecordWithTranscriptActivation()   {
        prepareConference();
        Assert.assertTrue(conA.recordWithTranscript(), "The Grid Dose not Exist Dose not Convert to Speaker Mode ");

    }

    @Test(priority = 10,description = "user should be able to real Record With Transcript for audio")
    public void testCallRecordingWithTranscript()   {
        String audioPath = System.getProperty("user.dir") + "/src/test/resources/audio/OneMinutesRecord.mp3";
        prepareConference();
        Assert.assertTrue(conA.recordWithTranscript(), "The Grid Dose not Exist Dose not Convert to Speaker Mode ");

        AudioUtils.startAudio(audioPath);
        conA.waitUntilRecordingFinished();
        AudioUtils.stopAudio();
        conA.stopRecording();
        Assert.assertTrue(conB.isTranscriptTextAccurate(),"there's an error with the transcript feature ");
    }

    @Test(priority = 11,description = "user should be able to Lock the meeting to prevent joining gust when needed ")
    public void testLockMeeting() throws InterruptedException {
        establishBaseCall();
        conA.startLockedConference();
        callB.get().rejectIncomingCall();
        Assert.assertTrue(conA.lockTheMeeting(), "The Meeting Lock icon and toast message dose not appear ");
        Assert.assertTrue(conB.isConferenceLocked(), "Device B can Enter the Meeting Conference Successfully !!!.");
    }

    @Test(priority = 12, description = "User should see valid talking time for first and second participants")
    public void testTalkingTime() throws InterruptedException {
        prepareConference();
        String localAudioPath =  System.getProperty("user.dir") + "/src/test/resources/audio/25 secondVoiceRecord.mp3";
        AudioUtils.startAudio(localAudioPath);

        conB. muteFor(5);
        conA. muteFor(4);
        conB. speakFor(4);

        talkA.openTalkingTimeScreen();
        AudioUtils.stopAudio();
        Assert.assertNotEquals(
                talkA.getParticipantTime(1),
                "00:00",
                "First participant talking time is invalid.");

        Assert.assertNotEquals(
                talkA.getParticipantTime(2),
                "00:00",
                "Second participant talking time is invalid.");
    }

    @Test(priority = 13,description = "user should be able to change The Conference Preferences as he want ")
    public void testShareBubbleWithPreferences() {
        prepareConference();

        conA.configureMeetingOptions(true,true);
        Assert.assertTrue(conB.areMeetingOptionsApplied(true,true));
        conB.clickNavigationBack();

    }

    @Test(priority = 14,description = "user should be able to past the copied link of the conference in google chrome ")
    public void testFillUrlInGoogleChrome()  {
        prepareConference();
        String res=conA.verifySharingLinkStander();
        System.out.println("the share Link is :"+res);
        chromeNavigation.openChromeAndNavigateToFirstLink(res);
        chromeNavigation.reopenRainbow();
        conB.returnToActiveCall();

    }
    @Test(priority = 15,description = "user should be able to change The Conference joining setting And add password, waiting room past the copied link of the conference in google chrome ")
    public void testFillUrlInChromeWithPassword()  {
        prepareConference();

        String res=conA.verifySharingBubbleWithCustomSetting(true,true);
        ChromeNavigationUtils chromeNavigationUtils = new ChromeNavigationUtils(DeviceManager.getDriverB());
        chromeNavigationUtils.openChromeAndNavigateToFirstLink(res);
        chromeNavigationUtils.reopenRainbow();

        conB.returnToActiveCall();

    }

}
