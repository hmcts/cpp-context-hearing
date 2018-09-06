package uk.gov.moj.cpp.hearing.command.defendant;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CaseDefendantDetailsWithHearingCommand {

    private UUID caseId;

    private Defendant defendant;

    private List<UUID> hearingIds;

    public UUID getCaseId() {
        return caseId;
    }

    public Defendant getDefendant() {
        return defendant;
    }

    public List<UUID> getHearingIds() {
        return new ArrayList<>(hearingIds);
    }

    public CaseDefendantDetailsWithHearingCommand setCaseId(UUID caseId) {
        this.caseId = caseId;
        return this;
    }

    public CaseDefendantDetailsWithHearingCommand setDefendant(Defendant defendant) {
        this.defendant = defendant;
        return this;
    }

    public CaseDefendantDetailsWithHearingCommand setHearingIds(List<UUID> hearingIds) {
        this.hearingIds = hearingIds;
        return this;
    }

    public static CaseDefendantDetailsWithHearingCommand caseDefendantDetailsWithHearingCommand() {
        return new CaseDefendantDetailsWithHearingCommand();
    }
}
