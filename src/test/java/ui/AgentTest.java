package ui;

import api.generators.RandomData;
import api.models.agent.AgentResponse;
import api.steps.AgentSteps;
import common.annotations.CreateUserAndLogIn;
import common.annotations.EnableAgent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceAccessMode;
import org.junit.jupiter.api.parallel.ResourceLock;
import ui.pages.AgentPage;

import java.util.List;

public class AgentTest extends BaseUiTest {

    @Test
    @CreateUserAndLogIn
    @EnableAgent(enabled = true)
    @ResourceLock(
            value = "teamcity-agent",
            mode = ResourceAccessMode.READ
    )
    void userCanOpenAgentsOverviewAndSeeActiveAgent() {
        new AgentPage()
                .open()
                .verifyAgentOverviewState()
                .verifyAgentIsEnabled()
                .verifyAgentIpAddress(AgentSteps.findAgent().getName());

        List<AgentResponse> agentsList = AgentSteps.getAllAgents();
        softly.assertThat(agentsList).isNotEmpty();
        softly.assertThat(agentsList.size()).isEqualTo(1);
    }

    @Test
    @CreateUserAndLogIn
    @EnableAgent(enabled = true)
    @ResourceLock(
            value = "teamcity-agent",
            mode = ResourceAccessMode.READ_WRITE
    )
    void userCanToggleAgentStatusWithComments() {
        new AgentPage()
                .open()
                .verifyAgentIsEnabled()
                .clickAgentToggle()
                .enterComment(RandomData.getComment())
                .confirmDisable()
                .verifyAgentIsDisabled()
                .clickAgentToggle()
                .enterComment(RandomData.getComment())
                .confirmEnable()
                .verifyAgentIsEnabled();

        AgentSteps.assertAgentReady(AgentSteps.findAgent());
    }
}
