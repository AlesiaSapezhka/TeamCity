package common;

public record UserContext(
        String userId,
        String username,
        String password,
        String token
) {
}
