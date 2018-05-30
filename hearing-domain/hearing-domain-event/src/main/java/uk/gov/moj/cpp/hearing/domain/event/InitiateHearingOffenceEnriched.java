package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

@Event("hearing.initiate-hearing-offence-enriched")
public class InitiateHearingOffenceEnriched implements Serializable {

    private static final long serialVersionUID = 1L;
    private UUID offenceId;
    private UUID caseId;
    private UUID defendantId;
    private UUID hearingId;
    private UUID originHearingId;
    private LocalDate pleaDate;
    private String value;

    public InitiateHearingOffenceEnriched(UUID offenceId, UUID caseId, UUID defendantId, UUID hearingId, UUID originHearingId, LocalDate pleaDate, String value) {
        this.offenceId = offenceId;
        this.caseId = caseId;
        this.defendantId = defendantId;
        this.hearingId = hearingId;
        this.originHearingId = originHearingId;
        this.pleaDate = pleaDate;
        this.value = value;
    }

    public UUID getOffenceId() {
        return offenceId;
    }

    public UUID getCaseId() {
        return caseId;
    }

    public UUID getDefendantId() {
        return defendantId;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public UUID getOriginHearingId() {
        return originHearingId;
    }

    public LocalDate getPleaDate() {
        return pleaDate;
    }

    public String getValue() {
        return value;
    }
}
