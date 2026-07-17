package utils;

import drivers.DeviceManager;
import io.appium.java_client.android.AndroidDriver;
import io.qameta.allure.Attachment;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

public class TestListener implements ITestListener {

    @Override
    public void onStart(ITestContext context) {
        System.out.println("🚀 [SUITE START] Context initialized for: " + context.getName());
    }

    @Override
    public void onTestStart(ITestResult result) {
        String testDescription = result.getMethod().getDescription();
        String testName = (testDescription != null && !testDescription.isEmpty())
                ? testDescription
                : result.getMethod().getMethodName();

        System.out.println("⏳ [TEST START] Launching: " + testName);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        System.out.println("✅ [TEST PASSED] " + result.getName());
    }

    @Override
    public void onTestFailure(ITestResult result) {
        System.out.println("❌ [TEST FAILED] " + result.getName() + " -> Capturing failure screenshots...");

        captureDeviceScreenshotSafely("Device A", DeviceManager.getDriverA());
        captureDeviceScreenshotSafely("Device B", DeviceManager.getDriverB());
    }

    private void captureDeviceScreenshotSafely(String deviceLabel, AndroidDriver driver) {
        if (driver != null) {
            try {
                captureScreenshotToAllure(deviceLabel + " - Failure Screenshot", driver);
            } catch (Exception e) {
                System.err.println("⚠️ Failed to capture screenshot for " + deviceLabel + ": " + e.getMessage());
            }
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        System.out.println("⚠️ [TEST SKIPPED] " + result.getName());
    }

    @Override
    public void onFinish(ITestContext context) {
        System.out.println("💾 [SUITE FINISH] Allure test processing completed for: " + context.getName());
    }

    @Attachment(value = "{attachmentName}", type = "image/png")
    public byte[] captureScreenshotToAllure(String attachmentName, AndroidDriver driver) {
        return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
    }
}