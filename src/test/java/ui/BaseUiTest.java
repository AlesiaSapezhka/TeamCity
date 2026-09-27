package ui;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import api.BaseTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.openqa.selenium.MutableCapabilities;

import java.util.Map;

@Execution(ExecutionMode.SAME_THREAD)

public class BaseUiTest extends BaseTest {
    @BeforeAll
    public static void setupSelenoid() {
        Configuration.remote = api.configs.Config.getProperty("uiRemote");
        Configuration.baseUrl = api.configs.Config.getProperty("uiBaseUrl");
        Configuration.browser = api.configs.Config.getProperty("browser");
        Configuration.browserVersion = api.configs.Config.getProperty("browserVersion");
        Configuration.browserSize = api.configs.Config.getProperty("browserSize");

        // VNC Chrome images expect headed Chrome; headless often breaks session startup
        Configuration.headless = false;
        Configuration.remoteConnectionTimeout = 120_000;
        Configuration.remoteReadTimeout = 120_000;

        MutableCapabilities caps = new MutableCapabilities();
        caps.setCapability("selenoid:options", Map.of("enableVNC", true, "enableLog", true, "sessionTimeout", "5m"));
        Configuration.browserCapabilities = caps;

    }

    @AfterEach
    public void closeBrowser() {
        Selenide.closeWebDriver();
    }
}
