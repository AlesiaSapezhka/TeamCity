package ui.steps;

import common.data.TeamCityAdminData;
import ui.pages.AgentPage;
import ui.pages.LoginPage;

public final class AgentUiSteps {

    private static final String AUTHORIZATION_COMMENT =
            "Authorized by automated tests";

    private AgentUiSteps() {
    }

    public static void authorizeAgent() {

        new LoginPage()
                .open()
                .login(
                        TeamCityAdminData.USERNAME,
                        TeamCityAdminData.PASSWORD
                );

        AgentPage agentPage = new AgentPage().open();

        if (!agentPage.isAuthorizationRequired()) {
            return;
        }

        agentPage.authorize(AUTHORIZATION_COMMENT);
    }
}
