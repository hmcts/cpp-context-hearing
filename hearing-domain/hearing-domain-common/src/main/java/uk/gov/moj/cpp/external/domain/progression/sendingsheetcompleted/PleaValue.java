package uk.gov.moj.cpp.external.domain.progression.sendingsheetcompleted;

import java.util.Optional;

public enum PleaValue {
  GUILTY("GUILTY");

  private final String value;

  PleaValue(String value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return value;
  }

  public static Optional<PleaValue> valueFor(final String value) {
    if(GUILTY.value.equals(value)) { return Optional.of(GUILTY); };
    return Optional.empty();
  }
}
