package api.steps;

import api.generators.RandomModelGenerator;
import api.models.project.CreateProjectRequest;
import api.models.project.ProjectResponse;
import api.requesters.CrudRequester;
import api.requesters.ValidatedCrudRequester;
import api.requesters.interfaces.Endpoints;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import io.restassured.response.ValidatableResponse;

import java.util.List;
import java.util.Map;

public final class ProjectSteps {
    private static final String BLANK_PROJECT_MESSAGE = "Project name cannot be empty.";
    private static final String BAD_REQUEST_STATUS_TEXT = "Responding with error, status code: 400 (Bad Request).";

    private ProjectSteps() {
    }

    public static CreateProjectRequest buildProjectValid() {
        return RandomModelGenerator.generate(CreateProjectRequest.class);
    }

    public static CreateProjectRequest buildProjectBlankName() {
        return CreateProjectRequest.builder()
                .name(" ")
                .build();
    }

    public static ProjectResponse createProject(CreateProjectRequest createProjectRequest) {
        return new ValidatedCrudRequester<ProjectResponse>(
                RequestSpecs.userSpec(),
                Endpoints.PROJECTS,
                ResponseSpecs.requestReturnsOK()
        ).post(createProjectRequest);
    }

    public static void createProjectBlankName(CreateProjectRequest request) {
        new CrudRequester(
                RequestSpecs.userSpec(),
                Endpoints.PROJECTS,
                ResponseSpecs.requestReturnsBadRequest(BAD_REQUEST_STATUS_TEXT, BLANK_PROJECT_MESSAGE)
        ).post(request);
    }

    public static void createProjectDuplicateId(CreateProjectRequest request, String projectId) {
        new CrudRequester(
                RequestSpecs.userSpec(),
                Endpoints.PROJECTS,
                ResponseSpecs.requestReturnsBadRequest(BAD_REQUEST_STATUS_TEXT, duplicateProjectIdMessage(projectId))
        ).post(request);
    }

    private static String duplicateProjectIdMessage(String projectId) {
        return "Project ID \"" + projectId + "\" is already used by another project";
    }

    public static ProjectResponse getProject(CreateProjectRequest request) {
        return new ValidatedCrudRequester<ProjectResponse>(
                RequestSpecs.userSpec(),
                Endpoints.PROJECT,
                ResponseSpecs.requestReturnsOK()
        ).get(Map.of("projectLocator", "id:" + request.getId()));
    }

    public static List<ProjectResponse> getAllProjects(String jsonPath) {
        return new ValidatedCrudRequester<ProjectResponse>(
                RequestSpecs.userSpec(),
                Endpoints.PROJECTS,
                ResponseSpecs.requestReturnsOK()
        ).getList(jsonPath);
    }

    public static ValidatableResponse deleteProject(String projectId) {
        return new CrudRequester(
                RequestSpecs.userSpec(),
                Endpoints.PROJECT,
                ResponseSpecs.entityWasDeleted()
        ).delete(Map.of("projectLocator", "id:" + projectId));
    }
}
