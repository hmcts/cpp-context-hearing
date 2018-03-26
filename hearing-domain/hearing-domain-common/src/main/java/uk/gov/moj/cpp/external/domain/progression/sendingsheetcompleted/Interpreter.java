package uk.gov.moj.cpp.external.domain.progression.sendingsheetcompleted;

public class Interpreter {
  private String language;

  private Boolean needed;

  public Interpreter() {
  }

  public Interpreter(final String language, final Boolean needed) {
    this.language = language;
    this.needed = needed;
  }

  public String getLanguage() {
    return language;
  }

  public Boolean getNeeded() {
    return needed;
  }

  public static Builder interpreter() {
    return new Interpreter.Builder();
  }

  public static class Builder {
    private String language;

    private Boolean needed;

    public Builder withLanguage(final String language) {
      this.language = language;
      return this;
    }

    public Builder withNeeded(final Boolean needed) {
      this.needed = needed;
      return this;
    }

    public Interpreter build() {
      return new Interpreter(language, needed);
    }
  }
}
