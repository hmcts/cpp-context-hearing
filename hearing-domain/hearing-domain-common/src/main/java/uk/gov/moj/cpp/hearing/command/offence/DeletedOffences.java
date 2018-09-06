package uk.gov.moj.cpp.hearing.command.offence;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DeletedOffences {

    private UUID defendantId;
    private UUID caseId;
    private List<UUID> offences;

    public DeletedOffences() {
    }

    @JsonCreator
    public DeletedOffences(@JsonProperty("defendantId") final UUID defendantId,
                           @JsonProperty("caseId") final UUID caseId,
                           @JsonProperty("offences") final List<UUID> offences) {
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

    public List<UUID> getOffences() {
        return offences;
    }

    public DeletedOffences setCaseId(UUID caseId) {
        this.caseId = caseId;
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
