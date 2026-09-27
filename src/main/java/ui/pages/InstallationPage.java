package ui.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;

import java.time.Duration;

import static com.codeborne.selenide.Selenide.$;

public class InstallationPage extends BasePage<InstallationPage> {

    // Each wizard step triggers server-side work that outlasts the default timeout
    private static final Duration STEP_TIMEOUT = Duration.ofMinutes(3);

    private final SelenideElement proceedButton =
            $("#proceedButton");

    private final SelenideElement acceptLicenseAgreement =
            $("#acceptLicenseAgreement");

    public void proceedFromFirstStart() {
        proceedButton
                .shouldBe(Condition.visible, STEP_TIMEOUT)
                .click();
    }

    public void proceedFromDatabaseSetup() {
        proceedButton
                .shouldBe(Condition.visible, STEP_TIMEOUT)
                .click();
    }

    public void acceptLicenseAgreement() {
        acceptLicenseAgreement
                .shouldBe(Condition.visible, STEP_TIMEOUT)
                .scrollTo()
                .click();
    }

    @Override
    public String url() {
        return "";
    }
}
