package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Event("hearing.hearing-effective-trial-set")
public class HearingEffectiveTrial {

    private UUID hearingId;
    private Boolean isEffectiveTrial;

    @JsonCreator
    public HearingEffectiveTrial(@JsonProperty("hearingId") final UUID hearingId,
                            @JsonProperty("isEffectiveTrial") final Boolean isEffectiveTrial) {
        this.hearingId = hearingId;
        this.isEffectiveTrial = isEffectiveTrial;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public Boolean getIsEffectiveTrial() {
        return isEffectiveTrial;
    }
}
