package api.requesters;

import api.models.BaseModel;
import api.requesters.interfaces.CrudEndpointInterface;
import api.requesters.interfaces.Endpoints;
import common.helpers.StepLogger;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

import java.util.Collections;
import java.util.Map;

import static io.restassured.RestAssured.given;

public class CrudRequester extends HttpRequest implements CrudEndpointInterface {

    public CrudRequester(
            RequestSpecification requestSpecification,
            Endpoints endpoints,
            ResponseSpecification responseSpecification) {
        super(requestSpecification, endpoints, responseSpecification);
    }

    @Override
    public ValidatableResponse post(BaseModel model) {
        return StepLogger.log("Post request to " + endpoints.getUrl() + " with model: " + model, () -> post(model, Collections.emptyMap()));
    }

    @Override
    public ValidatableResponse post() {
        return StepLogger.log("Post request to " + endpoints.getUrl(), () -> post(null, Collections.emptyMap()));
    }

    @Override
    public ValidatableResponse post(BaseModel model, Map<String, ?> pathParams) {
        return StepLogger.log("Post request to " + endpoints.getUrl() + " with model: " + model + " path parameters: " + pathParams, () -> {
            var request = given().spec(requestSpecification).pathParams(pathParams);
            if (model != null) {
                request.body(model);
            }
            return request
                    .post(endpoints.getUrl())
                    .then()
                    .spec(responseSpecification);
        });
    }

    @Override
    public ValidatableResponse get() {
        return StepLogger.log("Get request to " + endpoints.getUrl(), () -> get(Collections.emptyMap()));
    }

    @Override
    public ValidatableResponse get(Map<String, ?> pathParams) {
        return StepLogger.log("Get request to " + endpoints.getUrl() + " path parameters: " + pathParams, () ->
                given()
                        .spec(requestSpecification)
                        .pathParams(pathParams)
                        .when()
                        .get(endpoints.getUrl())
                        .then()
                        .spec(responseSpecification));
    }

    @Override
    public ValidatableResponse update(BaseModel model) {
        return StepLogger.log("Put request to " + endpoints.getUrl() + " with model: " + model, () ->
                update(model, Collections.emptyMap()));
    }

    @Override
    public ValidatableResponse update(BaseModel model, Map<String, ?> pathParams) {
        return StepLogger.log("Put request to " + endpoints.getUrl() + " with model: " + model + " path parameters: " + pathParams, () -> {

            var request = given().spec(requestSpecification).pathParams(pathParams);
            if (model != null) {
                request.body(model);
            }
            return request
                    .when()
                    .put(endpoints.getUrl())
                    .then()
                    .spec(responseSpecification);
        });
    }

    @Override
    public ValidatableResponse delete() {
        return StepLogger.log("Delete request to " + endpoints.getUrl(), () -> delete(Collections.emptyMap()));
    }

    @Override
    public ValidatableResponse delete(Map<String, ?> pathParams) {
        return StepLogger.log("Delete request to " + endpoints.getUrl() + " path parameters: " + pathParams, () -> given()
                .spec(requestSpecification)
                .pathParams(pathParams)
                .when()
                .delete(endpoints.getUrl())
                .then()
                .spec(responseSpecification));
    }
}
