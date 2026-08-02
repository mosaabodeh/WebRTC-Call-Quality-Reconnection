package utils;

import javazoom.jl.player.Player;
import javax.sound.sampled.*;
import java.io.File;
import java.io.FileInputStream;
import java.util.concurrent.CompletableFuture;

public class AudioUtils {

    private static volatile Clip activeClip;
    private static volatile Player activePlayer;
    private static volatile Process activeProcess;

    public static class PlaybackHandle {
        public final CompletableFuture<Void> started;
        public final CompletableFuture<Void> finished;

        public PlaybackHandle(CompletableFuture<Void> started, CompletableFuture<Void> finished) {
            this.started = started;
            this.finished = finished;
        }
    }

    public static synchronized PlaybackHandle startAudio(String localAudioFilePath) {
        stopAudio();

        CompletableFuture<Void> started = new CompletableFuture<>();
        CompletableFuture<Void> finished = new CompletableFuture<>();

        new Thread(() -> {
            try {
                File audioFile = new File(localAudioFilePath);

                if (!audioFile.exists()) {
                    System.err.println("Audio file not found at: " + audioFile.getAbsolutePath());
                    started.complete(null);
                    finished.complete(null);
                    return;
                }

                if (localAudioFilePath.endsWith(".wav") || localAudioFilePath.endsWith(".au")) {
                    AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
                    Clip clip = AudioSystem.getClip();
                    activeClip = clip;

                    clip.addLineListener(event -> {
                        if (event.getType() == LineEvent.Type.START) {
                            started.complete(null);
                        }
                        if (event.getType() == LineEvent.Type.STOP) {
                            finished.complete(null);
                        }
                    });

                    clip.open(audioStream);
                    clip.start();

                } else if (localAudioFilePath.endsWith(".mp3")) {

                    try (FileInputStream fis = new FileInputStream(audioFile)) {
                        Player player = new Player(fis);
                        activePlayer = player;


                        started.complete(null);
                        player.play();
                    }
                    activePlayer = null;
                    finished.complete(null);

                } else {

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

                    started.complete(null);
                    process.waitFor();
                    finished.complete(null);
                }
            } catch (Exception e) {
                System.err.println("Failed to start/play audio: " + e.getMessage());
                started.completeExceptionally(e);
                finished.completeExceptionally(e);
            }
        }, "audio-playback-thread").start();

        return new PlaybackHandle(started, finished);
    }


    public static PlaybackHandle startAudioAndAwaitStart(String localAudioFilePath) {
        PlaybackHandle handle = startAudio(localAudioFilePath);
        try {
            handle.started.join();
        } catch (Exception e) {
            System.err.println("Error waiting for playback to start: " + e.getMessage());
        }
        return handle;
    }

    public static void playAndWait(String localAudioFilePath) {
        try {
            startAudio(localAudioFilePath).finished.join();
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
                activePlayer.close();
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


}