package pages;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import pages.locators.ElementKey;
import pages.locators.ElementRegistry;

import java.util.List;

public class RaseHandPage extends BasePage{

    public RaseHandPage(AppiumDriver driver) {
        super(driver);
    }

    public int countRaisedHandsInSection() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) {}

        String xpath = "//*[count(preceding-sibling::android.widget.RelativeLayout) = 1 " +
                "and (.//*[contains(translate(@content-desc, 'HAND', 'hand'), 'hand is raised')] " +
                "or contains(translate(@content-desc, 'HAND', 'hand'), 'hand is raised'))]";

        List<WebElement> elements = driver.findElements(AppiumBy.xpath(xpath));
        return elements.size();
    }
    public void raisHand(){
        waitClickable(ElementRegistry.get(ElementKey.MORE_OPTION)).click();
        waitClickable(ElementRegistry.get(ElementKey.RAISE_HAND)).click();
    }
    public boolean isTheCounterOfRaisHAndAccurate(){
        waitClickable(ElementRegistry.get(ElementKey.MANAGE_RASE_HAND)).click();
        waitClickable(ElementRegistry.get(ElementKey.SHOW_RAISED_HANDS)).click();


        int count=countRaisedHandsInSection();
        System.out.println("The count Of user is :"+count);
        try {
            String actualText = driver.findElement(ElementRegistry.getRaisedHandCountLocator(count)).getText();
            System.out.println("the Real Count Appear text : "+actualText);
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
