package uk.gov.moj.cpp.hearing.domain.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.justice.core.courts.Offence;
import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Event("hearing.events.hearing-initiate-ignored")
public class HearingInitiateIgnored implements Serializable {

    private static final long serialVersionUID = 1L;

    private final UUID hearingId;
    private final List<Offence> offences;

    @JsonCreator
    public HearingInitiateIgnored(@JsonProperty("hearingId") final UUID hearingId, @JsonProperty("offences") final List<Offence> offences) {
        this.hearingId = hearingId;
        this.offences = offences;
    }

    public List<Offence> getOffences() {
        return offences;
    }

    public UUID getHearingId() {
        return hearingId;
    }
}