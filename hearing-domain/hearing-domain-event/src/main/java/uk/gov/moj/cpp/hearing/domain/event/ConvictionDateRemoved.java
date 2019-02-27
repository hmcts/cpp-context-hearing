package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

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

    public static ConvictionDateRemoved convictionDateRemoved() {
        return new ConvictionDateRemoved();
    }

    public UUID getCaseId() {
        return caseId;
    }

    public ConvictionDateRemoved setCaseId(UUID caseId) {
        this.caseId = caseId;
        return this;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public ConvictionDateRemoved setHearingId(UUID hearingId) {
        this.hearingId = hearingId;
        return this;
    }

    public UUID getOffenceId() {
        return offenceId;
    }

    public ConvictionDateRemoved setOffenceId(UUID offenceId) {
        this.offenceId = offenceId;
        return this;
    }
}