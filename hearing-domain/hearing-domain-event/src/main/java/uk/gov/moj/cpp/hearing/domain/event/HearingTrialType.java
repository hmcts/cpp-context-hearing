package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Event("hearing.hearing-trial-type-set")
public class HearingTrialType {

    private UUID hearingId;
    private UUID trialTypeId;
    private String code;
    private String description;
    private String type;

    @JsonCreator
    public HearingTrialType(@JsonProperty("hearingId") final UUID hearingId,
                            @JsonProperty("trialTypeId") final UUID trialTypeId,
                            @JsonProperty("code") final String code,
                            @JsonProperty("type") final String type,
                            @JsonProperty("description") final String description) {
        this.hearingId = hearingId;
        this.trialTypeId = trialTypeId;
        this.code = code;
        this.description = description;
        this.type = type;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public UUID getTrialTypeId() {
        return trialTypeId;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public String getType() {
        return type;
    }
}
