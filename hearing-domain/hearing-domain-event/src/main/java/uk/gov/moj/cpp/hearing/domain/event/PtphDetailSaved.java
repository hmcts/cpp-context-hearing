package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Event("hearing.ptph-detail-saved")
public class PtphDetailSaved {

    private final UUID hearingId;
    private final String tier;
    private final String listType;
    private final String keyReason;

    @JsonCreator
    public PtphDetailSaved(@JsonProperty("hearingId") final UUID hearingId,
                                @JsonProperty("tier") final String tier,
                                @JsonProperty("listType") final String listType,
                                @JsonProperty("keyReason") final String keyReason) {
        this.hearingId = hearingId;
        this.tier = tier;
        this.listType = listType;
        this.keyReason = keyReason;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public String getTier() {
        return tier;
    }

    public String getListType() {
        return listType;
    }

    public String getKeyReason() {
        return keyReason;
    }
}
