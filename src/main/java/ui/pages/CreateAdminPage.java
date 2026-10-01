package ui.pages;


import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;

import java.time.Duration;

import static com.codeborne.selenide.Selenide.$;

public class CreateAdminPage extends BasePage<CreateAdminPage> {

    // TeamCity initializes the data directory for minutes before this page appears
    private static final Duration PAGE_TIMEOUT = Duration.ofMinutes(5);

    private final SelenideElement username =
            $("#input_teamcityUsername");

    private final SelenideElement password =
            $("#password1");

    private final SelenideElement confirmPassword =
            $("#retypedPassword");

    private final SelenideElement createAccountButton =
            $("input[type='submit'][value='Create Account']");

    public CreateAdminPage shouldBeOpened() {
        $("#header")
                .shouldHave(
                        Condition.text("Create Administrator Account"),
                        PAGE_TIMEOUT
                );

        return this;
    }

    public void createAccount(
            String adminUsername,
            String adminPassword
    ) {
        username
                .shouldBe(Condition.visible)
                .setValue(adminUsername);

        password
                .shouldBe(Condition.visible)
                .setValue(adminPassword);

        confirmPassword
                .shouldBe(Condition.visible)
                .setValue(adminPassword);

        createAccountButton
                .shouldBe(Condition.visible)
                .click();
    }

    @Override
    public String url() {
        return "";
    }
}
