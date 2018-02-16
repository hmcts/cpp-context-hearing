package uk.gov.moj.cpp.hearing.persist.entity;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "plea_hearing")
@SuppressWarnings("squid:S00107")
public class PleaHearing implements Serializable {

    @Id
    @Column(name = "plea_id")
    private UUID pleaId;

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

    @Column(name = "plea_date")
    private LocalDate pleaDate;

    @Column(name = "value")
    private String value;

    public PleaHearing() {
        //For JPA
    }

    public PleaHearing(final UUID pleaId,
                       final UUID hearingId,
                       final UUID caseId,
                       final UUID defendantId,
                       final UUID offenceId,
                       final LocalDate pleaDate,
                       final String value,
                       final UUID personId) {
        this.pleaId = pleaId;
        this.hearingId = hearingId;
        this.caseId = caseId;
        this.defendantId = defendantId;
        this.offenceId = offenceId;
        this.pleaDate = pleaDate;
        this.value = value;
        this.personId = personId;
    }

    public UUID getPleaId() {
        return pleaId;
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

    public LocalDate getPleaDate() {
        return pleaDate;
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
