package ui;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import api.BaseTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

@Execution(ExecutionMode.SAME_THREAD)

public class BaseUiTest extends BaseTest {
    @BeforeAll
    public static void setupSelenoid() {
        Configuration.remote = api.configs.Config.getProperty("uiRemote");
        Configuration.baseUrl = api.configs.Config.getProperty("uiBaseUrl");
        Configuration.browser = api.configs.Config.getProperty("browser");
        Configuration.baseUrl = api.configs.Config.getProperty("uiBaseUrl");
        Configuration.browserSize = api.configs.Config.getProperty("browserSize");

        // VNC Chrome images expect headed Chrome; headless often breaks session startup
        Configuration.headless = false;
        Configuration.remoteConnectionTimeout = 120_000;
        Configuration.remoteReadTimeout = 120_000;
    }

    @AfterEach
    public void closeBrowser() {
        Selenide.closeWebDriver();
    }
}
