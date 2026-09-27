package ui;

import api.configs.Config;
import com.codeborne.selenide.WebDriverProvider;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Map;

public class SelenoidWebDriverProvider implements WebDriverProvider {

    @Override
    public WebDriver createDriver(Capabilities capabilities) {

        MutableCapabilities remoteCapabilities =
                new MutableCapabilities(capabilities);

        remoteCapabilities.setCapability(
                "browserName",
                Config.getProperty("browser")
        );
        remoteCapabilities.setCapability(
                "browserVersion",
                Config.getProperty("browserVersion")
        );
        remoteCapabilities.setCapability(
                "selenoid:options",
                Map.of(
                        "enableVNC", true,
                        "enableLog", true,
                        "sessionTimeout", "5m"
                )
        );

        return new RemoteWebDriver(remoteUrl(), remoteCapabilities);
    }

    private static URL remoteUrl() {
        try {
            return URI.create(Config.getProperty("uiRemote")).toURL();
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Invalid uiRemote", e);
        }
    }
}