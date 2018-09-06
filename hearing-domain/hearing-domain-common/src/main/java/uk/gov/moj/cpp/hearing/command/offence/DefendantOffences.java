package uk.gov.moj.cpp.hearing.command.offence;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.UUID;

public final class DefendantOffences {

    private UUID defendantId;

    private UUID caseId;

    private List<DefendantOffence> offences;

    public DefendantOffences() {
    }

    @JsonCreator
    public DefendantOffences(@JsonProperty("defendantId") final UUID defendantId,
                             @JsonProperty("caseId") final UUID caseId,
                             @JsonProperty("offences") final List<DefendantOffence> offences) {
        this.defendantId = defendantId;
        this.caseId = caseId;
        this.offences = offences;
    }

    public UUID getDefendantId() {
        return defendantId;
    }

    public UUID getCaseId() {
        return caseId;
    }

    public List<DefendantOffence> getOffences() {
        return offences;
    }

    public DefendantOffences setDefendantId(UUID defendantId) {
        this.defendantId = defendantId;
        return this;
    }

    public DefendantOffences setCaseId(UUID caseId) {
        this.caseId = caseId;
        return this;
    }

    public DefendantOffences setOffences(List<DefendantOffence> offences) {
        this.offences = offences;
        return this;
    }

    public static DefendantOffences defendantOffences() {
        return new DefendantOffences();
    }
}
