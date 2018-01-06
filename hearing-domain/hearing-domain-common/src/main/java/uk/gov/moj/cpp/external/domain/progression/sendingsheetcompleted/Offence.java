package uk.gov.moj.cpp.external.domain.progression.sendingsheetcompleted;

import java.time.LocalDate;
import java.util.UUID;

public class Offence {
  private String category;

  private LocalDate convictionDate;

  private String description;

  private LocalDate endDate;

  private UUID id;

  private IndicatedPlea indicatedPlea;

  private String offenceCode;

  private Plea plea;

  private String reason;

  private String section;

  private LocalDate startDate;

  private void setCategory(final String category) {
    this.category = category;
  }

  private void setConvictionDate(final LocalDate convictionDate) {
    this.convictionDate = convictionDate;
  }

  private void setDescription(final String description) {
    this.description = description;
  }

  private void setEndDate(final LocalDate endDate) {
    this.endDate = endDate;
  }

  private void setId(final UUID id) {
    this.id = id;
  }

  private void setIndicatedPlea(final IndicatedPlea indicatedPlea) {
    this.indicatedPlea = indicatedPlea;
  }

  private void setOffenceCode(final String offenceCode) {
    this.offenceCode = offenceCode;
  }

  private void setPlea(final Plea plea) {
    this.plea = plea;
  }

  private void setReason(final String reason) {
    this.reason = reason;
  }

  private void setSection(final String section) {
    this.section = section;
  }

  private void setStartDate(final LocalDate startDate) {
    this.startDate = startDate;
  }

  private void setWording(final String wording) {
    this.wording = wording;
  }

  private String wording;

  public String getCategory() {
    return category;
  }

  public LocalDate getConvictionDate() {
    return convictionDate;
  }

  public String getDescription() {
    return description;
  }

  public LocalDate getEndDate() {
    return endDate;
  }

  public UUID getId() {
    return id;
  }

  public IndicatedPlea getIndicatedPlea() {
    return indicatedPlea;
  }

  public String getOffenceCode() {
    return offenceCode;
  }

  public Plea getPlea() {
    return plea;
  }

  public String getReason() {
    return reason;
  }

  public String getSection() {
    return section;
  }

  public LocalDate getStartDate() {
    return startDate;
  }

  public String getWording() {
    return wording;
  }

  public static Builder offences() {
    return new Offence.Builder();
  }

  public static class Builder {
    private String category;

    private LocalDate convictionDate;

    private String description;

    private LocalDate endDate;

    private UUID id;

    private IndicatedPlea indicatedPlea;

    private String offenceCode;

    private Plea plea;

    private String reason;

    private String section;

    private LocalDate startDate;

    private String wording;

    public Builder withCategory(final String category) {
      this.category = category;
      return this;
    }

    public Builder withConvictionDate(final LocalDate convictionDate) {
      this.convictionDate = convictionDate;
      return this;
    }

    public Builder withDescription(final String description) {
      this.description = description;
      return this;
    }

    public Builder withEndDate(final LocalDate endDate) {
      this.endDate = endDate;
      return this;
    }

    public Builder withId(final UUID id) {
      this.id = id;
      return this;
    }

    public Builder withIndicatedPlea(final IndicatedPlea indicatedPlea) {
      this.indicatedPlea = indicatedPlea;
      return this;
    }

    public Builder withOffenceCode(final String offenceCode) {
      this.offenceCode = offenceCode;
      return this;
    }

    public Builder withPlea(final Plea plea) {
      this.plea = plea;
      return this;
    }

    public Builder withReason(final String reason) {
      this.reason = reason;
      return this;
    }

    public Builder withSection(final String section) {
      this.section = section;
      return this;
    }

    public Builder withStartDate(final LocalDate startDate) {
      this.startDate = startDate;
      return this;
    }

    public Builder withWording(final String wording) {
      this.wording = wording;
      return this;
    }

    public Offence build() {
      final Offence offence = new Offence();

      offence.setCategory(category);
      offence.setConvictionDate(convictionDate);
      offence.setDescription(description);
      offence.setEndDate(endDate);
      offence.setId(id);
      offence.setIndicatedPlea(indicatedPlea);
      offence.setOffenceCode(offenceCode);
      offence.setPlea(plea);
      offence.setReason(reason);
      offence.setSection(section);
      offence.setStartDate(startDate);
      offence.setWording(wording);

      return offence;
    }
  }
}
