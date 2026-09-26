package api;

import api.models.user.CreateUserRequest;
import api.models.user.UserResponse;
import api.steps.AgentSteps;
import api.steps.UserSteps;
import common.TeamCityInstallationClient;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

@Tag("precondition")
public class TeamCityInstallationTest extends  BaseTest {

    @Test
    void  setUpTeamCity() {
        new TeamCityInstallationClient().install();
        given()
                .baseUri("http://localhost:8111")
                .when()
                .get("/healthCheck/ready")
                .then()
                .statusCode(200);

        CreateUserRequest request = UserSteps.buildUserValid();
        UserResponse user = UserSteps.createUserValid(request);
        UserSteps.grantSystemAdmin(user.getUsername());

        AgentSteps.ensureAgentReady();
        UserSteps.deleteUser(user.getUsername());
    }
}
