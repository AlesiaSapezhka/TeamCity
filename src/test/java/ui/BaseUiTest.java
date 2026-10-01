package ui;

import api.BaseTest;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.logevents.SelenideLogger;
import io.qameta.allure.selenide.AllureSelenide;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

@Execution(ExecutionMode.SAME_THREAD)

public class BaseUiTest extends BaseTest {
    @BeforeAll
    public static void setupSelenoid() {
        // Provider owns the remote session, so Configuration.remote must stay unset
        Configuration.browser = SelenoidWebDriverProvider.class.getName();
        Configuration.baseUrl = api.configs.Config.getProperty("uiBaseUrl");
        Configuration.browserSize = api.configs.Config.getProperty("browserSize");

        // VNC Chrome images expect headed Chrome; headless often breaks session startup
        Configuration.headless = false;
        Configuration.remoteConnectionTimeout = 120_000;
        Configuration.remoteReadTimeout = 120_000;

        // Attach failure screenshots / page source into Allure (not only local file:// paths)
        SelenideLogger.addListener("AllureSelenide",
                new AllureSelenide()
                        .screenshots(true)
                        .savePageSource(true));
    }

    @AfterEach
    public void closeBrowser() {
        Selenide.closeWebDriver();
    }
}
