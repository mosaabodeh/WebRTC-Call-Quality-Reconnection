package drivers;

import io.appium.java_client.android.AndroidDriver;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DeviceManager {
    private static final Map<String, AndroidDriver> drivers = new ConcurrentHashMap<>();

    public static AndroidDriver getDriverA() {
        return drivers.get("A");
    }

    public static void setDriverA(AndroidDriver driver) {
        if (driver != null) drivers.put("A", driver);
    }

    public static AndroidDriver getDriverB() {
        return drivers.get("B");
    }

    public static void setDriverB(AndroidDriver driver) {
        if (driver != null) drivers.put("B", driver);
    }

    public static void clear() {
        drivers.clear();
    }

    public static void unload() {
        for (Map.Entry<String, AndroidDriver> entry : drivers.entrySet()) {
            AndroidDriver driver = entry.getValue();
            if (driver != null) {
                try {
                    System.out.println("🔄 [DeviceManager] Quitting active session for Device " + entry.getKey() + " during cleanup...");
                    driver.quit();
                } catch (Exception e) {
                    System.err.println("⚠️ [DeviceManager] Failed to quit driver safely for Device " + entry.getKey() + ": " + e.getMessage());
                }
            }
        }
        clear();
    }
}