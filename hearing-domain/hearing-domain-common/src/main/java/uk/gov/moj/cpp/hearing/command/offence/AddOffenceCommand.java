package uk.gov.moj.cpp.hearing.command.offence;

import uk.gov.justice.core.courts.Offence;

import java.io.Serializable;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings("squid:S00107")
public class AddOffenceCommand implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID hearingId;
    private UUID defendantId;
    private UUID prosecutionCaseId;
    private Offence offence;

    private AddOffenceCommand() {
    }

    @JsonCreator
    protected AddOffenceCommand(@JsonProperty("hearingId") final UUID hearingId,
                                @JsonProperty("defendantId") final UUID defendantId,
                                @JsonProperty("prosecutionCaseId") final UUID prosecutionCaseId,
                                @JsonProperty("offence") final Offence offence) {
        this.hearingId = hearingId;
        this.defendantId = defendantId;
        this.prosecutionCaseId = prosecutionCaseId;
        this.offence = offence;
    }

    public static AddOffenceCommand offenceAdded() {
        return new AddOffenceCommand();
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public UUID getDefendantId() {
        return defendantId;
    }

    public UUID getProsecutionCaseId() {
        return prosecutionCaseId;
    }

    public Offence getOffence() {
        return offence;
    }

    public AddOffenceCommand withHearingId(UUID hearingId) {
        this.hearingId = hearingId;
        return this;
    }

    public AddOffenceCommand withDefendantId(UUID defendantId) {
        this.defendantId = defendantId;
        return this;
    }

    public AddOffenceCommand withProsecutionCaseId(UUID prosecutionCaseId) {
        this.prosecutionCaseId = prosecutionCaseId;
        return this;
    }

    public AddOffenceCommand withOffence(Offence offence) {
        this.offence = offence;
        return this;
    }
}