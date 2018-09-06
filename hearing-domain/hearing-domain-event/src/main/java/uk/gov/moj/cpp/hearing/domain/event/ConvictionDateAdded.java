package uk.gov.moj.cpp.hearing.domain.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

@Event("hearing.conviction-date-added")
public class ConvictionDateAdded implements Serializable {

    private static final long serialVersionUID = 8784273723346590214L;

    private UUID caseId;
    private UUID hearingId;
    private UUID offenceId;
    private LocalDate convictionDate;

    public ConvictionDateAdded() {

    }

    @JsonCreator
    public ConvictionDateAdded(@JsonProperty("caseId") final UUID caseId,
                               @JsonProperty("hearingId") final UUID hearingId,
                               @JsonProperty("offenceId") final UUID offenceId,
                               @JsonProperty("convictionDate") final LocalDate convictionDate) {
        this.caseId = caseId;
        this.hearingId = hearingId;
        this.offenceId = offenceId;
        this.convictionDate = convictionDate;
    }

    public UUID getCaseId() {
        return caseId;
    }

    public UUID getOffenceId() {
        return offenceId;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public LocalDate getConvictionDate() {
        return convictionDate;
    }

    public ConvictionDateAdded setCaseId(UUID caseId) {
        this.caseId = caseId;
        return this;
    }

    public ConvictionDateAdded setHearingId(UUID hearingId) {
        this.hearingId = hearingId;
        return this;
    }

    public ConvictionDateAdded setOffenceId(UUID offenceId) {
        this.offenceId = offenceId;
        return this;
    }

    public ConvictionDateAdded setConvictionDate(LocalDate convictionDate) {
        this.convictionDate = convictionDate;
        return this;
    }

    public static ConvictionDateAdded convictionDateAdded() {
        return new ConvictionDateAdded();
    }
}