package utils;

import io.appium.java_client.AppiumDriver;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.openqa.selenium.OutputType;
import org.apache.commons.io.FileUtils;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ToastOcrHandler {

    public static String captureAndReadToast(AppiumDriver driver) {
        Tesseract tesseract = new Tesseract();

        String projectRoot = System.getProperty("user.dir");
        String tessdataPath = projectRoot + File.separator + "src"
                + File.separator + "test"
                + File.separator + "resources"
                + File.separator + "tessdata";

        tesseract.setDatapath(tessdataPath);
        tesseract.setLanguage("eng");

        File screenshot = ( driver).getScreenshotAs(OutputType.FILE);
        File destinationFile = new File(projectRoot + File.separator + "target" + File.separator + "toast_screenshot.png");

        try {
            FileUtils.copyFile(screenshot, destinationFile);

            BufferedImage fullImg = ImageIO.read(destinationFile);
            int cropY = (int) (fullImg.getHeight() * 0.75);
            int cropHeight = fullImg.getHeight() - cropY;

            BufferedImage toastRegion = fullImg.getSubimage(0, cropY, fullImg.getWidth(), cropHeight);

            File croppedFile = new File(projectRoot + File.separator + "target" + File.separator + "toast_cropped.png");
            ImageIO.write(toastRegion, "png", croppedFile);

            String extractedText = tesseract.doOCR(croppedFile);

            System.out.println("🔍 [OCR Extracted Text]: " + extractedText.trim());

            return extractedText;

        } catch (IOException | TesseractException e) {
            System.err.println("⚠️ OCR Processing Error: " + e.getMessage());
            return "";
        }
    }
    public static boolean waitForToastContaining(AppiumDriver driver, String expectedText, int timeoutSeconds, int pollIntervalMillis) {
        long deadline = System.currentTimeMillis() + (timeoutSeconds * 1000L);
        String lastSeenText = "";
        String expectedLower = expectedText.toLowerCase();

        while (System.currentTimeMillis() < deadline) {
            String text = ToastOcrHandler.captureAndReadToast(driver);
            lastSeenText = (text == null) ? "" :
                    text.toLowerCase()
                            .replace("\r", " ")
                            .replace("\n", " ")
                            .replaceAll("\\s+", " ")
                            .trim();

            if (lastSeenText.contains(expectedLower)) {
                return true;
            }

            try {
                Thread.sleep(pollIntervalMillis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        System.err.println("Toast not matched. Expected: '" + expectedText + "', Last OCR read: '" + lastSeenText + "'");
        return false;
    }
    public static String captureAndReadMissedCallBanner(AppiumDriver driver) {
        Tesseract tesseract = new Tesseract();

        String projectRoot = System.getProperty("user.dir");
        String tessdataPath = projectRoot + File.separator + "src"
                + File.separator + "test"
                + File.separator + "resources"
                + File.separator + "tessdata";

        tesseract.setDatapath(tessdataPath);
        tesseract.setLanguage("eng");

        File screenshot = ( driver).getScreenshotAs(OutputType.FILE);
        File destinationFile = new File(projectRoot + File.separator + "target" + File.separator + "missed_call_screenshot.png");

        try {
            FileUtils.copyFile(screenshot, destinationFile);

            BufferedImage fullImg = ImageIO.read(destinationFile);

            int cropY = (int) (fullImg.getHeight() * 0.05);
            int cropHeight = (int) (fullImg.getHeight() * 0.13); // top 5%-18% band

            BufferedImage bannerRegion = fullImg.getSubimage(0, cropY, fullImg.getWidth(), cropHeight);

            File croppedFile = new File(projectRoot + File.separator + "target" + File.separator + "missed_call_cropped.png");
            ImageIO.write(bannerRegion, "png", croppedFile);

            String extractedText = tesseract.doOCR(croppedFile);

            System.out.println("🔍 [OCR Extracted Text - Missed Call Banner]: " + extractedText.trim());

            return extractedText;

        } catch (IOException | TesseractException e) {
            System.err.println("⚠️ OCR Processing Error (Missed Call Banner): " + e.getMessage());
            return "";
        }
    }
    public static boolean waitForMissedCallBanner(AppiumDriver driver, int timeoutSeconds, int pollIntervalMillis) {
        long deadline = System.currentTimeMillis() + (timeoutSeconds * 1000L);
        String lastSeenText = "";

        while (System.currentTimeMillis() < deadline) {
            String text = ToastOcrHandler.captureAndReadMissedCallBanner(driver);
            lastSeenText = text;

            if (text.toLowerCase().contains("missed call")) {
                return true;
            }

            try {
                Thread.sleep(pollIntervalMillis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        System.err.println("Missed call banner not found. Last OCR read: " + lastSeenText);
        return false;
    }
}