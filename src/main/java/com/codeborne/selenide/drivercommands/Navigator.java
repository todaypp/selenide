package com.codeborne.selenide.drivercommands;

import com.codeborne.selenide.AuthenticationType;
import com.codeborne.selenide.BasicAuthCredentials;
import com.codeborne.selenide.Config;
import com.codeborne.selenide.Credentials;
import com.codeborne.selenide.Driver;
import com.codeborne.selenide.SelenideDriver;
import com.codeborne.selenide.logevents.SelenideLogger;
import com.codeborne.selenide.proxy.AuthenticationFilter;
import com.codeborne.selenide.proxy.SelenideProxyServer;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.net.URL;
import java.util.regex.Pattern;

import static com.codeborne.selenide.AuthenticationType.BASIC;
import static com.codeborne.selenide.FileDownloadMode.PROXY;
import static java.util.regex.Pattern.DOTALL;

@ParametersAreNonnullByDefault
public class Navigator {
  private static final Pattern ABSOLUTE_URL_REGEX = Pattern.compile("^[a-zA-Z-]+:.*", DOTALL);

  private final BasicAuthUrl basicAuthUrl = new BasicAuthUrl();

  public void open(SelenideDriver driver, String relativeOrAbsoluteUrl) {
    navigateTo(driver, relativeOrAbsoluteUrl, null, null);
  }

  public void open(SelenideDriver driver, URL url) {
    navigateTo(driver, url.toExternalForm(), null, null);
  }

  public void open(SelenideDriver driver, String relativeOrAbsoluteUrl, String domain, String login, String password) {
    navigateTo(driver, relativeOrAbsoluteUrl, BASIC, new BasicAuthCredentials(domain, login, password));
  }

  public void open(SelenideDriver driver, URL url, String domain, String login, String password) {
    navigateTo(driver, url.toExternalForm(), BASIC, new BasicAuthCredentials(domain, login, password));
  }

  public void open(SelenideDriver driver, String relativeOrAbsoluteUrl,
                   AuthenticationType authenticationType, Credentials credentials) {
    navigateTo(driver, relativeOrAbsoluteUrl, authenticationType, credentials);
  }

  private AuthenticationFilter basicAuthRequestFilter(SelenideProxyServer selenideProxy) {
    return selenideProxy.requestFilter("authentication");
  }

  @Nonnull
  String absoluteUrl(Config config, String relativeOrAbsoluteUrl) {
    return isAbsoluteUrl(relativeOrAbsoluteUrl) ? relativeOrAbsoluteUrl : config.baseUrl() + relativeOrAbsoluteUrl;
  }

  private void navigateTo(SelenideDriver driver, String relativeOrAbsoluteUrl, @Nullable AuthenticationType authenticationType, @Nullable Credentials credentials) {
    checkThatProxyIsEnabled(driver.config());

    String absoluteUrl = absoluteUrl(driver.config(), relativeOrAbsoluteUrl);
    String url = appendBasicAuthIfNeeded(driver.config(), absoluteUrl, authenticationType, credentials);

    SelenideLogger.run("open", url, () -> {
      try {
        WebDriver webDriver = driver.getAndCheckWebDriver();
        beforeNavigateTo(driver.config(), driver.getProxy(), authenticationType, credentials);
        webDriver.navigate().to(url);
      }
      catch (WebDriverException e) {
        e.addInfo("selenide.url", url);
        e.addInfo("selenide.baseUrl", driver.config().baseUrl());
        if (driver.config().remote() != null) {
          e.addInfo("selenide.remote", driver.config().remote());
        }
        throw e;
      }
    });
  }

  public void open(SelenideDriver driver) {
    checkThatProxyIsEnabled(driver.config());
    SelenideLogger.run("open", "", driver::getAndCheckWebDriver);
  }

  private void checkThatProxyIsEnabled(Config config) {
    if (!config.proxyEnabled() && config.fileDownload() == PROXY) {
      throw new IllegalStateException("config.proxyEnabled == false but config.fileDownload == PROXY. " +
        "You need to enable proxy server automatically.");
    }
  }

  private void checkThatProxyIsStarted(@Nullable SelenideProxyServer selenideProxy) {
    if (selenideProxy == null) {
      throw new IllegalStateException("config.proxyEnabled == true but proxy server is not created. " +
        "You need to call `setWebDriver(webDriver, selenideProxy)` instead of `setWebDriver(webDriver)` if you need to use proxy.");
    }
    if (!selenideProxy.isStarted()) {
      throw new IllegalStateException("config.proxyEnabled == true but proxy server is not started.");
    }
  }

  private void beforeNavigateTo(Config config, @Nullable SelenideProxyServer selenideProxy,
                                @Nullable AuthenticationType authenticationType, @Nullable Credentials credentials) {
    if (config.proxyEnabled()) {
      checkThatProxyIsStarted(selenideProxy);
      beforeNavigateToWithProxy(selenideProxy, authenticationType, credentials);
    }
    else {
      beforeNavigateToWithoutProxy(authenticationType, credentials);
    }
  }

  private void beforeNavigateToWithProxy(SelenideProxyServer selenideProxy,
                                         @Nullable AuthenticationType authenticationType, @Nullable Credentials credentials) {
    if (authenticationType != null && credentials != null) {
      basicAuthRequestFilter(selenideProxy).setAuthentication(authenticationType, credentials);
    }
    else {
      basicAuthRequestFilter(selenideProxy).removeAuthentication();
    }
  }

  private void beforeNavigateToWithoutProxy(@Nullable AuthenticationType authenticationType, @Nullable Credentials credentials) {
    if (credentials != null && authenticationType != BASIC) {
      throw new UnsupportedOperationException("Cannot use " + authenticationType + " authentication without proxy server");
    }
  }

  private String appendBasicAuthIfNeeded(Config config, String url, @Nullable AuthenticationType authType, @Nullable Credentials credentials) {
    return passBasicAuthThroughUrl(config, authType, credentials)
      ? basicAuthUrl.appendBasicAuthToURL(url, ((BasicAuthCredentials) credentials))
      : url;
  }

  private boolean passBasicAuthThroughUrl(Config config, @Nullable AuthenticationType authenticationType, @Nullable Credentials credentials) {
    return authenticationType == BASIC && credentials != null && !config.proxyEnabled();
  }

  boolean isAbsoluteUrl(String relativeOrAbsoluteUrl) {
    return ABSOLUTE_URL_REGEX.matcher(relativeOrAbsoluteUrl).matches();
  }

  public void back(Driver driver) {
    SelenideLogger.run("back", "", () -> {
      driver.getWebDriver().navigate().back();
    });
  }

  public void forward(Driver driver) {
    SelenideLogger.run("forward", "", () -> {
      driver.getWebDriver().navigate().forward();
    });
  }

  public void refresh(Driver driver) {
    SelenideLogger.run("refresh", "", () -> {
      driver.getWebDriver().navigate().refresh();
    });
  }
}
