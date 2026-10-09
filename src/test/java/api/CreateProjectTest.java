package api;

import api.models.comparison.ModelAssertions;
import api.models.project.CreateProjectRequest;
import api.models.project.ProjectResponse;
import api.steps.ProjectSteps;
import common.UserContext;
import common.annotations.CreateAndDeleteUser;
import common.data.JsonPaths;
import org.junit.jupiter.api.Test;

import java.util.List;

public class CreateProjectTest extends BaseTest {
    private String projectId;

    @Test
    @CreateAndDeleteUser
    void userCanCreateProjectWithValidData(UserContext user) {
        CreateProjectRequest projectRequest = ProjectSteps.buildProjectValid();
        ProjectResponse projectResponse =
                ProjectSteps.createProject(projectRequest, user);
        projectId = projectResponse.getId();

        ModelAssertions.assertThatModels(projectRequest, projectResponse).match();
        softly.assertThat(projectResponse.getId()).isNotBlank();

        ProjectResponse project = ProjectSteps.getProject(projectRequest, user);
        ModelAssertions.assertThatModels(projectRequest, project).match();

        ProjectSteps.deleteProject(projectId, user);
    }

    @Test
    @CreateAndDeleteUser
    void userCanNotCreateProjectWithBlankName(UserContext user) {
        CreateProjectRequest projectRequest = ProjectSteps.buildProjectBlankName();

        List<ProjectResponse> projects = ProjectSteps.getAllProjects(JsonPaths.PROJECTS.getPath(), user);
        softly.assertThat(projects).noneSatisfy(foundProject ->
                ModelAssertions.assertThatModels(projectRequest, foundProject).match());
    }

    @Test
    @CreateAndDeleteUser
    void userCanNotCreateProjectWithDuplicateId(UserContext user) {
        CreateProjectRequest project1Request = ProjectSteps.buildProjectValid();
        ProjectSteps.createProject(project1Request, user);
        projectId = project1Request.getId();

        CreateProjectRequest project2Request = ProjectSteps.buildProjectValid();
        project2Request.setId(projectId);
        ProjectSteps.createProjectDuplicateId(project2Request, projectId, user);

        List<ProjectResponse> projects = ProjectSteps.getAllProjects(JsonPaths.PROJECTS.getPath(), user);
        softly.assertThat(projects).noneSatisfy(foundProject ->
                ModelAssertions.assertThatModels(project2Request, foundProject).match());

        ProjectSteps.deleteProject(projectId, user);
    }
}
