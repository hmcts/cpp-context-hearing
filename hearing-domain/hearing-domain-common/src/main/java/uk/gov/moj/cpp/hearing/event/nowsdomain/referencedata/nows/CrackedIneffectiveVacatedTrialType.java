package uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows;

import java.time.LocalDate;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CrackedIneffectiveVacatedTrialType {

    private UUID id;
    private String reasonCode;
    private String trialType;
    private String reasonShortDescription;
    private String reasonFullDescription;
    private LocalDate date;

    @JsonCreator
    public CrackedIneffectiveVacatedTrialType(@JsonProperty("id") final UUID id,
                                              @JsonProperty("reasonCode") final String reasonCode,
                                              @JsonProperty("trialType") final String trialType,
                                              @JsonProperty("reasonShortDescription") final String reasonShortDescription,
                                              @JsonProperty("reasonFullDescription") final String reasonFullDescription,
                                              @JsonProperty("date") final LocalDate date) {
        this.id = id;
        this.reasonCode = reasonCode;
        this.trialType = trialType;
        this.reasonShortDescription = reasonShortDescription;
        this.reasonFullDescription = reasonFullDescription;
        this.date = date;
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

    public LocalDate getDate() {
        return date;
    }

    public String getReasonShortDescription() {
        return reasonShortDescription;
    }
}
