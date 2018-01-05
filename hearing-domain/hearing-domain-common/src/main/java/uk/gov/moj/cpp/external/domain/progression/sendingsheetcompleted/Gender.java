package uk.gov.moj.cpp.external.domain.progression.sendingsheetcompleted;

import java.util.Optional;

public enum Gender {
  NOT_SPECIFIED("Not Specified"),

  MALE("Male"),

  FEMALE("Female");

  private final String value;

  Gender(String value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return value;
  }

  public static Optional<Gender> valueFor(final String value) {
    if(NOT_SPECIFIED.value.equals(value)) { return Optional.of(NOT_SPECIFIED); };
    if(MALE.value.equals(value)) { return Optional.of(MALE); };
    if(FEMALE.value.equals(value)) { return Optional.of(FEMALE); };
    return Optional.empty();
  }
}
