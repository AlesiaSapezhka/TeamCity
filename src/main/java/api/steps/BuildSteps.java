package api.steps;

import api.generators.CommandLineCommand;
import api.generators.RandomModelGenerator;
import api.models.build.BuildResponse;
import api.models.build.RunBuildRequest;
import api.models.build_step.BuildStepResponse;
import api.models.build_step.CreateBuildStepRequest;
import api.models.build_step.Properties;
import api.models.build_step.Property;
import api.models.build_type.BuildTypeResponse;
import api.models.build_type.CreateBuildTypeRequest;
import api.models.project.ProjectResponse;
import api.requesters.CrudRequester;
import api.requesters.ValidatedCrudRequester;
import api.requesters.interfaces.Endpoints;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import common.UserContext;
import org.awaitility.Awaitility;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

public final class BuildSteps {
    private static final String BLANK_BUILD_MESSAGE = "When creating a build type, non empty name should be provided.";
    private static final String NOT_FOUND_STATUS_TEXT = "Responding with error, status code: 404 (Not Found).";
    private static final String BAD_REQUEST_STATUS_TEXT = "Responding with error, status code: 400 (Bad Request).";
    private static final String NOT_EXISTING_BUILD_MESSAGE = "Invalid value of dimension 'id': 'null'. Should be a number.";

    private BuildSteps() {
    }

    public static CreateBuildTypeRequest buildValid(String projectId) {
        CreateBuildTypeRequest request =
                RandomModelGenerator.generate(CreateBuildTypeRequest.class);
        request.setProject(ProjectResponse.builder()
                .id(projectId)
                .build());

        return request;
    }

    public static CreateBuildTypeRequest buildBlankName(String projectId) {
        CreateBuildTypeRequest request =
                CreateBuildTypeRequest.builder().name("").build();

        request.setProject(ProjectResponse.builder()
                .id(projectId)
                .build());

        return request;
    }

    public static BuildTypeResponse createBuild(CreateBuildTypeRequest createBuildRequest, UserContext user) {
        return new ValidatedCrudRequester<BuildTypeResponse>(
                RequestSpecs.userSpec(user),
                Endpoints.BUILD_TYPES,
                ResponseSpecs.requestReturnsOK()
        ).post(createBuildRequest);
    }

    public static BuildTypeResponse createBuildInvalid(CreateBuildTypeRequest createBuildRequest, UserContext user) {
        return new ValidatedCrudRequester<BuildTypeResponse>(
                RequestSpecs.userSpec(user),
                Endpoints.BUILD_TYPES,
                ResponseSpecs.requestReturnsBadRequest(BAD_REQUEST_STATUS_TEXT, BLANK_BUILD_MESSAGE)
        ).post(createBuildRequest);
    }

    public static List<BuildTypeResponse> getAllBuilds(String jsonPath, UserContext user) {
        return new ValidatedCrudRequester<BuildTypeResponse>(
                RequestSpecs.userSpec(user),
                Endpoints.BUILD_TYPES,
                ResponseSpecs.requestReturnsOK()
        ).getList(jsonPath);
    }

    public static BuildResponse runBuild(String buildTypeId, UserContext user) {
        RunBuildRequest request = createRunBuildRequest(buildTypeId);

        return new ValidatedCrudRequester<BuildResponse>(
                RequestSpecs.userSpec(user),
                Endpoints.BUILD_QUEUE,
                ResponseSpecs.requestReturnsOK()
        ).post(request);
    }

    public static BuildResponse getBuild(Integer buildId, UserContext user) {
        return new ValidatedCrudRequester<BuildResponse>(
                RequestSpecs.userSpec(user),
                Endpoints.BUILD,
                ResponseSpecs.requestReturnsOK()
        ).get(Map.of("buildLocator", "id:" + buildId));
    }

    public static BuildResponse getNotExistingBuild(Integer buildId, UserContext user) {
        return new ValidatedCrudRequester<BuildResponse>(
                RequestSpecs.userSpec(user),
                Endpoints.BUILD,
                ResponseSpecs.requestReturnsBadRequest(BAD_REQUEST_STATUS_TEXT, NOT_EXISTING_BUILD_MESSAGE)
        ).get(Map.of("buildLocator", "id:" + buildId));
    }

    public static BuildResponse runBuildWithoutConfiguration(String buildId, UserContext user) {
        RunBuildRequest request = createRunBuildRequest(buildId);

        return new ValidatedCrudRequester<BuildResponse>(
                RequestSpecs.userSpec(user),
                Endpoints.BUILD_QUEUE,
                ResponseSpecs.requestReturnsNotFound(NOT_FOUND_STATUS_TEXT, runBuildWithoutConfigurationMessage(buildId))
        ).post(request);
    }

