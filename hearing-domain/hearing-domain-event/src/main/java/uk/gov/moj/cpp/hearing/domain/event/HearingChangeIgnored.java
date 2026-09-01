package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Event("hearing.hearing-change-ignored")
public class HearingChangeIgnored implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID hearingId;
    private String reason;


    /**
     * Parameters are named explicitly rather than relying on {@code ParameterNamesModule} plus a
     * {@code -parameters} compile flag inherited from the parent pom. This event is written to the
     * hearing's own stream and so is replayed on every subsequent aggregate load — if it ever
     * failed to deserialise, every later command on that hearing would fail, not just the one that
     * was ignored. {@code PtphDetailSaved} and the other events in this package name them too.
     */
    @JsonCreator
    public HearingChangeIgnored(@JsonProperty("hearingId") final UUID hearingId,
                                @JsonProperty("reason") final String reason) {
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
