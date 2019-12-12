package uk.gov.moj.cpp.hearing.event.order;

import java.io.Serializable;

public class Prompts implements Serializable {
  private static final long serialVersionUID = 1870765747443534132L;

  private final String label;

  private final String value;

  private final String welshLabel;

  private final String welshValue;

  public Prompts(final String label, final String value, final String welshLabel, final String welshValue) {
    this.label = label;
    this.value = value;
    this.welshLabel = welshLabel;
    this.welshValue = welshValue;
  }

  public String getLabel() {
    return label;
  }

  public String getValue() {
    return value;
  }

  public String getWelshLabel() {
    return welshLabel;
  }

  public String getWelshValue() {
    return welshValue;
  }

  public static Builder prompts() {
    return new Prompts.Builder();
  }

  public static class Builder {
    private String label;

    private String value;

    private String welshLabel;

    private String welshValue;

    public Builder withLabel(final String label) {
      this.label = label;
      return this;
    }

    public Builder withValue(final String value) {
      this.value = value;
      return this;
    }

    public Builder withWelshLabel(final String welshLabel) {
      this.welshLabel = welshLabel;
      return this;
    }

    public Builder withWelshValue(final String welshValue) {
      this.welshValue = welshValue;
      return this;
    }

    public Prompts build() {
      return new Prompts(label, value, welshLabel, welshValue);
    }
  }
}
