package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;

import java.time.LocalDate;
import java.util.UUID;

@Event("hearing.offence-plea-updated")
public class OffencePleaUpdated {

    private UUID originHearingId;
    private UUID offenceId;
    private LocalDate pleaDate;
    private String value;

    public OffencePleaUpdated(){

    }

    public OffencePleaUpdated(UUID originHearingId, UUID offenceId, LocalDate pleaDate, String value) {
        this.originHearingId = originHearingId;
        this.offenceId = offenceId;
        this.pleaDate = pleaDate;
        this.value = value;
    }

    public UUID getOriginHearingId() {
        return originHearingId;
    }

    public UUID getOffenceId() {
        return offenceId;
    }

    public LocalDate getPleaDate() {
        return pleaDate;
    }

    public String getValue() {
        return value;
    }
}
