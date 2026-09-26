package api;

import api.models.user.CreateUserRequest;
import api.models.user.TokenResponse;
import api.models.user.UserResponse;
import api.specs.RequestSpecs;
import api.steps.AgentSteps;
import api.steps.AuthSteps;
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
//        given()
//                .baseUri("http://localhost:8111")
//                .when()
//                .get("/healthCheck/ready")
//                .then()
//                .statusCode(200);

        AuthSteps.ensurePerProjectPermissions();

        CreateUserRequest request = UserSteps.buildUserValid();
        UserResponse user = UserSteps.createUserValid(request);
        UserSteps.grantSystemAdmin(user.getUsername());

        TokenResponse token = UserSteps.createToken(request.getUsername(), request.getPassword());
        if (token.getValue() == null || token.getValue().isBlank()) {
            throw new IllegalStateException(
                    "TokenResponse.value is empty after createToken for user " + user.getUsername()
            );
        }
        RequestSpecs.setUserToken(token.getValue());

        AgentSteps.ensureAgentReady();
        UserSteps.deleteUser(user.getUsername());
    }
}
