package api.steps;

import api.models.agent.AgentEnabledInfoRequest;
import api.models.agent.AgentResponse;
import api.requesters.ValidatedCrudRequester;
import api.requesters.interfaces.Endpoints;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AgentSteps {

    private static final String AGENT_NAME = "teamcity-agent";

    public static void ensureAgentReady() {

        await()
                .atMost(Duration.ofMinutes(3))
                .pollInterval(Duration.ofSeconds(10))
                .untilAsserted(() -> {

                    AgentResponse agent = findAgent();

                    if (!Boolean.TRUE.equals(agent.getAuthorized())) {
                        authorizeAgent(agent.getId());
                    }

                    if (!Boolean.TRUE.equals(agent.getEnabled())) {
                        updateAgentEnabledStatus(
                                agent.getId(),
                                true,
                                "Enable agent for automated tests"
                        );
                    }

                    AgentResponse actual = findAgent();

                    assertThat(actual.getConnected())
                            .as("Agent must be connected")
                            .isTrue();

                    assertThat(actual.getAuthorized())
                            .as("Agent must be authorized")
                            .isTrue();

                    assertThat(actual.getEnabled())
                            .as("Agent must be enabled")
                            .isTrue();
                });
    }

    public static AgentResponse findAgent() {

        ValidatedCrudRequester<AgentResponse> agentsRequester =
                new ValidatedCrudRequester<>(
                        RequestSpecs.userSpec(),
                        Endpoints.AGENTS_ANY,
                        ResponseSpecs.requestReturnsOK()
                );

        AgentResponse agent = agentsRequester.getList("agent")
                .stream()
                .filter(item -> AGENT_NAME.equals(item.getName()))
                .findFirst()
                .orElseThrow(() ->
                        new AssertionError(
                                "Agent not found: " + AGENT_NAME
                        )
                );

        return new ValidatedCrudRequester<AgentResponse>(
                RequestSpecs.userSpec(),
                Endpoints.AGENT,
                ResponseSpecs.requestReturnsOK()
        ).get(
                Map.of("agentLocator", "id:" + agent.getId())
        );
    }

    private static void authorizeAgent(int agentId) {

        AgentEnabledInfoRequest request =
                new AgentEnabledInfoRequest(
                        true,
                        "Authorize agent for automated tests"
                );

        new ValidatedCrudRequester<AgentResponse>(
                RequestSpecs.userSpec(),
                Endpoints.AGENT_AUTHORIZED,
                ResponseSpecs.requestReturnsOK()
        ).update(
                request,
                Map.of("agentLocator", "id:" + agentId)
        );
    }

    public static void updateAgentEnabledStatus(
            int agentId,
            boolean enabled,
            String comment) {

        AgentEnabledInfoRequest request =
                new AgentEnabledInfoRequest(enabled, comment);

        new ValidatedCrudRequester<AgentResponse>(
                RequestSpecs.userSpec(),
                Endpoints.ENABLE_AGENT,
                ResponseSpecs.requestReturnsOK()
        ).update(
                request,
                Map.of("agentLocator", "id:" + agentId)
        );
    }

    public static void assertAgentReady(AgentResponse agent) {

        assertAll(
                () -> assertTrue(
                        agent.getConnected(),
                        "Agent is not connected"
                ),
                () -> assertTrue(
                        agent.getAuthorized(),
                        "Agent is not authorized"
                ),
                () -> assertTrue(
                        agent.getEnabled(),
                        "Agent is disabled"
                )
        );
    }

    public static List<AgentResponse> getAllAgents() {

        return new ValidatedCrudRequester<AgentResponse>(
                RequestSpecs.userSpec(),
                Endpoints.AGENTS,
                ResponseSpecs.requestReturnsOK()
        ).getList("agent");
    }
}
