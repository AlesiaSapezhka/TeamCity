package api.steps;

import api.models.server.AuthSettingsRequest;
import api.requesters.CrudRequester;
import api.requesters.interfaces.Endpoints;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import common.UserContext;

public final class AuthSteps {
    private static final String INVALID_TOKEN =
            "Invalid authentication request or authentication scheme is not supported\n"
                    + "To login manually go to \"/login.html\" page";
    private static final String WITHOUT_AUTHENTIFICATION =
            "Authentication required\n"
                    + "To login manually go to \"/login.html\" page";

    private static volatile boolean perProjectPermissionsEnabled;

    private AuthSteps() {
    }

    /**
     * Roles require per-project permissions; enable once per JVM if needed.
     */
    public static synchronized void ensurePerProjectPermissions() {
        if (perProjectPermissionsEnabled) {
            return;
        }
        new CrudRequester(
                RequestSpecs.superUserSpec(),
                Endpoints.AUTH_SETTINGS,
                ResponseSpecs.requestReturnsOK()
        ).update(AuthSettingsRequest.withPerProjectPermissions());
        perProjectPermissionsEnabled = true;
    }

    public static void authAsUser(UserContext user) {
        new CrudRequester(
                RequestSpecs.userSpec(user),
                Endpoints.PROJECTS,
                ResponseSpecs.requestReturnsOK()
        ).get();
    }

    public static void authWithInvalidToken() {
        new CrudRequester(
                RequestSpecs.bearerSpec("invalid-token"),
                Endpoints.PROJECTS,
                ResponseSpecs.requestReturnsForbiddenRequestWithoutKey(INVALID_TOKEN)
        ).get();
    }

    public static void authWithoutToken() {
        new CrudRequester(
                RequestSpecs.baseSpec(),
                Endpoints.PROJECTS,
                ResponseSpecs.requestReturnsForbiddenRequestWithoutKey(WITHOUT_AUTHENTIFICATION)
        ).get();
    }
}
