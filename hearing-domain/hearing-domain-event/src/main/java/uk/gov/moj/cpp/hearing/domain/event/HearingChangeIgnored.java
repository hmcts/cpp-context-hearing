package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;

@Event("hearing.hearing-change-ignored")
public class HearingChangeIgnored implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID hearingId;
    private String reason;


    @JsonCreator
    public HearingChangeIgnored(final UUID hearingId, final String reason) {
        this.hearingId = hearingId;
        this.reason = reason;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public String getReason() {
        return reason;
    }


}
