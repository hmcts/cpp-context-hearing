package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Event("hearing.conviction-date-added")
public class ConvictionDateAdded implements Serializable {

    private static final long serialVersionUID = 8784273723346590214L;

    private UUID caseId;
    private UUID hearingId;
    private UUID offenceId;
    private UUID courtApplicationId;
    private LocalDate convictionDate;

    public ConvictionDateAdded() {

    }

    @JsonCreator
    public ConvictionDateAdded(@JsonProperty("caseId") final UUID caseId,
                               @JsonProperty("hearingId") final UUID hearingId,
                               @JsonProperty("offenceId") final UUID offenceId,
                               @JsonProperty("convictionDate") final LocalDate convictionDate,
                               @JsonProperty("courtApplicationId") final UUID courtApplicationId) {
        this.caseId = caseId;
        this.hearingId = hearingId;
        this.offenceId = offenceId;
        this.convictionDate = convictionDate;
        this.courtApplicationId = courtApplicationId;
    }

    public static ConvictionDateAdded convictionDateAdded() {
        return new ConvictionDateAdded();
    }

    public UUID getCaseId() {
        return caseId;
    }

    public ConvictionDateAdded setCaseId(UUID caseId) {
        this.caseId = caseId;
        return this;
    }

    public UUID getOffenceId() {
        return offenceId;
    }

    public ConvictionDateAdded setOffenceId(UUID offenceId) {
        this.offenceId = offenceId;
        return this;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public ConvictionDateAdded setHearingId(UUID hearingId) {
        this.hearingId = hearingId;
        return this;
    }

    public LocalDate getConvictionDate() {
        return convictionDate;
    }

    public ConvictionDateAdded setConvictionDate(LocalDate convictionDate) {
        this.convictionDate = convictionDate;
        return this;
    }

    public UUID getCourtApplicationId() { return courtApplicationId;}

    public ConvictionDateAdded setCourtApplicationId(final UUID courtApplicationId){
        this.courtApplicationId = courtApplicationId;
        return this;
    }
}