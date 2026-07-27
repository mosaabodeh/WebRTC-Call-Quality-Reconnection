package utils;

import io.appium.java_client.AppiumDriver;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
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

        File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
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
}