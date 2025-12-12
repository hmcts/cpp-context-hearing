package uk.gov.moj.cpp;

import java.util.Optional;

public enum JudicialRoleTypeEnum {
  DISTRICT_JUDGE("DISTRICT_JUDGE"),

  RECORDER("RECORDER"),

  CIRCUIT_JUDGE("CIRCUIT_JUDGE"),

  MAGISTRATE("MAGISTRATE");

  private final String value;

  JudicialRoleTypeEnum(String value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return value;
  }

  public static Optional<JudicialRoleTypeEnum> valueFor(final String value) {
    if(DISTRICT_JUDGE.value.equals(value)) {
      return Optional.of(DISTRICT_JUDGE);
    }
    if(RECORDER.value.equals(value)) {
      return Optional.of(RECORDER);
    }
    if(CIRCUIT_JUDGE.value.equals(value)) {
      return Optional.of(CIRCUIT_JUDGE);
    }
    if(MAGISTRATE.value.equals(value)) {
      return Optional.of(MAGISTRATE);
    }
    return Optional.empty();
  }
}
