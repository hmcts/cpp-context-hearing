package uk.gov.moj.cpp.hearing.command.enforcement;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class FineRequest {

    private final UUID defendantId;
    private final String firstName;
    private final String lastName;
    private final String dateOfBirth;
    private final String nationalInsuranceNumber;
    private final String legalEntityDefendantName;
    private final UUID caseId;
    private final String ouCode;
    private final LocalDate dateOfHearing;
    private final ZonedDateTime timeOfHearing;

    @JsonCreator
    public FineRequest(@JsonProperty("defendantId") final UUID defendantId,
                       @JsonProperty("caseId") final UUID caseId,
                       @JsonProperty("ouCode") final String ouCode,
                       @JsonProperty("dateOfHearing") final LocalDate dateOfHearing,
                       @JsonProperty("timeOfHearing") final ZonedDateTime timeOfHearing,
                       @JsonProperty("firstName") final String firstName,
                       @JsonProperty("lastName") final String lastName,
                       @JsonProperty("dateOfBirth") final String dateOfBirth,
                       @JsonProperty("nationalInsuranceNumber") final String nationalInsuranceNumber,
                       @JsonProperty("legalEntityDefendantName") final String legalEntityDefendantName) {

        this.defendantId = defendantId;
        this.caseId = caseId;
        this.ouCode = ouCode;
        this.dateOfHearing = dateOfHearing;
        this.timeOfHearing = timeOfHearing;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.nationalInsuranceNumber = nationalInsuranceNumber;
        this.legalEntityDefendantName = legalEntityDefendantName;
    }

    private FineRequest(final Builder builder) {
        defendantId = builder.defendantId;
        firstName = builder.firstName;
        lastName = builder.lastName;
        dateOfBirth = builder.dateOfBirth;
        nationalInsuranceNumber = builder.nationalInsuranceNumber;
        legalEntityDefendantName = builder.legalEntityDefendantName;
        caseId = builder.caseId;
        ouCode = builder.ouCode;
        dateOfHearing = builder.dateOfHearing;
        timeOfHearing = builder.timeOfHearing;
    }

    public static Builder fineRequest() {
        return new Builder();
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static Builder newBuilder(final FineRequest copy) {
        Builder builder = new Builder();
        builder.defendantId = copy.getDefendantId();
        builder.firstName = copy.getFirstName();
        builder.lastName = copy.getLastName();
        builder.dateOfBirth = copy.getDateOfBirth();
        builder.nationalInsuranceNumber = copy.getNationalInsuranceNumber();
        builder.legalEntityDefendantName = copy.getLegalEntityDefendantName();
        builder.caseId = copy.getCaseId();
        builder.ouCode = copy.getOuCode();
        builder.ouCode = copy.getOuCode();
        builder.dateOfHearing = copy.getDateOfHearing();
        builder.timeOfHearing = copy.getTimeOfHearing();
        return builder;
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

    public String getLegalEntityDefendantName() {
        return legalEntityDefendantName;
    }

    public UUID getCaseId() {
        return caseId;
    }

    public String getOuCode() {
        return ouCode;
    }

    public LocalDate getDateOfHearing() {
        return dateOfHearing;
    }

    public ZonedDateTime getTimeOfHearing() {
        return timeOfHearing;
    }


    public static final class Builder {
        private UUID defendantId;
        private String firstName;
        private String lastName;
        private String dateOfBirth;
        private String nationalInsuranceNumber;
        private String legalEntityDefendantName;
        private UUID caseId;
        private String ouCode;
        private LocalDate dateOfHearing;
        private ZonedDateTime timeOfHearing;

        private Builder() {
        }

        public Builder withDefendantId(final UUID defendantId) {
            this.defendantId = defendantId;
            return this;
        }

        public Builder withFirstName(final String firstName) {
            this.firstName = firstName;
            return this;
        }

        public Builder withLastName(final String lastName) {
            this.lastName = lastName;
            return this;
        }

        public Builder withDateOfBirth(final String dateOfBirth) {
            this.dateOfBirth = dateOfBirth;
            return this;
        }

        public Builder withNationalInsuranceNumber(final String nationalInsuranceNumber) {
            this.nationalInsuranceNumber = nationalInsuranceNumber;
            return this;
        }

        public Builder withLegalEntityDefendantName(final String legalEntityDefendantName) {
            this.legalEntityDefendantName = legalEntityDefendantName;
            return this;
        }

        public Builder withCaseId(final UUID caseId) {
            this.caseId = caseId;
            return this;
        }

        public Builder withOuCode(final String ouCode) {
            this.ouCode = ouCode;
            return this;
        }

        public Builder withDateOfHearing(final LocalDate dateOfHearing) {
            this.dateOfHearing = dateOfHearing;
            return this;
        }

        public Builder withTimeOfHearing(final ZonedDateTime timeOfHearing) {
            this.timeOfHearing = timeOfHearing;
            return this;
        }

        public FineRequest build() {
            return new FineRequest(this);
        }
    }
}