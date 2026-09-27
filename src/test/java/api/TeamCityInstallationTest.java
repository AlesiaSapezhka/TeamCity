package api;

import api.models.user.TokenResponse;
import api.specs.RequestSpecs;
import api.steps.AgentSteps;
import api.steps.AuthSteps;
import api.steps.UserSteps;
import common.data.TeamCityAdminData;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import ui.BaseUiTest;
import ui.steps.AgentUiSteps;
import ui.steps.TeamCityInstallationSteps;


/**
 * Environment precondition: runs once against a freshly started TeamCity stack
 */
@Tag("precondition")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TeamCityInstallationTest extends BaseUiTest {

    @Test
    @Order(1)
    void setUpTeamCity() {
        TeamCityInstallationSteps.install();
    }

    @Test
    @Order(2)
    void setUpAgent() {
        AuthSteps.ensurePerProjectPermissions();

        UserSteps.ensureUserExists(
                TeamCityAdminData.USERNAME,
                TeamCityAdminData.PASSWORD
        );

        UserSteps.grantSystemAdmin(
                TeamCityAdminData.USERNAME
        );

        TokenResponse token =
                UserSteps.createToken(
                        TeamCityAdminData.USERNAME,
                        TeamCityAdminData.PASSWORD
                );

        if (token.getValue() == null
                || token.getValue().isBlank()) {

            throw new IllegalStateException(
                    "Admin token is empty"
            );
        }

        RequestSpecs.setUserToken(
                token.getValue()
        );

        AgentUiSteps.authorizeAgent();
        AgentSteps.ensureAgentReady();

        AgentSteps.assertAgentReady(
                AgentSteps.findAgent()
        );
    }
}
