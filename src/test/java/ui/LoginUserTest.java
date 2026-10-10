package ui;

import common.UserContext;
import common.annotations.CreateAndDeleteUser;
import org.junit.jupiter.api.Test;
import ui.pages.LoginPage;

class LoginUserTest extends BaseUiTest {

    @Test
    @CreateAndDeleteUser
    void userShouldLoginViaUiWithValidData(UserContext user) {
        new LoginPage()
                .open()
                .login(user.username(), user.password())
                .goToMainPage()
                .welcomeMessageShouldBeVisible();
    }

    @Test
    @CreateAndDeleteUser
    void errorMessageShouldBeVisibleIfLoginWithInvalidData(UserContext user) {
        new LoginPage()
                .open()
                .login(user.username(), user.password().toLowerCase())
                .checkErrorMessage();
    }

    @Test
    @CreateAndDeleteUser
    void userShouldLoginViaUiValidData(UserContext user) {
        new LoginPage()
                .open()
                .login(user.username(), user.password())
                .goToMainPage()
                .welcomeMessageShouldBeVisible();
    }
}
