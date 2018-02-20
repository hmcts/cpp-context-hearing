package uk.gov.moj.cpp.hearing.persist.entity;

import java.io.Serializable;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "hearing_outcome")
public class HearingOutcome implements Serializable {

    @Column(name = "offence_id")
    private UUID offenceId;

    @Column(name = "hearing_id")
    private UUID hearingId;

    @Column(name = "defendant_id")
    private UUID defendantId;

    @Id
    private UUID id;

    @Column(name = "draft_result")
    private String draftResult;

    public HearingOutcome() {
        //For JPA
    }

    public HearingOutcome(final UUID offenceId,
                          final UUID hearingId,
                          final UUID defendantId,
                          final UUID id,
                          final String draftResult) {
        this.offenceId = offenceId;
        this.hearingId = hearingId;
        this.defendantId = defendantId;
        this.id = id;
        this.draftResult = draftResult;
    }

    public UUID getOffenceId() {
        return offenceId;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public UUID getDefendantId() {
        return defendantId;
    }

    public UUID getId() {
        return id;
    }

    public String getDraftResult() {
        return draftResult;
    }
}
