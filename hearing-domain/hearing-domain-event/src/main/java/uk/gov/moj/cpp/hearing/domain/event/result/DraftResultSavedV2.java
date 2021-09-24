package uk.gov.moj.cpp.hearing.domain.event.result;

import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

import javax.json.JsonObject;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings({"squid:S2384", "squid:S1067", "squid:S1948"})
@Event("hearing.draft-result-saved-v2")
public class DraftResultSavedV2 implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID hearingId;

    private JsonObject draftResult;

    private UUID amendedByUserId;

    private LocalDate hearingDay;

    @JsonCreator
    public DraftResultSavedV2(
            @JsonProperty("hearingId") final UUID hearingId,
            @JsonProperty("hearingDay") final LocalDate hearingDay,
            @JsonProperty("draftResult") final JsonObject draftResult,
            @JsonProperty("amendedByUserId") final UUID amendedByUserId
    ) {
        this.hearingId = hearingId;
        this.draftResult = draftResult;
        this.amendedByUserId = amendedByUserId;
        this.hearingDay = hearingDay;
    }

    public void setAmendedByUserId(final UUID amendedByUserId) {
        this.amendedByUserId = amendedByUserId;
    }

    public UUID getAmendedByUserId() {
        return amendedByUserId;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public void setHearingId(final UUID hearingId) {
        this.hearingId = hearingId;
    }

    public LocalDate getHearingDay() {
        return hearingDay;
    }

    public void setHearingDay(LocalDate hearingDay) {
        this.hearingDay = hearingDay;
    }

    public JsonObject getDraftResult() {
        return draftResult;
    }

    public void setDraftResult(final JsonObject draftResult) {
        this.draftResult = draftResult;
    }
}