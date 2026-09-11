package uk.gov.moj.cpp.hearing.command;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SavePtphDetailCommand {

    private UUID hearingId;
    private Tier tier;
    private ListType listType;
    private String keyReason;

    public SavePtphDetailCommand() {
    }

    @JsonCreator
    public SavePtphDetailCommand(@JsonProperty("hearingId") final UUID hearingId,
                                      @JsonProperty("tier") final Tier tier,
                                      @JsonProperty("listType") final ListType listType,
                                      @JsonProperty("keyReason") final String keyReason) {
        this.hearingId = hearingId;
        this.tier = tier;
        this.listType = listType;
        this.keyReason = keyReason;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public Tier getTier() {
        return tier;
    }

    public ListType getListType() {
        return listType;
    }

    public String getKeyReason() {
        return keyReason;
    }
}
