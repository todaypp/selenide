package com.codeborne.selenide.ex;

import com.codeborne.selenide.impl.CollectionSource;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import static java.lang.System.lineSeparator;

@ParametersAreNonnullByDefault
public class MatcherError extends UIAssertionError {

  public MatcherError(@Nullable String explanation,
                      String expected, String actual,
                      CollectionSource collection,
                      @Nullable Exception lastError,
                      long timeoutMs) {
    super(
      "Collection matcher error" +
        lineSeparator() + "Expected: " + expected +
        (explanation == null ? "" : lineSeparator() + "Because: " + explanation) +
        lineSeparator() + "Collection: " + collection.description() +
        lineSeparator() + "Elements: " + actual,
      expected, "Elements: " + actual,
      lastError
    );
    super.timeoutMs = timeoutMs;
  }
}
