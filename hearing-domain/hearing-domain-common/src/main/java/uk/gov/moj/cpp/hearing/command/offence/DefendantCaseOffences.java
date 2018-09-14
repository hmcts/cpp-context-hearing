package uk.gov.moj.cpp.hearing.command.offence;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import uk.gov.justice.json.schemas.core.Offence;

import java.util.List;
import java.util.UUID;

public final class DefendantCaseOffences {

    private UUID defendantId;

    private UUID prosecutionCaseId;

    private List<Offence> offences;

    public DefendantCaseOffences() {
    }

    @JsonCreator
    public DefendantCaseOffences(@JsonProperty(value = "defendantId", required = true) final UUID defendantId,
            @JsonProperty(value = "prosecutionCaseId", required = true) final UUID prosecutionCaseId,
            @JsonProperty(value = "offences", required = true) final List<Offence> offences) {
        this.defendantId = defendantId;
        this.prosecutionCaseId = prosecutionCaseId;
        this.offences = offences;
    }

    public UUID getDefendantId() {
        return defendantId;
    }

    public UUID getProsecutionCaseId() {
        return prosecutionCaseId;
    }

    public List<Offence> getOffences() {
        return offences;
    }

    public DefendantCaseOffences withDefendantId(final UUID defendantId) {
        this.defendantId = defendantId;
        return this;
    }

    public DefendantCaseOffences withProsecutionCaseId(final UUID prosecutionCaseId) {
        this.prosecutionCaseId = prosecutionCaseId;
        return this;
    }

    public DefendantCaseOffences withOffences(final List<Offence> offences) {
        this.offences = offences;
        return this;
    }

    public static DefendantCaseOffences defendantCaseOffences() {
        return new DefendantCaseOffences();
    }
}
