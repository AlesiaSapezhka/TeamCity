package api.requesters.interfaces;

import api.models.BaseModel;
import api.models.agent.AgentResponse;
import api.models.build.BuildResponse;
import api.models.build.RunBuildRequest;
import api.models.build_step.BuildStepResponse;
import api.models.build_step.CreateBuildStepRequest;
import api.models.build_type.BuildTypeResponse;
import api.models.build_type.CreateBuildTypeRequest;
import api.models.project.CreateProjectRequest;
import api.models.project.ProjectResponse;
import api.models.server.AuthSettingsRequest;
import api.models.user.*;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Endpoints {
    USERS("/users", CreateUserRequest.class, UserResponse.class),
    USER("/users/{userLocator}", BaseModel.class, UserResponse.class),
    USER_TOKENS("/users/{userLocator}/tokens", CreateTokenRequest.class, TokenResponse.class),
    USER_ROLE("/users/{userLocator}/roles/{roleId}/{scope}", BaseModel.class, Role.class),
    CURRENT_USER("/users/current", BaseModel.class, UserResponse.class),
    AUTH_SETTINGS("/server/authSettings", AuthSettingsRequest.class, BaseModel.class),
    AGENT(
            "/agents/{agentLocator}",
            BaseModel.class,
            AgentResponse.class
    ),

    AGENTS(
            "/agents",
            BaseModel.class,
            AgentResponse.class
    ),

    // The default /agents list omits unauthorized and disconnected agents
    AGENTS_ANY(
            "/agents?locator=authorized:any,connected:any,enabled:any",
            BaseModel.class,
            AgentResponse.class
    ),

    AGENT_AUTHORIZED(
            "/agents/{agentLocator}/authorizedInfo",
            BaseModel.class,
            AgentResponse.class
    ),

    ENABLE_AGENT(
            "/agents/{agentLocator}/enabledInfo",
            BaseModel.class,
            AgentResponse.class
    ),

    PROJECTS("/projects", CreateProjectRequest.class, ProjectResponse.class),

    PROJECT("/projects/{projectLocator}", BaseModel.class, ProjectResponse.class),

    BUILD_TYPES("/buildTypes", CreateBuildTypeRequest.class, BuildTypeResponse.class),

    BUILD_TYPE("/buildTypes/{btLocator}", BaseModel.class, BuildTypeResponse.class),

    BUILD_TYPE_STEPS(
            "/buildTypes/{btLocator}/steps",
            CreateBuildStepRequest.class,
            BuildStepResponse.class
    ),

    BUILD_QUEUE("/buildQueue", RunBuildRequest.class, BuildResponse.class),

    BUILD("/builds/{buildLocator}", BaseModel.class, BuildResponse.class);

    private final String url;
    private final Class<? extends BaseModel> requestModel;
    private final Class<? extends BaseModel> responseModel;
}
