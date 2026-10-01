package common.extensions;

import api.models.user.CreateUserRequest;
import api.models.user.TokenResponse;
import api.models.user.UserResponse;
import api.steps.AuthSteps;
import api.steps.UserSteps;
import common.UserContext;
import common.UserContexts;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.extension.*;
import ui.pages.LoginPage;

/**
 * Creates per-test user + PAT in store and logs in via UI.
 */
@Order(1)
public class CreateUserAndLogInExtension
        implements BeforeEachCallback, AfterEachCallback, ParameterResolver {

    public static final ExtensionContext.Namespace NAMESPACE =
            ExtensionContext.Namespace.create(CreateUserAndLogInExtension.class);

    @Override
    public void beforeEach(ExtensionContext context) {
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

        UserContext userContext = new UserContext(
                user.getId(),
                user.getUsername(),
                request.getPassword(),
                token.getValue()
        );
        context.getStore(NAMESPACE).put(UserContexts.STORE_KEY, userContext);

        new LoginPage()
                .open()
                .login(user.getUsername(), request.getPassword());
    }

    @Override
    public void afterEach(ExtensionContext context) {
        UserContext userContext = context.getStore(NAMESPACE)
                .remove(UserContexts.STORE_KEY, UserContext.class);
        if (userContext != null) {
            UserSteps.deleteUser(userContext.username());
        }
    }

    @Override
    public boolean supportsParameter(
            ParameterContext parameterContext,
            ExtensionContext extensionContext) {
        return parameterContext.getParameter().getType().equals(UserContext.class);
    }

    @Override
    public Object resolveParameter(
            ParameterContext parameterContext,
            ExtensionContext extensionContext) {
        return UserContexts.require(extensionContext);
    }
}
