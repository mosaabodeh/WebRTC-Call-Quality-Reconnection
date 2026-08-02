package tests;
import org.testng.Assert;
import org.testng.annotations.Test;
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
    public void testSharingScreen()   {
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

    @Test(priority = 5,description = "user should be able to verify Turned Off Incoming Video ")
    public void testTurningOffIncomingVideo()   {
        prepareConference();
        callB.get().upgradeToVideo();
        conA.turnedOffIncomingVideo();
        Assert.assertTrue(callA.get().isVideoFeedReceived(), "The Sharing Video Still Appear");
    }

    @Test(priority = 6,description = "user should be able to See the actual count of user raised hand in the participant List Of the Conference ")
    public void testCountOfRaisingHand()   {
        prepareConference();
        rasB.raisHand();
        Assert.assertTrue(resA.isTheCounterOfRaisHAndAccurate(), "The Count of Rais hand dose not correct.");
    }

    @Test(priority = 7,description = "user should be able to Lawyer ALL Hands that raised and see change in the participant List Of the Conference ")
    public void testLowerRaisingHand()   {
        prepareConference();
        rasB.raisHand();
        Assert.assertTrue(resA.isAllHandLowered(), "Not All hand lowered Successfully");
    }

    @Test(priority = 8,description = "user should be able to Select The Speaker Only Mode And See changes ")
    public void testSpeakerMode()   {
        prepareConference();
        conA.speakerOnlyMode();
        Assert.assertTrue(conA.isGridViewExist(), "The Grid Dose not Exist Dose not Convert to Speaker Mode ");
    }

    @Test(priority = 9,description = "user should be able to Start/Active Record With Transcript ")
    public void testRecordWithTranscriptActivation()   {
        prepareConference();
        Assert.assertTrue(conB.recordWithTranscript(), "The Grid Dose not Exist Dose not Convert to Speaker Mode ");
    }

    @Test(priority = 10,description = "user should be able to real Record With Transcript for audio")
    public void testCallRecordingWithTranscript()   {
        String audioPath = System.getProperty("user.dir") + "/src/test/resources/audio/TTSOL-en-AU-Natasha-20260723-132800.mp3";
        prepareConference();
        Assert.assertTrue(conB.recordWithTranscript(), "The Grid Dose not Exist Dose not Convert to Speaker Mode ");

        AudioUtils.playAndWait(audioPath);
        conB.stopRecording();
        Assert.assertTrue(conB.isTranscriptTextAccurate(),"there's an error with the transcript feature ");
    }

    @Test(priority = 11,description = "user should be able to Lock the meeting to prevent users joining when needed ")
    public void testLockMeeting()   {
        establishBaseCall();
        conA.startLockedConference();
        callB.get().rejectIncomingCall();
        Assert.assertTrue(conA.lockTheMeeting(), "The Meeting Lock icon and toast message dose not appear ");
        Assert.assertTrue(conB.isConferenceLocked(), "Device B can Enter the Meeting Conference Successfully !!!.");
    }

    @Test(priority = 12, description = "User should see valid talking time for first and second participants")
    public void testTalkingTime()   {
        prepareConference();
        String localAudioPath =  System.getProperty("user.dir") + "/src/test/resources/audio/25 secondVoiceRecord.mp3";
        AudioUtils.startAudio(localAudioPath);
        conB. muteFor(5);
        conA. muteFor(4);
        conB. speakFor(3);

        talkA.openTalkingTimeScreen();
        AudioUtils.stopAudio();
        Assert.assertNotEquals(talkA.getParticipantTime(1),"00:00","First participant talking time is invalid.");
        Assert.assertNotEquals(talkA.getParticipantTime(2),"00:00","Second participant talking time is invalid.");
    }

    @Test(priority = 13,description = "user should be able to change The Conference Preferences as he want Mute Enter enter and Play Sound ")
    public void testShareBubbleWithPreferences() {
        prepareConference();

        conA.configureMeetingOptions(true,true);
        Assert.assertTrue(conB.areMeetingOptionsApplied(true,true));
        conB.clickNavigationBack();
    }

    @Test(priority = 14,description = "user should be able to copy The Link from Share Link rainbow past the copied link of the conference in google chrome ")
    public void testFillUrlInGoogleChrome()  {
        prepareConference();
        String res=conA.verifySharingLinkStander();
        System.out.println("the share Link is :"+res);
        chromeNavigation.openChromeAndNavigateToFirstLink(res);
        chromeNavigation.reopenRainbow();
        conB.returnToActiveCall();

    }

    @Test(priority = 15,description = "user should be able to change The Conference joining setting And add password, waiting room past the copied link of the conference in google chrome ")
    public void testFillUrlInChromeWithPassword()   {
        prepareConference();
        conA.verifySharingBubbleWithCustomSetting(true,true);
        String res=conA.shareLink();
        chromeNavigation.openChromeAndNavigateToFirstLink(res);
        chromeNavigation.reopenRainbow();

        conB.returnToActiveCall();
    }

    @Test(priority = 16,description = "user should be able to Turn Off incoming sharing screen and stop se the share in the grid ")
    public void testTurnOffIncomingSharing()   {
        prepareConference();
        conB.sharingScreen();
        conA.turnedOffIncomingSharing();

        Assert.assertFalse(conA.isShareScreenAppear(), "The Sharing sharing screen Still Appear");
    }

    @Test(priority = 17,description = "user should be able to verify the users Rule in the conference ")
    public void testVerifyUSerRoll()   {
        prepareConference();
        conA.participantList();
        Assert.assertTrue(conA.verifyUserRuleInRaibow("Mosaab Teat(.net)","Owner"), "Owner role mismatch");
        Assert.assertTrue(conA.verifyUserRuleInRaibow("Mosaab m odeh","Member"), "Member role mismatch");
        Assert.assertTrue(conA.verifyUserRuleInRaibow("mosaab odeh","Organizer"), "Organizer role mismatch");
        conA.clickNavigationBack();
    }

    @Test(priority = 18,description = "user should be able to verify the users Rule in the conference ")
    public void testVerifyUSerRuleByList()   {
        prepareConference();
        conA.participantList();
        Assert.assertTrue( conA.verifyUserRuleInRainbowUsingList("Mosaab Teat(.net)", "Owner"),"The element Not Found Owner");
        Assert.assertTrue( conA.verifyUserRuleInRainbowUsingList("mosaab odeh", "Organizer"),"The element Not Found organizer");
        Assert.assertTrue( conA.verifyUserRuleInRainbowUsingList("Mosaab m odeh", "Member"),"The element Not Found Member");
        conA.clickNavigationBack();
    }

    @Test(priority = 19,description = "user should be able to verify That the Rainbow Room added the Conference with Organizer Rule")
    public void testVerifyRainbowRoomAdding()   {
        establishBaseCall();
        audioCall();
        conA.addRoomToConference();
        conA.participantList();
        Assert.assertTrue( conA.verifyUserRuleInRainbowUsingList("Test", "Member"),"The element Not Found Member");
        conA.clickNavigationBack();
    }

    @Test(priority = 20,description = "user should be able to verify the missed Call Notification when he app is in the foreground state")
    public void testVerifyMissedCallNotification()   {
        establishBaseCall();
        dashboardAInstance.MissedContact(nameB);
        conA.missCall();
        Assert.assertTrue( conB.isMissedCallNotificationAppear(),"Missed call banner not detected via OCR");
    }

    @Test(priority = 21, description = "User should receive a missed call notification when the app is in the background state")
    public void testVerifyMissedCallNotificationAppInBackground() {
        establishBaseCall();
        dashboardAInstance.MissedContact(nameB);
        notification.sendAppToBackground();
        conA.missCall();
        Assert.assertTrue(notification.isMissedCallNotificationDisplayed( nameC),
                "Missed call notification was not received while app is in background");

        notification.activateApp( "com.ale.rainbow");
    }

    @Test(priority = 22, description = "User should receive a missed call notification when the app is terminated")
    public void testVerifyMissedCallNotificationAppTerminated() {
        establishBaseCall();
        dashboardAInstance.MissedContact(nameB);
        notification.terminateApp("com.ale.rainbow");
        conA.missCall();

        try {
            boolean isNotificationReceived = notification.isMissedCallNotificationDisplayed(nameC);
            Assert.assertTrue(isNotificationReceived,
                    "Missed call notification from " + nameC + " was not displayed while app was terminated.");
        } finally {
            notification.activateApp( "com.ale.rainbow");
        }
    }

    @Test(priority = 23, description = "App icon should indicate the number of missed calls")
    public void testVerifyAppIconMissedCallBadgeCount() {
        establishBaseCall();
        dashboardAInstance.MissedContact(nameB);
        conA.missCall();
        notification.sendAppToBackground();

        Assert.assertTrue( notification.getAppIconBadgeCount() > 0,"App icon badge count does not indicate missed calls");
        notification.activateApp( "com.ale.rainbow");
    }


}
