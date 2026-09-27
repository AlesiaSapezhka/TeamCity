package ui.steps;

import api.configs.Config;
import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import ui.pages.CreateAdminPage;
import ui.pages.InstallationPage;
import common.data.TeamCityAdminData;

import java.time.Duration;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;
import static com.codeborne.selenide.Selenide.refresh;
import static com.codeborne.selenide.Selenide.sleep;

public final class TeamCityInstallationSteps {

    private static final String DEFAULT_TEAMCITY_URL =
            "http://localhost:8111";

    private static final Duration STARTUP_TIMEOUT =
            Duration.ofMinutes(5);

    private static final Duration POLL_INTERVAL =
            Duration.ofSeconds(3);

    private TeamCityInstallationSteps() {
    }

    /**
     * Resolved from the browser point of view (Selenoid container).
     */
    private static String teamCityUrl() {

        String url = Config.getProperty("uiBaseUrl");

        return url == null || url.isBlank()
                ? DEFAULT_TEAMCITY_URL
                : url.trim();
    }

    public static void install() {

        open(teamCityUrl());

        if (!waitForInstallationWizard()) {

            System.out.println(
                    "[INSTALLATION] Server is already set up, wizard skipped"
            );

            return;
        }

        InstallationPage installationPage =
                new InstallationPage();

        installationPage.proceedFromFirstStart();

        installationPage.proceedFromDatabaseSetup();

        installationPage.acceptLicenseAgreement();

        CreateAdminPage createAdminPage =
                new CreateAdminPage();

        createAdminPage
                .shouldBeOpened()
                .createAccount(
                        TeamCityAdminData.USERNAME,
                        TeamCityAdminData.PASSWORD
                );
    }

    /**
     * Polls on the test thread: Selenide binds the WebDriver to it.
     *
     * @return false when the server is already set up and shows the login page
     */
    private static boolean waitForInstallationWizard() {

        long deadline = System.currentTimeMillis()
                + STARTUP_TIMEOUT.toMillis();

        while (System.currentTimeMillis() < deadline) {

            if ($("#proceedButton").is(Condition.visible)) {
                return true;
            }

            if ($("#loginPage").exists()
                    || $("#username").is(Condition.visible)) {

                return false;
            }

            // "TeamCity is starting" screen hides the wizard behind this link
            SelenideElement maintenanceLink =
                    $("#admin-welcome-link a");

            if (maintenanceLink.is(Condition.visible)) {
                maintenanceLink.click();
            } else {
                refresh();
            }

            sleep(POLL_INTERVAL.toMillis());
        }

        throw new IllegalStateException(
                "TeamCity installation wizard did not appear within "
                        + STARTUP_TIMEOUT
        );
    }
}
