package common;

import common.extensions.CreateAndDeleteUserExtension;
import common.extensions.CreateUserAndLogInExtension;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Resolves {@link UserContext} from user extensions' ExtensionContext.Store
 * (thread-safe across ForkJoinPool workers).
 */
public final class UserContexts {

    public static final String STORE_KEY = "userContext";

    private UserContexts() {
    }

    public static UserContext require(ExtensionContext context) {
        UserContext user = context.getStore(CreateAndDeleteUserExtension.NAMESPACE)
                .get(STORE_KEY, UserContext.class);
        if (user == null) {
            user = context.getStore(CreateUserAndLogInExtension.NAMESPACE)
                    .get(STORE_KEY, UserContext.class);
        }
        if (user == null) {
            throw new IllegalStateException(
                    "UserContext is missing. Annotate the test with @CreateAndDeleteUser "
                            + "or @CreateUserAndLogIn (must run before project/agent extensions)."
            );
        }
        return user;
    }
}
