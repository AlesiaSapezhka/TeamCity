package common;

/**
 * Per-test project plus the user that owns the session (PAT for API calls).
 */
public record ProjectContext(
        String projectId,
        String projectName,
        UserContext user
) {
    public String token() {
        return user.token();
    }
}
