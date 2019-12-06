package uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit;

import java.io.Serializable;

public class Defendant implements Serializable {
  private static final long serialVersionUID = 309017867635555909L;

  private String firstName;

  private String lastName;

  private String middleName;

  public Defendant(final String firstName, final String lastName, final String middleName) {
    this.firstName = firstName;
    this.lastName = lastName;
    this.middleName = middleName;
  }

  public String getFirstName() {
    return firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public String getMiddleName() {
    return middleName;
  }

  public static Builder defendant() {
    return new Defendant.Builder();
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()){
      return false;
    }
    final Defendant that = (Defendant) obj;

    return java.util.Objects.equals(this.firstName, that.firstName) &&
    java.util.Objects.equals(this.lastName, that.lastName) &&
    java.util.Objects.equals(this.middleName, that.middleName);
  }

  @Override
  public int hashCode() {
    return java.util.Objects.hash(firstName, lastName, middleName);}

  @Override
  public String toString() {
    return "Defendant{" +
    	"firstName='" + firstName + "'," +
    	"lastName='" + lastName + "'," +
    	"middleName='" + middleName + "'" +
    "}";
  }

  public Defendant setFirstName(String firstName) {
    this.firstName = firstName;
    return this;
  }

  public Defendant setLastName(String lastName) {
    this.lastName = lastName;
    return this;
  }

  public Defendant setMiddleName(String middleName) {
    this.middleName = middleName;
    return this;
  }

  public static class Builder {
    private String firstName;

    private String lastName;

    private String middleName;

    public Builder withFirstName(final String firstName) {
      this.firstName = firstName;
      return this;
    }

    public Builder withLastName(final String lastName) {
      this.lastName = lastName;
      return this;
    }

    public Builder withMiddleName(final String middleName) {
      this.middleName = middleName;
      return this;
    }

    public Defendant build() {
      return new Defendant(firstName, lastName, middleName);
    }
  }
}
