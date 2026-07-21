package drivers;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

public class NetworkHelper {
    private static final int ADB_COMMAND_TIMEOUT_SECONDS = 5;

    public void ensureWifiEnabled(String udid) {
        if (udid == null || udid.isBlank()) return;
        if (!isWifiEnabled(udid)) {
            System.out.println("WiFi left disabled on device {} after test. Re-enabling."+ udid);
            executeCommand("adb", "-s", udid, "shell", "svc", "wifi", "enable");
        }
    }
    public void executeCommand(String... command) {
        try {
            Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
            boolean finished = process.waitFor(ADB_COMMAND_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                System.out.println("Command timed out and was killed: {}"+ String.join(" "+ command));
            } else if (process.exitValue() != 0) {
                System.out.println("Command exited with {}: {}"+process.exitValue()+ String.join(" "+ command));
            }
        } catch (Exception e) {
            System.out.println("Non-fatal error executing command [{}]: {}"+ String.join(" "+ command)+ e.getMessage());
        }
    }
    private boolean isWifiEnabled(String udid) {
        try {
            Process process = new ProcessBuilder("adb", "-s", udid, "shell", "settings", "get", "global", "wifi_on")
                    .redirectErrorStream(true)
                    .start();
            boolean finished = process.waitFor(ADB_COMMAND_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                System.out.println("Timed out checking WiFi state on device {}"+ udid);
                return true;
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String output = reader.readLine();
                return "1".equals(output == null ? "" : output.trim());
            }
        } catch (Exception e) {
            System.out.println("Could not check WiFi state on device {}: {}"+ udid+ e.getMessage());
            return true;
        }
    }
    public void cleanDevicePorts(String udid) {
        if (udid == null || udid.isBlank()) return;
        System.out.println("Clearing ADB forwards & killing orphaned servers on: {}"+ udid);
        executeCommand("adb", "-s", udid, "forward", "--remove-all");
        executeCommand("adb", "-s", udid, "shell", "am", "force-stop", "io.appium.uiautomator2.server");
        executeCommand("adb", "-s", udid, "shell", "am", "force-stop", "io.appium.uiautomator2.server.test");
    }
}
