package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Event("hearing.event.vacated-trial-detail-updated")
public class HearingVacatedTrialDetailUpdated implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID hearingId;
    private UUID vacatedTrialReasonId;
    private Boolean isVacated;

    public HearingVacatedTrialDetailUpdated() {
    }

    @JsonCreator
    public HearingVacatedTrialDetailUpdated(@JsonProperty("hearingId") final UUID hearingId,
                                            @JsonProperty("isVacated") final Boolean isVacated,
                                            @JsonProperty("vacatedTrialReasonId") final UUID vacatedTrialReasonId) {
        this.hearingId = hearingId;
        this.isVacated = isVacated;
        this.vacatedTrialReasonId = vacatedTrialReasonId;
    }

    public static HearingVacatedTrialDetailUpdated hearingVacatedTrialDetailChanged() {
        return new HearingVacatedTrialDetailUpdated();
    }


    public Boolean getIsVacated() {
        return isVacated;
    }

    public HearingVacatedTrialDetailUpdated setIsVacated(final Boolean isVacated) {
        this.isVacated = isVacated;
        return this;
    }

    public UUID getVacatedTrialReasonId() {
        return vacatedTrialReasonId;
    }

    public HearingVacatedTrialDetailUpdated setVacatedTrialReasonId(final UUID vacatedTrialReasonId) {
        this.vacatedTrialReasonId = vacatedTrialReasonId;
        return this;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public HearingVacatedTrialDetailUpdated setHearingId(final UUID hearingId) {
        this.hearingId = hearingId;
        return this;
    }

}
