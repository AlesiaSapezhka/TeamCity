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

/**
 * Selenide 7 only knows chrome/firefox/edge/safari/ie, so Selenoid sessions
 * are requested directly to keep browsers such as Opera usable.
 */
public class SelenoidWebDriverProvider implements WebDriverProvider {

    @Override
    public WebDriver createDriver(Capabilities capabilities) {

        MutableCapabilities caps = new MutableCapabilities(capabilities);

        caps.setCapability("browserName", Config.getProperty("browser"));
        caps.setCapability("browserVersion", Config.getProperty("browserVersion"));
        caps.setCapability("selenoid:options", Map.of(
                "enableVNC", true,
                "enableLog", true,
                "sessionTimeout", "5m"
        ));

        return new RemoteWebDriver(remoteUrl(), caps);
    }

    private static URL remoteUrl() {
        try {
            return URI.create(Config.getProperty("uiRemote")).toURL();
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Invalid uiRemote", e);
        }
    }
}
