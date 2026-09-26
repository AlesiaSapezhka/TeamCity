package common;

import api.configs.SuperUserTokenResolver;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;

public class TeamCityInstallationClient {

    private static final String BASE_URL = "http://localhost:8111";

    private static final String INSTALLATION_PAGE = "/mnt";
    private static final String READY_ENDPOINT = "/healthCheck/ready";

    private static final String SESSION_COOKIE = "TCSESSIONID";
    private static final String CSRF_HEADER = "X-TC-CSRF-Token";

    private String csrfToken;
    private String sessionId;

    public void install() {
        waitForInstallationPage();
        proceedInstallation();
        proceedDatabase();
        acceptLicense();
        waitForTeamCityReady();
        authenticateSuperUser();
    }

    private void authenticateSuperUser() {
        String superUserToken = SuperUserTokenResolver.resolve();

        Response response = given()
                .baseUri(BASE_URL)
                .cookie(SESSION_COOKIE, sessionId)
                .header(CSRF_HEADER, csrfToken)
                .header("X-Requested-With", "XMLHttpRequest")
                .header("Accept", "*/*")
                .header("Origin", BASE_URL)
                .header("Referer", BASE_URL + "/mnt")
                .when()
                .post("/mnt/do/authenticate?token=" + superUserToken);

        System.out.println(
                "Super User authentication -> " + response.statusCode()
        );

        if (response.statusCode() != 200) {
            System.out.println(
                    "Response body:\n" + response.asPrettyString()
            );
        }

        response.then().statusCode(200);
    }

    private void waitForInstallationPage() {
        await()
                .atMost(Duration.ofMinutes(5))
                .pollInterval(Duration.ofSeconds(2))
                .until(() -> {
                    try {
                        Response response = given()
                                .baseUri(BASE_URL)
                                .when()
                                .get(INSTALLATION_PAGE);

                        System.out.println(
                                "TeamCity /mnt status: "
                                        + response.statusCode()
                        );

                        if (response.statusCode() != 200) {
                            return false;
                        }

                        sessionId = response.getCookie(SESSION_COOKIE);

                        if (sessionId == null || sessionId.isBlank()) {
                            System.out.println(
                                    "TCSESSIONID was not received"
                            );
                            return false;
                        }

                        csrfToken = extractCsrfToken(response.asString());

                        System.out.println(
                                "TCSESSIONID received: " + sessionId
                        );

                        System.out.println(
                                "CSRF token received: " + csrfToken
                        );

                        return csrfToken != null
                                && !csrfToken.isBlank();

                    } catch (Exception e) {
                        System.out.println(
                                "Waiting for TeamCity installation page: "
                                        + e.getMessage()
                        );

                        return false;
                    }
                });
    }

    private String extractCsrfToken(String html) {
        Pattern pattern = Pattern.compile(
                "name=\"tc-csrf-token\"\\s+content=\"([^\"]+)\""
        );

        Matcher matcher = pattern.matcher(html);

        if (!matcher.find()) {
            throw new IllegalStateException(
                    "CSRF token was not found on TeamCity installation page"
            );
        }

        return matcher.group(1);
    }

    private void proceedInstallation() {
        post(
                "/mnt/do/goNewInstallation",
                request -> request.formParam("restore", "false")
        );
    }

    private void proceedDatabase() {
        post(
                "/mnt/do/goNewDatabase",
                request -> request.formParam("dbType", "HSQLDB2")
        );
    }

    private void acceptLicense() {
        post(
                "/mnt/do/acceptLicenseAgreementAndSendUsageStatistics"
        );
    }

    private void post(
            String endpoint,
            RequestCustomizer customizer
    ) {
        RequestSpecification request = given()
                .baseUri(BASE_URL)
                .cookie(SESSION_COOKIE, sessionId)
                .header(CSRF_HEADER, csrfToken)
                .header("X-Requested-With", "XMLHttpRequest")
                .header("Accept", "*/*")
                .header("Origin", BASE_URL)
                .header("Referer", BASE_URL + "/mnt")
                .contentType(
                        "application/x-www-form-urlencoded; charset=UTF-8"
                );

        customizer.customize(request);

        Response response = request
                .when()
                .post(endpoint);

        System.out.println(
                endpoint + " -> " + response.statusCode()
        );

        if (response.statusCode() != 200) {
            System.out.println(
                    "Response body:\n" + response.asPrettyString()
            );
        }

        response.then().statusCode(200);
    }

    private void post(String endpoint) {
        post(endpoint, request -> {
        });
    }

    private void waitForTeamCityReady() {
        await()
                .atMost(Duration.ofMinutes(5))
                .pollInterval(Duration.ofSeconds(3))
                .until(() -> {
                    try {
                        int statusCode = given()
                                .baseUri(BASE_URL)
                                .when()
                                .get(READY_ENDPOINT)
                                .statusCode();

                        System.out.println(
                                "TeamCity readiness status: "
                                        + statusCode
                        );

                        return statusCode == 200;

                    } catch (Exception e) {
                        return false;
                    }
                });
    }

    @FunctionalInterface
    private interface RequestCustomizer {

        void customize(RequestSpecification request);
    }
}
