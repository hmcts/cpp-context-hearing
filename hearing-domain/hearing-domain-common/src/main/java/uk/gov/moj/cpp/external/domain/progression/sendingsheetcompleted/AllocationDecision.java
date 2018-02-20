package uk.gov.moj.cpp.external.domain.progression.sendingsheetcompleted;

import java.util.Optional;

public enum AllocationDecision {
  ELECT_TRIAL("ELECT_TRIAL"),

  COURT_DECLINED("COURT_DECLINED"),

  INDICTABLE_LINKED("INDICTABLE_LINKED");

  private final String value;

  AllocationDecision(String value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return value;
  }

  public static Optional<AllocationDecision> valueFor(final String value) {
    if(ELECT_TRIAL.value.equals(value)) { return Optional.of(ELECT_TRIAL); };
    if(COURT_DECLINED.value.equals(value)) { return Optional.of(COURT_DECLINED); };
    if(INDICTABLE_LINKED.value.equals(value)) { return Optional.of(INDICTABLE_LINKED); };
    return Optional.empty();
  }
}
