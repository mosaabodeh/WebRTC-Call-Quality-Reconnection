package utils;

import io.appium.java_client.android.AndroidDriver;
import javazoom.jl.player.Player;
import javax.sound.sampled.*;
import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class AudioUtils {

    private static volatile Clip activeClip;
    private static volatile Player activePlayer;
    private static volatile Process activeProcess;


    public static synchronized CompletableFuture<Void> startAudio(String localAudioFilePath) {
        stopAudio();

        CompletableFuture<Void> finished = new CompletableFuture<>();

        new Thread(() -> {
            try {
                File audioFile = new File(localAudioFilePath);

                if (!audioFile.exists()) {
                    System.err.println("Audio file not found at: " + audioFile.getAbsolutePath());
                    finished.complete(null);
                    return;
                }

                if (localAudioFilePath.endsWith(".wav") || localAudioFilePath.endsWith(".au")) {
                    AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
                    Clip clip = AudioSystem.getClip();
                    activeClip = clip;

                    clip.addLineListener(event -> {
                        if (event.getType() == LineEvent.Type.STOP) {
                            finished.complete(null);
                        }
                    });

                    clip.open(audioStream);
                    clip.start();

                } else if (localAudioFilePath.endsWith(".mp3")) {
                    // Pure-Java MP3 decode/playback — play() is synchronous and only
                    // returns when the track truly ends (or close() is called), so
                    // it's not dependent on any OS media app's process lifecycle.
                    try (FileInputStream fis = new FileInputStream(audioFile)) {
                        Player player = new Player(fis);
                        activePlayer = player;
                        player.play(); // blocks this background thread until real end-of-audio
                    }
                    activePlayer = null;
                    finished.complete(null);

                } else {
                    // Fallback for other formats (webm, etc.) via native OS player.
                    // Note: on Windows this may return early for file-association-launched
                    // apps, since "start /wait" only waits on the launcher stub.
                    String os = System.getProperty("os.name").toLowerCase();
                    Process process;
                    if (os.contains("mac")) {
                        process = new ProcessBuilder("afplay", audioFile.getAbsolutePath()).start();
                    } else if (os.contains("win")) {
                        process = new ProcessBuilder("cmd", "/c",
                                "start", "/wait", "", audioFile.getAbsolutePath()).start();
                    } else {
                        process = new ProcessBuilder("paplay", audioFile.getAbsolutePath()).start();
                    }
                    activeProcess = process;
                    process.waitFor();
                    finished.complete(null);
                }
            } catch (Exception e) {
                System.err.println("Failed to start/play audio: " + e.getMessage());
                finished.completeExceptionally(e);
            }
        }, "audio-playback-thread").start();

        return finished;
    }

    public static void playAndWait(String localAudioFilePath) {
        try {
            startAudio(localAudioFilePath).join();
        } catch (Exception e) {
            System.err.println("Audio playback error: " + e.getMessage());
        }
    }

    public static synchronized void stopAudio() {
        try {
            if (activeClip != null) {
                if (activeClip.isRunning()) {
                    activeClip.stop();
                }
                activeClip.close();
                activeClip = null;
            }

            if (activePlayer != null) {
                activePlayer.close(); // unblocks player.play() in the playback thread
                activePlayer = null;
            }

            if (activeProcess != null) {
                if (activeProcess.isAlive()) {
                    activeProcess.destroyForcibly();
                }
                activeProcess = null;
            }

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