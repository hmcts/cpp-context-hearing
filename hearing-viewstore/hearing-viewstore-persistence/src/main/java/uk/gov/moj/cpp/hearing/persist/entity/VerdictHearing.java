package uk.gov.moj.cpp.hearing.persist.entity;

import java.io.Serializable;
import java.util.UUID;

import javax.persistence.Column;
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

    @Column(name = "value")
    private String value;

    public VerdictHearing() {
        //For JPA
    }

    public VerdictHearing(final UUID verdictId, final UUID hearingId, final UUID caseId, final UUID personId, final UUID defendantId, final UUID offenceId, final String value) {
        this.verdictId = verdictId;
        this.hearingId = hearingId;
        this.caseId = caseId;
        this.personId = personId;
        this.defendantId = defendantId;
        this.offenceId = offenceId;
        this.value = value;
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

    public String getValue() {
        return value;
    }

}
