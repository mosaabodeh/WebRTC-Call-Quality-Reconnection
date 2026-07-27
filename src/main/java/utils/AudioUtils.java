package utils;

import io.appium.java_client.android.AndroidDriver;
import javax.sound.sampled.*;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class AudioUtils {

    private static Clip activeClip;
    private static Process activeProcess;


    public static synchronized void startAudio(String localAudioFilePath) {
        // Stop any audio currently playing before starting a new one
        stopAudio();

        new Thread(() -> {
            try {
                File audioFile = new File(localAudioFilePath);

                if (!audioFile.exists()) {
                    System.err.println("Audio file not found at: " + audioFile.getAbsolutePath());
                    return;
                }

                // WAV/AU formats use Java Sound API
                if (localAudioFilePath.endsWith(".wav") || localAudioFilePath.endsWith(".au")) {
                    AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
                    activeClip = AudioSystem.getClip();
                    activeClip.open(audioStream);
                    activeClip.start();
                } else {
                    // WEBM/MP3 formats use the native OS media player
                    String os = System.getProperty("os.name").toLowerCase();
                    if (os.contains("mac")) {
                        activeProcess = new ProcessBuilder("afplay", audioFile.getAbsolutePath()).start();
                    } else if (os.contains("win")) {
                        activeProcess = new ProcessBuilder("cmd", "/c", "start", "", audioFile.getAbsolutePath()).start();
                    } else {
                        activeProcess = new ProcessBuilder("paplay", audioFile.getAbsolutePath()).start();
                    }
                }
            } catch (Exception e) {
                System.err.println("Failed to start audio: " + e.getMessage());
            }
        }).start();
    }


    public static synchronized void stopAudio() {
        try {
            // Stop Java WAV clip
            if (activeClip != null) {
                if (activeClip.isRunning()) {
                    activeClip.stop();
                }
                activeClip.close();
                activeClip = null;
            }

            // Kill external player process (macOS / Linux / Windows)
            if (activeProcess != null) {
                if (activeProcess.isAlive()) {
                    activeProcess.destroyForcibly();
                }
                activeProcess = null;
            }

            // Backup process cleanup for Mac OS player
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("mac")) {
                Runtime.getRuntime().exec(new String[]{"killall", "afplay"});
            }
        } catch (Exception e) {
            System.err.println("Failed to stop audio: " + e.getMessage());
        }
    }


    public static void pushAndPlayOnAndroid(AndroidDriver driver, String relativeResourcePath, String deviceFileName, String mimeType) {
        String localPath = System.getProperty("user.dir") + relativeResourcePath;
        File localFile = new File(localPath);

        if (!localFile.exists()) {
            System.err.println("Local audio file not found: " + localPath);
            return;
        }

        String remotePath = "/sdcard/Download/" + deviceFileName;

        try {
            driver.pushFile(remotePath, localFile);

            Map<String, Object> args = new HashMap<>();
            args.put("command", "am");
            args.put("args", Arrays.asList(
                    "start",
                    "-a", "android.intent.action.VIEW",
                    "-d", "file://" + remotePath,
                    "-t", mimeType
            ));
            driver.executeScript("mobile: shell", args);

        } catch (Exception e) {
            System.err.println("Failed to push and play audio on device: " + e.getMessage());
        }
    }
}