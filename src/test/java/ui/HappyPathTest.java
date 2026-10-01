package ui;

import api.generators.BuildCommands;
import api.generators.CommandLineCommand;
import api.generators.RandomData;
import api.models.build_step.CreateBuildStepRequest;
import api.models.project.CreateProjectRequest;
import api.steps.BuildSteps;
import api.steps.ProjectSteps;
import common.UserContext;
import common.annotations.CreateAndDeleteUser;
import common.annotations.EnableAgent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceAccessMode;
import org.junit.jupiter.api.parallel.ResourceLock;
import ui.pages.LoginPage;

public class HappyPathTest extends BaseUiTest {

    @Test
    @CreateAndDeleteUser
    @EnableAgent(enabled = true)
    @ResourceLock(
            value = "teamcity-agent",
            mode = ResourceAccessMode.READ
    )
    void fullHappyPathViaUI(UserContext user) {
        String buildName = RandomData.getBuildName();
        CommandLineCommand command = BuildCommands.randomCommandLineCommand();
        CreateBuildStepRequest buildStepRequest = BuildSteps.commandLine(command);

        CreateProjectRequest projectRequest = ProjectSteps.buildProjectValid();

        new LoginPage()
                .open()
                .login(user.username(), user.password())
                .goToMainPage()
                .welcomeMessageShouldBeVisible()
                .createProject()
                .goToProjectPage()
                .projectPageShouldBeOpened()
                .enterProjectName(projectRequest.getName())
                .enterProjectId(projectRequest.getId())
                .enterProjectDescription(projectRequest.getDescription())
                .createProject()
                .shouldShowConnectionStep()
                .proceedWithoutRepository()
                .shouldShowSetupBuildStep()
                .skipSetup()
                .projectTitleCheck(projectRequest.getName())
                .createBuildConfiguration()
                .goToBuildConfiguration()
                .shouldShowSetupYourBuild()
                .parentProjectShouldBeSet(projectRequest.getName())
                .enterBuildName(buildName)
                .createBuild()
                .buildShouldBeOpened(buildName)
                .openBuildStepsTab()
                .addBuildSteps()
                .selectCommandLine()
                .enterBuildStepName(buildStepRequest.getName())
                .enterBuildStepId(buildStepRequest.getId())
                .clickSaveButton()
                .buildCanNotBeCreatedWithoutScript()
                .enterStepCommand(command.executable() + " " + command.parameters())
                .clickSaveButton()
                .buildSettingsUpdates()
                .shouldHaveBuildSteps(buildStepRequest.getName(), command.parameters())
                .goToRunBuild()
                .runBuildFromProject()
                .checkSuccessBuildIcon()
                .openBuildLog()
                .refreshPage()
                .expandBuildLog(buildStepRequest.getName())
                .refreshPage()
                .shouldContainCommand(command.parameters());

        ProjectSteps.deleteProject(projectRequest.getId(), user);
    }
}
