package common.extensions;

import api.generators.RandomData;
import api.models.agent.AgentResponse;
import api.steps.AgentSteps;
import common.annotations.EnableAgent;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class EnableAgentExtension implements BeforeEachCallback {

    @Override
    public void beforeEach(ExtensionContext context) {

        EnableAgent annotation = context.getRequiredTestMethod()
                .getAnnotation(EnableAgent.class);

        boolean enabled = annotation.enabled();

        AgentResponse agent = AgentSteps.findAgent();

        AgentSteps.updateAgentEnabledStatus(
                agent.getId(),
                enabled,
                RandomData.getComment()
        );

        AgentResponse agentAfterChangeStatus = AgentSteps.findAgent();
        if (enabled) {
            AgentSteps.assertAgentReady(agentAfterChangeStatus);
        }
    }
}
