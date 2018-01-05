package uk.gov.moj.cpp.external.domain.progression.sendingsheetcompleted;

import java.time.LocalDate;
import java.util.UUID;

public class Plea {
  private final UUID id;

  private final LocalDate pleaDate;

  private final PleaValue value;

  public Plea(final UUID id, final LocalDate pleaDate, final PleaValue value) {
    this.id = id;
    this.pleaDate = pleaDate;
    this.value = value;
  }

  public UUID getId() {
    return id;
  }

  public LocalDate getPleaDate() {
    return pleaDate;
  }

  public PleaValue getValue() {
    return value;
  }

  public static Builder plea() {
    return new Plea.Builder();
  }

  public static class Builder {
    private UUID id;

    private LocalDate pleaDate;

    private PleaValue pleaValue;

    public Builder withId(final UUID id) {
      this.id = id;
      return this;
    }

    public Builder withPleaDate(final LocalDate pleaDate) {
      this.pleaDate = pleaDate;
      return this;
    }

    public Builder withPleaValue(final PleaValue pleaValue) {
      this.pleaValue = pleaValue;
      return this;
    }

    public Plea build() {
      return new Plea(id, pleaDate, pleaValue);
    }
  }
}
