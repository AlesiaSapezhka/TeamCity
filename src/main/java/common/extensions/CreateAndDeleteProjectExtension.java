package common.extensions;

import api.models.project.CreateProjectRequest;
import api.models.project.ProjectResponse;
import api.steps.ProjectSteps;
import common.ProjectContext;
import common.UserContext;
import common.UserContexts;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.extension.*;


@Order(10)
public class CreateAndDeleteProjectExtension
        implements BeforeEachCallback, AfterEachCallback, ParameterResolver {

    private static final String PROJECT_CONTEXT = "projectContext";

    private static final ExtensionContext.Namespace NAMESPACE =
            ExtensionContext.Namespace.create(CreateAndDeleteProjectExtension.class);

    @Override
    public void beforeEach(ExtensionContext context) {
        UserContext user = UserContexts.require(context);

        CreateProjectRequest projectRequest = ProjectSteps.buildProjectValid();
        ProjectResponse projectResponse = ProjectSteps.createProject(projectRequest, user);

        ProjectContext projectContext = new ProjectContext(
                projectResponse.getId(),
                projectResponse.getName(),
                user
        );
        context.getStore(NAMESPACE).put(PROJECT_CONTEXT, projectContext);
    }

    @Override
    public void afterEach(ExtensionContext context) {
        ProjectContext projectContext = context.getStore(NAMESPACE)
                .remove(PROJECT_CONTEXT, ProjectContext.class);

        if (projectContext != null) {
            ProjectSteps.deleteProject(projectContext.projectId(), projectContext.user());
        }
    }

    @Override
    public boolean supportsParameter(
            ParameterContext parameterContext,
            ExtensionContext extensionContext) {
        return parameterContext.getParameter().getType().equals(ProjectContext.class);
    }

    @Override
    public Object resolveParameter(
            ParameterContext parameterContext,
            ExtensionContext extensionContext) {
        ProjectContext projectContext = extensionContext.getStore(NAMESPACE)
                .get(PROJECT_CONTEXT, ProjectContext.class);
        if (projectContext == null) {
            throw new IllegalStateException(
                    "ProjectContext is not initialized. "
                            + "CreateAndDeleteProjectExtension.beforeEach() was not executed."
            );
        }
        return projectContext;
    }
}
