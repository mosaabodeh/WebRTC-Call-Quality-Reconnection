package pages;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;
import pages.locators.ElementKey;
import pages.locators.ElementRegistry;
import utils.ConfigReader;

import java.time.Duration;
import java.util.List;

public class RaseHandPage extends BasePage {

    public RaseHandPage(AppiumDriver driver) {
        super(driver);
    }

    private static final String RAISED_HAND_XPATH =
            "//*[count(preceding-sibling::android.widget.RelativeLayout) = 1 " +
                    "and (.//*[contains(translate(@content-desc, 'HAND', 'hand'), 'hand is raised')] " +
                    "or contains(translate(@content-desc, 'HAND', 'hand'), 'hand is raised'))]";

    public int countRaisedHandsInSection() {
        long timeoutSec = Long.parseLong(ConfigReader.getProperty("raisedHand.stabilize.timeoutSeconds", "8"));
        long pollMs = Long.parseLong(ConfigReader.getProperty("raisedHand.stabilize.pollMillis", "500"));

        Wait<AppiumDriver> stabilizeWait = new FluentWait<>(driver)
                .withTimeout(Duration.ofSeconds(timeoutSec))
                .pollingEvery(Duration.ofMillis(pollMs))
                .ignoring(Exception.class);

        final int[] lastCount = {-1};

        try {
            return stabilizeWait.until(d -> {
                List<WebElement> elements = d.findElements(AppiumBy.xpath(RAISED_HAND_XPATH));
                int currentCount = elements.size();

                if (currentCount == lastCount[0]) {
                    return currentCount;
                }
                lastCount[0] = currentCount;
                return null;
            });
        } catch (Exception e) {
            return Math.max(lastCount[0], 0);
        }
    }

    public void raisHand() {
        waitClickable(ElementRegistry.get(ElementKey.MORE_OPTION)).click();
        waitClickable(ElementRegistry.get(ElementKey.RAISE_HAND)).click();
    }

    public boolean isTheCounterOfRaisHAndAccurate() {
        waitClickable(ElementRegistry.get(ElementKey.MANAGE_RASE_HAND)).click();
        waitClickable(ElementRegistry.get(ElementKey.SHOW_RAISED_HANDS)).click();

        int count = countRaisedHandsInSection();
        System.out.println("The count Of user is :" + count);
        try {
            String actualText = driver.findElement(ElementRegistry.getRaisedHandCountLocator(count)).getText();
            System.out.println("the Real Count Appear text : " + actualText);
            return actualText.equals(String.valueOf(count));
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isAllHandLowered() {
        waitClickable(ElementRegistry.get(ElementKey.MANAGE_RASE_HAND)).click();
        waitClickable(ElementRegistry.get(ElementKey.LOWER_ALL_HANDS)).click();
        waitClickable(ElementRegistry.get(ElementKey.START_OK_APPLY)).click();
        return wait.until(ExpectedConditions.invisibilityOfElementLocated(ElementRegistry.get(ElementKey.SHOW_RAISED_HANDS)));
    }
}