package integration;

import com.codeborne.selenide.WebDriverRunner;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.grid.Main;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UncheckedIOException;

import static com.codeborne.selenide.Configuration.timeout;
import static com.codeborne.selenide.Selenide.closeWebDriver;
import static org.openqa.selenium.net.PortProber.findFreePort;

abstract class AbstractGridTest extends IntegrationTest {
  private final Logger log = LoggerFactory.getLogger(getClass());

  int hubPort;

  @BeforeEach
  final void setUpGrid() {
    if (WebDriverRunner.isChrome()) {
      WebDriverManager.chromedriver().setup();
    }
    if (WebDriverRunner.isFirefox()) {
      WebDriverManager.firefoxdriver().setup();
    }
    if (WebDriverRunner.isEdge()) {
      WebDriverManager.edgedriver().setup();
    }
    closeWebDriver();

    for (int tries = 0; tries < 3; tries++) {
      int port = findFreePort();
      try {
        Main.main(new String[]{"standalone", "--port", String.valueOf(port)});
        hubPort = port;
        break;
      }
      catch (UncheckedIOException portAlreadyUsed) {
        log.warn("Failed to start Selenium Grid on port {}", port, portAlreadyUsed);
      }
    }

    timeout = 4000;
  }

  @AfterEach
  final void tearDownGrid() {
    closeWebDriver();
  }
}
