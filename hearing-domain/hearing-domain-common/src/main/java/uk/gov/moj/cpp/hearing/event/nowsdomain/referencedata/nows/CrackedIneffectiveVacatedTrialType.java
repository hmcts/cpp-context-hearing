package uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CrackedIneffectiveVacatedTrialType {

    private UUID id;
    private String reasonCode;
    private String trialType;
    private String reasonFullDescription;

    @JsonCreator
    public CrackedIneffectiveVacatedTrialType(@JsonProperty("id") final UUID id,
                                              @JsonProperty("reasonCode") final String reasonCode,
                                              @JsonProperty("trialType") final String trialType,
                                              @JsonProperty("reasonFullDescription") final String reasonFullDescription) {
        this.id = id;
        this.reasonCode = reasonCode;
        this.trialType = trialType;
        this.reasonFullDescription = reasonFullDescription;
    }

    public UUID getId() {
        return id;
    }

    public String getReasonCode() {
        return reasonCode;
    }

    public String getTrialType() {
        return trialType;
    }

    public String getReasonFullDescription() {
        return reasonFullDescription;
    }
}
