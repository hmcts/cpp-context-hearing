package uk.gov.moj.cpp.hearing.persist.entity;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "verdict_hearing")
public class VerdictHearing implements Serializable {

    @Id
    @Column(name = "verdict_id")
    private UUID verdictId;

    @Column(name = "hearing_id")
    private UUID hearingId;

    @Column(name = "case_id")
    private UUID caseId;

    @Column(name = "person_id")
    private UUID personId;

    @Column(name = "defendant_id")
    private UUID defendantId;

    @Column(name = "offence_id")
    private UUID offenceId;

    @Column(name = "verdict_date")
    private LocalDate verdictDate;

    @Column(name = "number_of_split_jurors")
    private Integer numberOfSplitJurors;

    @Column(name = "number_of_jurors")
    private Integer numberOfJurors;

    @Column(name = "unanimous")
    private Boolean unanimous;

    @Embedded
    private VerdictValue value;

    public VerdictHearing() {
        //For JPA
    }

    private VerdictHearing(final Builder builder) {
        assert null != builder;
        this.verdictId = builder.verdictId;
        this.hearingId = builder.hearingId;
        this.caseId = builder.caseId;
        this.personId = builder.personId;
        this.defendantId = builder.defendantId;
        this.offenceId = builder.offenceId;
        this.value = builder.value;
        this.verdictDate = builder.verdictDate;
        this.numberOfSplitJurors = builder.numberOfSplitJurors;
        this.numberOfJurors = builder.numberOfJurors;
        this.unanimous = builder.unanimous;
    }

    public UUID getVerdictId() {
        return verdictId;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public UUID getCaseId() {
        return caseId;
    }

    public UUID getPersonId() {
        return personId;
    }

    public UUID getOffenceId() {
        return offenceId;
    }

    public UUID getDefendantId() {
        return defendantId;
    }

    public VerdictValue getValue() {
        return value;
    }

    public LocalDate getVerdictDate() {
        return verdictDate;
    }

    public Integer getNumberOfSplitJurors() {
        return numberOfSplitJurors;
    }

    public Integer getNumberOfJurors() {
        return numberOfJurors;
    }

    public Boolean getUnanimous() {
        return unanimous;
    }
    
    public static final class Builder {

        private UUID verdictId;
        private UUID hearingId;
        private UUID caseId;
        private UUID personId;
        private UUID defendantId;
        private UUID offenceId;
        private VerdictValue value;
        private LocalDate verdictDate;
        private Integer numberOfSplitJurors;
        private Integer numberOfJurors;
        private Boolean unanimous;

        public Builder withVerdictId(UUID verdictId) {
            this.verdictId = verdictId;
            return this;
        }

        public Builder withHearingId(UUID hearingId) {
            this.hearingId = hearingId;
            return this;
        }

        public Builder withCaseId(UUID caseId) {
            this.caseId = caseId;
            return this;
        }

        public Builder withPersonId(UUID personId) {
            this.personId = personId;
            return this;
        }

        public Builder withDefendantId(UUID defendantId) {
            this.defendantId = defendantId;
            return this;
        }

        public Builder withOffenceId(UUID offenceId) {
            this.offenceId = offenceId;
            return this;
        }

        public Builder withValue(final VerdictValue value) {
            this.value = value;
            return this;
        }

        public Builder withVerdictDate(final LocalDate verdictDate) {
            this.verdictDate = verdictDate;
            return this;
        }
        
        public Builder withNumberOfSplitJurors(Integer numberOfSplitJurors) {
            this.numberOfSplitJurors = numberOfSplitJurors;
            return this;
        }

        public Builder withNumberOfJurors(Integer numberOfJurors) {
            this.numberOfJurors = numberOfJurors;
            return this;
        }

        public Builder withUnanimous(Boolean unanimous) {
            this.unanimous = unanimous;
            return this;
        }
        
        public VerdictHearing build() {
            return new VerdictHearing(this);
        }
    }
}
