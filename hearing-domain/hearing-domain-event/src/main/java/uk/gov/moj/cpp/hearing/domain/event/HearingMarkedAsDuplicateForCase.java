package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Event("hearing.events.marked-as-duplicate-for-case")
public class HearingMarkedAsDuplicateForCase implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID prosecutionCaseId;

    private UUID hearingId;

    @JsonCreator
    public HearingMarkedAsDuplicateForCase(@JsonProperty("prosecutionCaseId") final UUID prosecutionCaseId, @JsonProperty("hearingId") final UUID hearingId) {
        this.prosecutionCaseId = prosecutionCaseId;
        this.hearingId = hearingId;
    }

    public UUID getProsecutionCaseId() {
        return prosecutionCaseId;
    }

    public void setProsecutionCaseId(final UUID prosecutionCaseId) {
        this.prosecutionCaseId = prosecutionCaseId;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public void setHearingId(final UUID hearingId) {
        this.hearingId = hearingId;
    }
}
