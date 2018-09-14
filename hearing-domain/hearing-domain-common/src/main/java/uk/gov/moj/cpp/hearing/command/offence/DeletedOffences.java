package uk.gov.moj.cpp.hearing.command.offence;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DeletedOffences {

    private UUID defendantId;
    private UUID prosecutionCaseId;
    private List<UUID> offences;

    public DeletedOffences() {
    }

    @JsonCreator
    public DeletedOffences(@JsonProperty(value = "defendantId", required = true) final UUID defendantId,
            @JsonProperty(value = "prosecutionCaseId", required = true) final UUID prosecutionCaseId,
            @JsonProperty(value = "offences", required = true) final List<UUID> offences) {
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

    public List<UUID> getOffences() {
        return offences;
    }

    public DeletedOffences setProsecutionCaseId(UUID caseId) {
        this.prosecutionCaseId = caseId;
        return this;
    }

    public DeletedOffences setOffences(List<UUID> offences) {
        this.offences = offences;
        return this;
    }

    public DeletedOffences setDefendantId(UUID defendantId) {
        this.defendantId = defendantId;
        return this;
    }

    public static DeletedOffences deletedOffences() {
        return new DeletedOffences();
    }
}
