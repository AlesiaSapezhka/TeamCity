package api.specs;

import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.specification.ResponseSpecification;
import org.apache.http.HttpStatus;

import static org.hamcrest.Matchers.equalTo;

public final class ResponseSpecs {

    private ResponseSpecs() {
    }

    private static ResponseSpecBuilder defaultResponseBuilder() {
        return new ResponseSpecBuilder();
    }

    public static ResponseSpecification requestReturnsOK() {
        return defaultResponseBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .build();
    }

    public static ResponseSpecification anyStatus() {
        return defaultResponseBuilder().build();
    }

    public static ResponseSpecification requestReturnsNotFound(
            String expectedStatusText,
            String expectedMessage) {

        return defaultResponseBuilder()
                .expectStatusCode(HttpStatus.SC_NOT_FOUND)
                .expectBody("errors[0].statusText", equalTo(expectedStatusText))
                .expectBody("errors[0].message", equalTo(expectedMessage))
                .build();
    }

    public static ResponseSpecification entityWasDeleted() {
        return ResponseSpecs.defaultResponseBuilder()
                .expectStatusCode(HttpStatus.SC_NO_CONTENT)
                .build();
    }

    public static ResponseSpecification requestReturnsBadRequest(
            String expectedStatusText,
            String expectedMessage) {

        return defaultResponseBuilder()
                .expectStatusCode(HttpStatus.SC_BAD_REQUEST)
                .expectBody("errors[0].statusText", equalTo(expectedStatusText))
                .expectBody("errors[0].message", equalTo(expectedMessage))
                .build();
    }

    public static ResponseSpecification requestReturnsForbiddenRequestWithoutKey(String errorMessage) {
        return defaultResponseBuilder().expectStatusCode(HttpStatus.SC_UNAUTHORIZED).expectBody(equalTo(errorMessage)).build();
    }
}
