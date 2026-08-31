import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SeleniumTest {

    private WebDriver driver;

    @BeforeAll
    static void setupDriverBinary() {
        WebDriverManager.chromedriver().setup();
    }

    @BeforeEach
    void setupBrowser() {
        ChromeOptions options = new ChromeOptions();
        // run headless so the test works in CI (no display)
        options.addArguments("--headless=new", "--no-sandbox", "--disable-dev-shm-usage");
        driver = new ChromeDriver(options);
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    void pageTitle_shouldContainExpectedText() {
        driver.get("https://example.com");
        String title = driver.getTitle();
        assertTrue(title.contains("Example"), "Page title should contain 'Example', but was: " + title);
    }

    @Test
    void pageHeading_shouldBePresent() {
        driver.get("https://example.com");
        WebElement heading = driver.findElement(By.tagName("h1"));
        assertNotNull(heading, "Page should have an <h1> element");
        assertFalse(heading.getText().isBlank(), "Heading text should not be blank");
    }

    @Test
    void pageBody_shouldContainMoreInfoLink() {
        driver.get("https://example.com");
        List<WebElement> links = driver.findElements(By.tagName("a"));
        assertFalse(links.isEmpty(), "Page should contain at least one link");
        assertTrue(links.stream().anyMatch(WebElement::isDisplayed), "At least one link should be visible");
    }
}
