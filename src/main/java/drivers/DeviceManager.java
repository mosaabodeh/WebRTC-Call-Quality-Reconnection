package drivers;

import io.appium.java_client.android.AndroidDriver;

public class DeviceManager {

    private static  AndroidDriver driverA;
    private static  AndroidDriver driverB;

    public static synchronized void setDriverA(AndroidDriver driver) {
        driverA = driver;
    }

    public static synchronized void setDriverB(AndroidDriver driver) {
        driverB = driver;
    }

    public static synchronized AndroidDriver getDriverA() {
        return driverA;
    }

    public static synchronized AndroidDriver getDriverB() {
        return driverB;
    }

    public static synchronized void clear() {
        driverA = null;
        driverB = null;
    }

    public static synchronized void unload() {
        if (driverA != null) {
            try {
                driverA.quit();
            } catch (Exception ignored) {}
        }
        if (driverB != null) {
            try {
                driverB.quit();
            } catch (Exception ignored) {}
        }
        clear();
    }
}