package uk.gov.moj.cpp.hearing.domain.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;
import java.util.UUID;

@Event("hearing.conviction-date-removed")
public class ConvictionDateRemoved implements Serializable {

    private static final long serialVersionUID = -5027540555216255520L;

    private UUID caseId;
    private UUID hearingId;
    private UUID offenceId;

    public ConvictionDateRemoved() {
    }

    @JsonCreator
    public ConvictionDateRemoved(@JsonProperty("caseId") final UUID caseId,
                                 @JsonProperty("hearingId") final UUID hearingId,
                                 @JsonProperty("offenceId") final UUID offenceId) {
        this.caseId = caseId;
        this.hearingId = hearingId;
        this.offenceId = offenceId;
    }

    public UUID getCaseId() {
        return caseId;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public UUID getOffenceId() {
        return offenceId;
    }

    public ConvictionDateRemoved setCaseId(UUID caseId) {
        this.caseId = caseId;
        return this;
    }

    public ConvictionDateRemoved setHearingId(UUID hearingId) {
        this.hearingId = hearingId;
        return this;
    }

    public ConvictionDateRemoved setOffenceId(UUID offenceId) {
        this.offenceId = offenceId;
        return this;
    }

    public static ConvictionDateRemoved convictionDateRemoved() {
        return new ConvictionDateRemoved();
    }
}