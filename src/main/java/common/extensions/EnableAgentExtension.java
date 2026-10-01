package common.extensions;

import api.generators.RandomData;
import api.models.agent.AgentResponse;
import api.steps.AgentSteps;
import common.UserContext;
import common.UserContexts;
import common.annotations.EnableAgent;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

@Order(5)
public class EnableAgentExtension implements BeforeEachCallback {

    @Override
    public void beforeEach(ExtensionContext context) {
        EnableAgent annotation = context.getRequiredTestMethod()
                .getAnnotation(EnableAgent.class);
        boolean enabled = annotation.enabled();

        UserContext user = UserContexts.require(context);
        AgentResponse agent = AgentSteps.findAgent(user);

        AgentSteps.updateAgentEnabledStatus(
                agent.getId(),
                enabled,
                RandomData.getComment(),
                user
        );

        AgentResponse agentAfterChangeStatus = AgentSteps.findAgent(user);
        if (enabled) {
            AgentSteps.assertAgentReady(agentAfterChangeStatus);
        }
    }
}
