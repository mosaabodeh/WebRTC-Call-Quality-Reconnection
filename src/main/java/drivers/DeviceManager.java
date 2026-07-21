package drivers;

import io.appium.java_client.android.AndroidDriver;

public class DeviceManager {

    private static final ThreadLocal<AndroidDriver> driverA = new ThreadLocal<>();
    private static final ThreadLocal<AndroidDriver> driverB = new ThreadLocal<>();

    public static AndroidDriver getDriverA() {
        return driverA.get();
    }

    public static void setDriverA(AndroidDriver driver) {
        driverA.set(driver);
    }

    public static AndroidDriver getDriverB() {
        return driverB.get();
    }

    public static void setDriverB(AndroidDriver driver) {
        driverB.set(driver);
    }

    public static void clear() {
        driverA.remove();
        driverB.remove();
    }

    public static void unload() {
        quitSafely(driverA);
        quitSafely(driverB);
    }

    private static void quitSafely(ThreadLocal<AndroidDriver> driverThreadLocal) {
        AndroidDriver driver = driverThreadLocal.get();
        try {
            if (driver != null) {
                System.out.println("Quitting active session for thread: " + Thread.currentThread().getName());
                driver.quit();
            }
        } catch (Exception e) {
            System.err.println("[DeviceManager] Failed to quit driver safely: " + e.getMessage());
        } finally {
            driverThreadLocal.remove();
        }
    }
}