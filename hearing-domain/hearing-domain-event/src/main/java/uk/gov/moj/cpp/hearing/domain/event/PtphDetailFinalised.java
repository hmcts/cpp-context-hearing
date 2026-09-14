package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Event("hearing.ptph-detail-finalised")
public class PtphDetailFinalised {

    private final UUID hearingId;

    @JsonCreator
    public PtphDetailFinalised(@JsonProperty("hearingId") final UUID hearingId) {
        this.hearingId = hearingId;
    }

    public UUID getHearingId() {
        return hearingId;
    }
}
