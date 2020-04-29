package uk.gov.moj.cpp.hearing.domain;

import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DefendantDetail {

    private final UUID defendantId;
    private final String firstName;
    private final String lastName;
    private final String dateOfBirth;
    private final String nationalInsuranceNumber;
    private final String legalEntityOrganizationName;

    @JsonCreator
    public DefendantDetail(@JsonProperty("defendantId") final UUID defendantId,
                           @JsonProperty("firstName") final String firstName,
                           @JsonProperty("lastName") final String lastName,
                           @JsonProperty("dateOfBirth") final String dateOfBirth,
                           @JsonProperty("nationalInsuranceNumber") final String nationalInsuranceNumber,
                           @JsonProperty("legalEntityOrganizationName") final String legalEntityOrganizationName) {

        this.defendantId = defendantId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.nationalInsuranceNumber = nationalInsuranceNumber;
        this.legalEntityOrganizationName = legalEntityOrganizationName;
    }

    public static DefendantDetail.Builder defendantDetail() {
        return new Builder();
    }

    public UUID getDefendantId() {
        return defendantId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public String getNationalInsuranceNumber() {
        return nationalInsuranceNumber;
    }

    public String getLegalEntityOrganizationName() {
        return legalEntityOrganizationName;
    }

    public static class Builder {
        private UUID defendantId;
        private String firstName;
        private String lastName;
        private String dateOfBirth;
        private String nationalInsuranceNumber;
        private String legalEntityOrganizationName;

        public DefendantDetail.Builder withDefendantId(final UUID defendantId) {
            this.defendantId = defendantId;
            return this;
        }

        public DefendantDetail.Builder withLastName(final String lastName) {
            if (Objects.nonNull(lastName)) {
                this.lastName = lastName;
            }
            return this;
        }

        public DefendantDetail.Builder withFirstName(final String firstName) {
            if (Objects.nonNull(firstName)) {
                this.firstName = firstName;
            }
            return this;
        }

        public DefendantDetail.Builder withDateOfBirth(final String dateOfBirth) {
            if (Objects.nonNull(dateOfBirth)) {
                this.dateOfBirth = dateOfBirth;
            }
            return this;
        }

        public DefendantDetail.Builder withNationalInsuranceNumber(final String nationalInsuranceNumber) {
            if (Objects.nonNull(nationalInsuranceNumber)) {
                this.nationalInsuranceNumber = nationalInsuranceNumber;
            }
            return this;
        }

        public DefendantDetail.Builder withLegalEntityOrganizationName(final String legalEntityOrganizationName) {
            if (Objects.nonNull(legalEntityOrganizationName)) {
                this.legalEntityOrganizationName = legalEntityOrganizationName;
            }
            return this;
        }

        public DefendantDetail build() {
            return new DefendantDetail(defendantId, firstName, lastName, dateOfBirth, nationalInsuranceNumber, legalEntityOrganizationName);
        }
    }

}