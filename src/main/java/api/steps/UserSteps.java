package api.steps;

import api.generators.RandomData;
import api.generators.RandomModelGenerator;
import api.models.user.*;
import api.requesters.CrudRequester;
import api.requesters.ValidatedCrudRequester;
import api.requesters.interfaces.Endpoints;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import common.UserContext;
import io.restassured.response.ValidatableResponse;

import java.util.List;
import java.util.Map;

/**
 * Create/delete users — via Super User ({@link RequestSpecs#superUserSpec()}).
 * Tokens — via the user themselves (Basic username:password).
 * Current user — via Bearer from {@link UserContext#token()}.
 */
public final class UserSteps {
    private static final String BAD_REQUEST_STATUS_TEXT = "Responding with error, status code: 400 (Bad Request).";
    private static final String BLANK_NAME_MESSAGE = "Username must not be empty when creating user.";
    private static final String BLANK_PASSWORD_MESSAGE = "Password must not be empty when creating user.";

    private UserSteps() {
    }

    public static CreateUserRequest buildUserValid() {
        return RandomModelGenerator.generate(CreateUserRequest.class);
    }

    /**
     * Idempotent: the wizard may or may not have created the account already.
     */
    public static void ensureUserExists(String username, String password) {
        int status = new CrudRequester(
                RequestSpecs.superUserSpec(),
                Endpoints.USER,
                ResponseSpecs.anyStatus()
        ).get(Map.of("userLocator", "username:" + username))
                .extract()
                .statusCode();

        if (status == 200) {
            return;
        }

        createUserValid(CreateUserRequest.builder()
                .username(username)
                .password(password)
                .build());
    }

    public static UserResponse createUserValid(CreateUserRequest request) {
        return new ValidatedCrudRequester<UserResponse>(
                RequestSpecs.superUserSpec(),
                Endpoints.USERS,
                ResponseSpecs.requestReturnsOK()
        ).post(request);
    }

    public static CreateUserRequest buildUserBlankName() {
        return CreateUserRequest.builder()
                .username("")
                .password(RandomData.getPassword())
                .build();
    }

    public static CreateUserRequest buildUserBlankPassword() {
        return CreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password("")
                .build();
    }

    public static void createUserInvalidName(CreateUserRequest request) {
        new CrudRequester(
                RequestSpecs.superUserSpec(),
                Endpoints.USERS,
                ResponseSpecs.requestReturnsBadRequest(BAD_REQUEST_STATUS_TEXT, BLANK_NAME_MESSAGE)
        ).post(request);
    }

    public static void createUserInvalidPassword(CreateUserRequest request) {
        new CrudRequester(
                RequestSpecs.superUserSpec(),
                Endpoints.USERS,
                ResponseSpecs.requestReturnsBadRequest(BAD_REQUEST_STATUS_TEXT, BLANK_PASSWORD_MESSAGE)
        ).post(request);
    }

    /**
     * Grant global SYSTEM_ADMIN (requires per-project permissions enabled).
     */
    public static void grantSystemAdmin(String username) {
        new ValidatedCrudRequester<Role>(
                RequestSpecs.superUserSpec(),
                Endpoints.USER_ROLE,
                ResponseSpecs.requestReturnsOK()
        ).update(null, Map.of(
                "userLocator", "username:" + username,
                "roleId", "SYSTEM_ADMIN",
                "scope", "g"
        ));
    }

    /**
     * PAT must be created by the user (Super User gets 403 for other users' tokens).
     */
    public static TokenResponse createToken(String username, String password) {
        CreateTokenRequest request = CreateTokenRequest.builder()
                .name("automation-token")
                .build();

        return new ValidatedCrudRequester<TokenResponse>(
                RequestSpecs.authAsUserSpec(username, password),
                Endpoints.USER_TOKENS,
                ResponseSpecs.requestReturnsOK()
        ).post(request, Map.of("userLocator", "username:" + username));
    }

    public static UserResponse getUser(CreateUserRequest request) {
        return new ValidatedCrudRequester<UserResponse>(
                RequestSpecs.superUserSpec(),
                Endpoints.USER,
                ResponseSpecs.requestReturnsOK()
        ).get(Map.of("userLocator", "username:" + request.getUsername()));
    }

    public static UserResponse getCurrentUser(UserContext user) {
        return new ValidatedCrudRequester<UserResponse>(
                RequestSpecs.userSpec(user),
                Endpoints.CURRENT_USER,
                ResponseSpecs.requestReturnsOK()
        ).get();
    }

    public static List<UserResponse> getAllUsers(String jsonPath) {
        return new ValidatedCrudRequester<UserResponse>(
                RequestSpecs.superUserSpec(),
                Endpoints.USERS,
                ResponseSpecs.requestReturnsOK()
        ).getList(jsonPath);
    }

    public static ValidatableResponse deleteUser(String username) {
        return new CrudRequester(
                RequestSpecs.superUserSpec(),
                Endpoints.USER,
                ResponseSpecs.entityWasDeleted()
        ).delete(Map.of("userLocator", "username:" + username));
    }
}
