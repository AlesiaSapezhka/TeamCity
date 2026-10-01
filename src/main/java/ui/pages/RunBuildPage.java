package ui.pages;

import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$x;

public class RunBuildPage extends BasePage<RunBuildPage> {

    public static String AGENT_ABSENCE_ERROR_MESSAGE = "There are no idle compatible agents which can run this build";
    public static String INTERRUPT_RUN_BUILD_MESSAGE = "Stopping build on agent. Reason: stop build command from the server";

    private final SelenideElement runBuildButton = $("[data-test='run-build']");
    private final SelenideElement runBuild = $x("//span[contains(@class,'ring-button-group-split')]//button[normalize-space()='Run']");
    private final SelenideElement buildStatus = $("[data-test='ring-link build-status-link']");
    private final SelenideElement successStatus = $("[data-test='status-badge'][data-test-status='success']");
    private final SelenideElement stopBuildButton = $("button[title^='Stop build on']");
    private final SelenideElement stopBuildComment = $("#removeQueuedBuildComment");
    private final SelenideElement submitStopBuildButton = $("#submitRemoveQueuedBuild");
    private final SelenideElement buildLogTab = $("a[data-test='ring-link'][aria-label='Build Log']");
    private final SelenideElement canceledIcon =
            $("[data-test='ring-icon'][data-test-icon='canceled']");
    private final SelenideElement buildRow =
            $("button[data-test='details-summary'][aria-label='Build']");

    private SelenideElement stepLog(String stepName) {
        return $x("//div[@data-test-log-message='true']"
                + "[.//div[@data-test='log-message-text' and contains(text(), '%s')]]"
                .formatted(stepName));
    }

    private SelenideElement buildNumberLink(int buildNumber) {
        return $x("//span[@title='Build number: %d']/span[@data-test='middle-ellipsis-searchable']"
                .formatted(buildNumber));
    }

    private SelenideElement expandStepButton(String stepName) {
        return stepLog(stepName)
                .$("[data-test='collapse-button'][title='Expand']");
    }

    private SelenideElement logMessage(String command) {
        return $x("//div[@data-test='log-message-text' and contains(normalize-space(.), '%s')]"
                .formatted(command));
    }

    @Override
    public String url() {
        return "";
    }

    public RunBuildPage runBuildFromProject() {
        click(runBuild);
        return this;
    }

    public RunBuildPage checkBuildStatus(String status) {
        elementShouldHaveText(buildStatus, status);
        return this;
    }

    public RunBuildPage checkSuccessBuildIcon() {
        elementShouldBeVisible(successStatus);
        return this;
    }

    public RunBuildPage interruptBuildRun() {
        click(stopBuildButton);
        return this;
    }

    public RunBuildPage enterStopComment(String comment) {
        setValue(stopBuildComment, comment);
        return this;
    }

    public RunBuildPage submitStopBuild() {
        click(submitStopBuildButton);
        return this;
    }

    public RunBuildPage openBuildDetailsByBuildNumber(int buildNumber) {
        click(buildNumberLink(buildNumber));
        return this;
    }

    public RunBuildPage openFirstBuildDetails() {
        click(firstBuildNumberLink());
        return this;
    }

    private SelenideElement firstBuildNumberLink() {
        return buildNumberLink(1);
    }

    public RunBuildPage openBuildLog() {
        click(buildLogTab);
        return this;
    }

    public RunBuildPage expandBuildLog(String stepName) {
        click(expandStepButton(stepName));
        return this;
    }

    public RunBuildPage shouldContainCommand(String command) {
        elementShouldBeVisible(logMessage(command));
        return this;
    }

    public RunBuildPage shouldBeCanceled() {
        elementShouldBeVisible(canceledIcon);
        return this;
    }

    public RunBuildPage clickOnBuildTableRow() {
        click(buildRow);
        return this;
    }

    public RunBuildPage shouldHaveMessage(String message) {
        String detailsId = buildRow.getAttribute("aria-controls");

        $("#" + detailsId)
                .shouldBe(visible)
                .shouldHave(text(message));

        return this;
    }

    public RunBuildPage runBuild() {
        click(runBuildButton);
        return this;
    }
}
