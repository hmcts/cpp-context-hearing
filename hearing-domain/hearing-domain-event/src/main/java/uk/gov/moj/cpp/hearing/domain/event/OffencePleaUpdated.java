package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;

import java.time.LocalDate;
import java.util.UUID;

@Event("hearing.offence-plea-updated")
public class OffencePleaUpdated {

    private UUID caseId;
    private UUID originHearingId;
    private UUID offenceId;
    private UUID pleaId;
    private LocalDate pleaDate;
    private String value;

    public OffencePleaUpdated(){

    }

    public OffencePleaUpdated(UUID caseId, UUID originHearingId, UUID offenceId, UUID pleaId, LocalDate pleaDate, String value) {
        this.caseId = caseId;
        this.originHearingId = originHearingId;
        this.offenceId = offenceId;
        this.pleaId = pleaId;
        this.pleaDate = pleaDate;
        this.value = value;
    }

    public UUID getCaseId() {
        return caseId;
    }

    public UUID getOriginHearingId() {
        return originHearingId;
    }

    public UUID getOffenceId() {
        return offenceId;
    }

    public UUID getPleaId() {
        return pleaId;
    }

    public LocalDate getPleaDate() {
        return pleaDate;
    }

    public String getValue() {
        return value;
    }
}
