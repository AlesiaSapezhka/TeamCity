package ui;

import api.models.comparison.ModelAssertions;
import api.models.project.CreateProjectRequest;
import api.models.project.ProjectResponse;
import api.steps.ProjectSteps;
import common.UserContext;
import common.annotations.CreateUserAndLogIn;
import common.data.JsonPaths;
import org.junit.jupiter.api.Test;
import ui.pages.MainPage;

import java.util.List;

public class CreateProjectTest extends BaseUiTest {

    @Test
    @CreateUserAndLogIn
    void userCanCreateProjectWithValidName(UserContext user) {
        CreateProjectRequest projectRequest = ProjectSteps.buildProjectValid();

        new MainPage()
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
                .projectTitleCheck(projectRequest.getName());

        ProjectResponse project = ProjectSteps.getProject(projectRequest, user);
        ModelAssertions.assertThatModels(projectRequest, project).match();

        ProjectSteps.deleteProject(projectRequest.getId(), user);
    }

    @Test
    @CreateUserAndLogIn
    void userCanNotCreateProjectWithBlankName(UserContext user) {
        CreateProjectRequest projectRequest = ProjectSteps.buildProjectBlankName();

        new MainPage()
                .createProject()
                .goToProjectPage()
                .projectPageShouldBeOpened()
                .enterProjectName(projectRequest.getName())
                .createProject()
                .checkErrorMessage();

        List<ProjectResponse> projects = ProjectSteps.getAllProjects(JsonPaths.PROJECTS.getPath(), user);
        softly.assertThat(projects).noneSatisfy(foundProject ->
                ModelAssertions.assertThatModels(projectRequest, foundProject).match());
    }
}
