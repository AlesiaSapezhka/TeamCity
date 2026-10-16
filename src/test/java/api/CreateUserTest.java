package api;

import api.models.comparison.ModelAssertions;
import api.models.user.CreateUserRequest;
import api.models.user.UserResponse;
import api.steps.UserSteps;
import common.data.JsonPaths;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

public class CreateUserTest extends BaseTest {

    private String createdUsername;

    @AfterEach
    void cleanupCreatedUser() {
        if (createdUsername == null || createdUsername.isBlank()) {
            return;
        }
        try {
            UserSteps.deleteUser(createdUsername);
        } catch (AssertionError | RuntimeException ignored) {
            // user may not exist (negative tests)
        }
        createdUsername = null;
    }

    @Test
    void userCanCreateUserWithValidData() {
        CreateUserRequest createUserRequest = UserSteps.buildUserValid();
        UserResponse createUserResponse = UserSteps.createUserValid(createUserRequest);
        createdUsername = createUserResponse.getUsername();

        ModelAssertions.assertThatModels(createUserRequest, createUserResponse).match();
        softly.assertThat(createUserResponse.getId()).isNotBlank();

        UserResponse user = UserSteps.getUser(createUserRequest);
        ModelAssertions.assertThatModels(createUserRequest, user).match();
    }

    @Test
    void userCanNotCreateUserWithBlankName() {
        CreateUserRequest createUserRequest = UserSteps.buildUserBlankName();
        UserSteps.createUserInvalidName(createUserRequest);

        List<UserResponse> users = UserSteps.getAllUsers(JsonPaths.USERS.getPath());
        softly.assertThat(users).noneSatisfy(foundUser ->
                ModelAssertions.assertThatModels(createUserRequest, foundUser).match());
    }

    // Production defect: user is created with a blank password (expected: rejected).
    // Tagged as bug for Dashboard metrics; CI runs this group separately (does not fail pipeline).
    @Test
    @Tag("bug")
    void userCanNotCreateUserWithBlankPassword() {
        CreateUserRequest createUserRequest = UserSteps.buildUserBlankPassword();
        createdUsername = createUserRequest.getUsername();
        UserSteps.createUserInvalidPassword(createUserRequest);

        List<UserResponse> users = UserSteps.getAllUsers(JsonPaths.USERS.getPath());
        softly.assertThat(users).noneSatisfy(foundUser ->
                ModelAssertions.assertThatModels(createUserRequest, foundUser).match());
    }
}
