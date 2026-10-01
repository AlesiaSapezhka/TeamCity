package common;

/**
 * Holds the authenticated test user including PAT.
 * Token must travel with the context — ThreadLocal breaks under JUnit ForkJoinPool.
 */
public record UserContext(
        String userId,
        String username,
        String password,
        String token
) {
}
