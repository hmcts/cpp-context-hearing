package uk.gov.moj.cpp.hearing.command.enforcement;

import java.util.Optional;

public enum ExportStatus {
  ENFORCEMENT_EXPORT_FAILED("ENFORCEMENT_EXPORT_FAILED"),

  ENFORCEMENT_ACKNOWLEDGED("ENFORCEMENT_ACKNOWLEDGED"),

  ENFORCEMENT_ACKNOWLEDGEMENT_FAILED("ENFORCEMENT_ACKNOWLEDGEMENT_FAILED"),

  ENFORCEMENT_EXPORTED("ENFORCEMENT_EXPORTED"),

  ENFORCEMENT_REQUEST_SELECTED("ENFORCEMENT_REQUEST_SELECTED"),

  AWAITING_EXPORT_TO_ENFORCEMENT("AWAITING_EXPORT_TO_ENFORCEMENT");

  private final String value;

  ExportStatus(String value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return value;
  }

  public static Optional<ExportStatus> valueFor(final String value) {
    if(ENFORCEMENT_EXPORT_FAILED.value.equals(value)) { return Optional.of(ENFORCEMENT_EXPORT_FAILED); };
    if(ENFORCEMENT_ACKNOWLEDGED.value.equals(value)) { return Optional.of(ENFORCEMENT_ACKNOWLEDGED); };
    if(ENFORCEMENT_ACKNOWLEDGEMENT_FAILED.value.equals(value)) { return Optional.of(ENFORCEMENT_ACKNOWLEDGEMENT_FAILED); };
    if(ENFORCEMENT_EXPORTED.value.equals(value)) { return Optional.of(ENFORCEMENT_EXPORTED); };
    if(ENFORCEMENT_REQUEST_SELECTED.value.equals(value)) { return Optional.of(ENFORCEMENT_REQUEST_SELECTED); };
    if(AWAITING_EXPORT_TO_ENFORCEMENT.value.equals(value)) { return Optional.of(AWAITING_EXPORT_TO_ENFORCEMENT); };
    return Optional.empty();
  }
}
