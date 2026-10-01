package api;

import api.generators.BuildCommands;
import api.generators.CommandLineCommand;
import api.generators.RandomData;
import api.models.build.BuildResponse;
import api.models.build_step.CreateBuildStepRequest;
import api.models.build_type.BuildTypeResponse;
import api.models.build_type.CreateBuildTypeRequest;
import api.models.comparison.ModelAssertions;
import api.steps.BuildSteps;
import common.ProjectContext;
import common.annotations.CreateAndDeleteProject;
import common.annotations.CreateAndDeleteUser;
import common.annotations.EnableAgent;
import common.data.BuildInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceAccessMode;
import org.junit.jupiter.api.parallel.ResourceLock;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RunBuildTest extends BaseTest {
    @Test
    @CreateAndDeleteUser
    @CreateAndDeleteProject
    @EnableAgent(enabled = true)
    @ResourceLock(
            value = "teamcity-agent",
            mode = ResourceAccessMode.READ
    )
    void userCanRunBuildWithValidData(ProjectContext project) {
        CreateBuildTypeRequest buildRequest = BuildSteps.buildValid(project.projectId());

        BuildTypeResponse buildResponse =
                BuildSteps.createBuild(buildRequest, project.user());

        ModelAssertions.assertThatModels(buildRequest, buildResponse).match();
        softly.assertThat(buildResponse.getId()).isNotBlank();

        String buildTypeId = buildResponse.getId();

        CommandLineCommand command = BuildCommands.randomCommandLineCommand();
        CreateBuildStepRequest buildStepRequest = BuildSteps.commandLine(command);
        BuildSteps.addBuildStep(buildTypeId, buildStepRequest, project.user());

        BuildTypeResponse build = BuildSteps.getBuild(buildTypeId, project.user());
        BuildSteps.assertBuildStep(
                build,
                buildTypeId,
                buildResponse.getName(),
                project.projectName(),
                buildStepRequest
        );

        BuildResponse buildRun = BuildSteps.runBuild(buildTypeId, project.user());
        BuildResponse finishedBuild = BuildSteps.waitForBuild(buildRun.getId(), project.user());

        softly.assertThat(finishedBuild.getState())
                .isEqualTo(BuildInfo.FINISHED_STATE.getValue());

        softly.assertThat(finishedBuild.getStatus())
                .isEqualTo(BuildInfo.SUCCESS_STATUS.getValue());
    }

    @Test
    @CreateAndDeleteUser
    @CreateAndDeleteProject
    void userCanNotRunBuildWithoutBuildConfiguration(ProjectContext project) {
        BuildResponse buildRun = BuildSteps.runBuildWithoutConfiguration(RandomData.getId(), project.user());
        BuildResponse build = BuildSteps.getNotExistingBuild(buildRun.getId(), project.user());

        softly.assertThat(build.getId()).isNull();
    }

    @Test
    @CreateAndDeleteUser
    @CreateAndDeleteProject
    @EnableAgent(enabled = false)
    @ResourceLock(
            value = "teamcity-agent",
            mode = ResourceAccessMode.READ_WRITE
    )
    void userCanNotRunBuildWithoutConnectedAgent(ProjectContext project) {
        CreateBuildTypeRequest buildRequest = BuildSteps.buildValid(project.projectId());

        BuildTypeResponse buildResponse =
                BuildSteps.createBuild(buildRequest, project.user());

        ModelAssertions.assertThatModels(buildRequest, buildResponse).match();
        softly.assertThat(buildResponse.getId()).isNotBlank();

        String buildTypeId = buildResponse.getId();

        CommandLineCommand command = BuildCommands.randomCommandLineCommand();
        CreateBuildStepRequest buildStepRequest = BuildSteps.commandLine(command);
        BuildSteps.addBuildStep(buildTypeId, buildStepRequest, project.user());

        BuildTypeResponse build = BuildSteps.getBuild(buildResponse.getId(), project.user());
        BuildSteps.assertBuildStep(
                build,
                buildTypeId,
                buildResponse.getName(),
                project.projectName(),
                buildStepRequest
        );

        BuildResponse buildRun = BuildSteps.runBuild(buildTypeId, project.user());
        BuildResponse buildInfo = BuildSteps.getBuild(buildRun.getId(), project.user());

        assertEquals(
                BuildInfo.QUEUED_STATE.getValue(),
                buildInfo.getState()
        );

        assertEquals(
                BuildInfo.WAIT_REASON.getValue(),
                buildInfo.getWaitReason()
        );
    }
}
