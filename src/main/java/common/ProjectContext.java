package common;

public record ProjectContext(
        String projectId,
        String projectName,
        UserContext user
) {
    public String token() {
        return user.token();
    }
}
