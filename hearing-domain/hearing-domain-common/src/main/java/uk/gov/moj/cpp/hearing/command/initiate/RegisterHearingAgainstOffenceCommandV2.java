package uk.gov.moj.cpp.hearing.command.initiate;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class RegisterHearingAgainstOffenceCommandV2 implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID offenceId;
    private List<UUID> hearingIds;

    public RegisterHearingAgainstOffenceCommandV2() {

    }

    @JsonCreator
    public RegisterHearingAgainstOffenceCommandV2(@JsonProperty("hearingIds") List<UUID> hearingIds,
                                                  @JsonProperty("offenceId") UUID offenceId) {
        this.offenceId = offenceId;
        this.hearingIds = hearingIds;
    }

    public static RegisterHearingAgainstOffenceCommandV2 registerHearingAgainstOffenceDefendantCommandV2() {
        return new RegisterHearingAgainstOffenceCommandV2();
    }

    public UUID getOffenceId() {
        return offenceId;
    }

    public RegisterHearingAgainstOffenceCommandV2 setOffenceId(UUID offenceId) {
        this.offenceId = offenceId;
        return this;
    }

    public List<UUID> getHearingIds() {
        return hearingIds;
    }

    public RegisterHearingAgainstOffenceCommandV2 setHearingIds(List<UUID> hearingIds) {
        this.hearingIds = hearingIds.stream().toList();
        return this;
    }
}
