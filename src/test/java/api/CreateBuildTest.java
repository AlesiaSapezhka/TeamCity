package api;

import api.models.build_type.BuildTypeResponse;
import api.models.build_type.CreateBuildTypeRequest;
import api.models.comparison.ModelAssertions;
import api.steps.BuildSteps;
import common.ProjectContext;
import common.annotations.CreateAndDeleteProject;
import common.annotations.CreateAndDeleteUser;
import common.data.JsonPaths;
import org.junit.jupiter.api.Test;

import java.util.List;

public class CreateBuildTest extends BaseTest {

    @Test
    @CreateAndDeleteUser
    @CreateAndDeleteProject
    void userCanCreateBuildWithValidData(ProjectContext project) {
        CreateBuildTypeRequest buildRequest = BuildSteps.buildValid(project.projectId());
        BuildTypeResponse buildResponse =
                BuildSteps.createBuild(buildRequest, project.user());

        ModelAssertions.assertThatModels(buildRequest, buildResponse).match();
        softly.assertThat(buildResponse.getId()).isNotBlank();

        BuildTypeResponse build = BuildSteps.getBuild(buildResponse.getId(), project.user());
        ModelAssertions.assertThatModels(buildRequest, build).match();
    }

    @Test
    @CreateAndDeleteUser
    @CreateAndDeleteProject
    void userCanNotCreateBuildWithInvalidData(ProjectContext project) {
        CreateBuildTypeRequest buildRequest = BuildSteps.buildBlankName(project.projectId());
        BuildTypeResponse buildResponse =
                BuildSteps.createBuildInvalid(buildRequest, project.user());

        softly.assertThat(buildResponse.getId()).isBlank();

        List<BuildTypeResponse> builds = BuildSteps.getAllBuilds(JsonPaths.BUILDS.getPath(), project.user());
        softly.assertThat(builds).noneSatisfy(foundBuild ->
                ModelAssertions.assertThatModels(buildRequest, foundBuild).match());
    }
}