    private static String runBuildWithoutConfigurationMessage(String buildId) {
        return "No build type nor template is found by id " + "'" + buildId + "'.";
    }

    public static BuildResponse waitForBuild(Integer buildId, UserContext user) {
        return Awaitility.await()
                .atMost(Duration.ofMinutes(1))
                .pollInterval(Duration.ofSeconds(5))
                .until(
                        () -> getBuild(Math.toIntExact(buildId), user),
                        build -> "finished".equalsIgnoreCase(build.getState())
                );
    }

    public static CreateBuildStepRequest commandLine(CommandLineCommand command) {
        CreateBuildStepRequest request =
                RandomModelGenerator.generate(CreateBuildStepRequest.class);

        request.setType("simpleRunner");

        Property executable = new Property();
        executable.setName("command.executable");
        executable.setValue(command.executable());

        Property parameters = new Property();
        parameters.setName("command.parameters");
        parameters.setValue(command.parameters());

        Properties properties = new Properties();
        properties.setProperty(List.of(executable, parameters));

        request.setProperties(properties);

        return request;
    }

    public static void addBuildStep(
            String buildTypeId,
            CreateBuildStepRequest stepRequest,
            UserContext user) {

        new CrudRequester(
                RequestSpecs.userSpec(user),
                Endpoints.BUILD_TYPE_STEPS,
                ResponseSpecs.requestReturnsOK()
        ).post(
                stepRequest,
                Map.of("btLocator", "id:" + buildTypeId)
        );
    }

    public static BuildTypeResponse getBuild(String buildId, UserContext user) {
        return new ValidatedCrudRequester<BuildTypeResponse>(
                RequestSpecs.userSpec(user),
                Endpoints.BUILD_TYPE,
                ResponseSpecs.requestReturnsOK()
        ).get(Map.of("btLocator", "id:" + buildId));
    }

    public static BuildTypeResponse getBuildByName(String projectId, String buildName, UserContext user) {
        String locator = "name:" + buildName + ",project:(id:" + projectId + ")";
        return new ValidatedCrudRequester<BuildTypeResponse>(
                RequestSpecs.userSpec(user),
                Endpoints.BUILD_TYPE,
                ResponseSpecs.requestReturnsOK()
        ).get(Map.of("btLocator", locator));
    }

    public static CreateBuildStepRequest customScriptStep(String stepId, String stepName, String script) {
        Property scriptContent = new Property();
        scriptContent.setName("script.content");
        scriptContent.setValue(script);

        Property useCustomScript = new Property();
        useCustomScript.setName("use.custom.script");
        useCustomScript.setValue("true");

        Properties properties = new Properties();
        properties.setProperty(List.of(scriptContent, useCustomScript));

        return CreateBuildStepRequest.builder()
                .id(stepId)
                .name(stepName)
                .type("simpleRunner")
                .properties(properties)
                .build();
    }

    public static void assertBuildStep(
            BuildTypeResponse actual,
            String expectedBuildTypeId,
            String expectedBuildTypeName,
            String expectedProjectName,
            CreateBuildStepRequest stepRequest
    ) {
        assertAll(
                () -> assertEquals(expectedBuildTypeId, actual.getId()),
                () -> assertEquals(expectedBuildTypeName, actual.getName()),
                () -> assertEquals(expectedProjectName, actual.getProjectName()),

                () -> assertEquals(1, actual.getSteps().getCount()),
                () -> assertEquals(1, actual.getSteps().getStep().size())
        );

        BuildStepResponse actualStep = actual.getSteps().getStep().getFirst();

        assertAll(
                () -> assertEquals(stepRequest.getName(), actualStep.getName()),
                () -> assertEquals(stepRequest.getType(), actualStep.getType())
        );

        for (Property expectedProperty : stepRequest.getProperties().getProperty()) {

            Property actualProperty = actualStep.getProperties().getProperty().stream()
                    .filter(property ->
                            expectedProperty.getName().equals(property.getName()))
                    .findFirst()
                    .orElseThrow(() ->
                            new AssertionError(
                                    "Property '%s' not found"
                                            .formatted(expectedProperty.getName())
                            ));

            assertEquals(
                    expectedProperty.getValue(),
                    actualProperty.getValue(),
                    "Wrong value for property: " + expectedProperty.getName()
            );
        }
    }

    public static RunBuildRequest createRunBuildRequest(String buildTypeId) {
        return RunBuildRequest.builder()
                .buildType(
                        RunBuildRequest.BuildTypeReference.builder()
                                .id(buildTypeId)
                                .build()
                )
                .build();
    }
}
