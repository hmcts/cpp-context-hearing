package uk.gov.moj.cpp.hearing.command.initiate;

import java.io.Serializable;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class RegisterHearingAgainstOffenceCommand implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID offenceId;
    private UUID hearingId;

    public RegisterHearingAgainstOffenceCommand() {

    }

    @JsonCreator
    public RegisterHearingAgainstOffenceCommand(@JsonProperty("hearingId") UUID hearingId,
                                                @JsonProperty("offenceId") UUID offenceId) {
        this.offenceId = offenceId;
        this.hearingId = hearingId;
    }

    public static RegisterHearingAgainstOffenceCommand registerHearingAgainstOffenceDefendantCommand() {
        return new RegisterHearingAgainstOffenceCommand();
    }

    public UUID getOffenceId() {
        return offenceId;
    }

    public RegisterHearingAgainstOffenceCommand setOffenceId(UUID offenceId) {
        this.offenceId = offenceId;
        return this;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public RegisterHearingAgainstOffenceCommand setHearingId(UUID hearingId) {
        this.hearingId = hearingId;
        return this;
    }
}
