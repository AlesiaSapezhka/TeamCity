package api.specs;

import api.configs.Config;
import api.configs.SuperUserTokenResolver;
import common.UserContext;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

public final class RequestSpecs {

    private RequestSpecs() {
    }

    private static RequestSpecBuilder defaultRequestBuilder() {
        return defaultRequestBuilder(restBasePath());
    }

    private static RequestSpecBuilder defaultRequestBuilder(String restPath) {
        String baseUrl = Config.getProperty("apiBaseUrl");
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = "http://localhost:8111";
        }

        return new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .addFilters(List.of(
                        new RequestLoggingFilter(),
                        new ResponseLoggingFilter(),
                        new AllureRestAssured()
                ))
                .setBaseUri(baseUrl + restPath);
    }

    private static String restBasePath() {
        String configured = Config.getProperty("apiVersion");
        if (configured != null && !configured.isBlank()) {
            return configured.trim();
        }
        return "/app/rest";
    }

    public static RequestSpecification baseSpec() {
        return defaultRequestBuilder().build();
    }

    public static RequestSpecification superUserSpec() {
        return defaultRequestBuilder("/httpAuth/app/rest")
                .addHeader("Authorization", basicAuthHeader("", SuperUserTokenResolver.resolve()))
                .build();
    }

    public static RequestSpecification authAsUserSpec(String username, String password) {
        return defaultRequestBuilder()
                .addHeader("Authorization", basicAuthHeader(username, password))
                .build();
    }

    public static RequestSpecification bearerSpec(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalStateException("Bearer token is null/empty");
        }
        String value = token.startsWith("Bearer ") ? token.substring("Bearer ".length()).trim() : token.trim();
        return defaultRequestBuilder("/app/rest")
                .addHeader("Authorization", "Bearer " + value)
                .build();
    }

    public static RequestSpecification userSpec(UserContext user) {
        if (user == null || user.token() == null || user.token().isBlank()) {
            throw new IllegalStateException("UserContext.token is null/empty");
        }
        return bearerSpec(user.token());
    }

    public static String basicAuthHeader(String username, String password) {
        String raw = (username == null ? "" : username) + ":" + (password == null ? "" : password);
        String encoded = Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
        return "Basic " + encoded;
    }
}
