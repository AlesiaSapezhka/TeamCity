package ui;

import api.configs.Config;
import com.codeborne.selenide.WebDriverProvider;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Dimension;
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
        String browserSize = Config.getProperty("browserSize");
        Dimension windowSize = parseBrowserSize(browserSize);
        remoteCapabilities.setCapability(
                "selenoid:options",
                Map.of(
                        "enableVNC", true,
                        "enableLog", true,
                "sessionTimeout", "5m",
                "screenResolution",
                windowSize.getWidth() + "x" + windowSize.getHeight() + "x24"
                )
        );

        WebDriver driver = new RemoteWebDriver(remoteUrl(), remoteCapabilities);
        driver.manage().window().setSize(windowSize);
        return driver;
    }

    private static Dimension parseBrowserSize(String browserSize) {
        String[] dimensions = browserSize.split("x");
        if (dimensions.length != 2) {
            throw new IllegalArgumentException(
                    "browserSize must have the format WIDTHxHEIGHT: " + browserSize
            );
        }
        return new Dimension(
                Integer.parseInt(dimensions[0]),
                Integer.parseInt(dimensions[1])
        );
    }

    private static URL remoteUrl() {
        try {
            return URI.create(Config.getProperty("uiRemote")).toURL();
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Invalid uiRemote", e);
        }
    }
}
