package uk.gov.moj.cpp.hearing.command.defendant;

import java.util.UUID;

public class CaseDefendantDetailsCommand {

    private UUID caseId;

    private Defendant defendant;

    public UUID getCaseId() {
        return caseId;
    }

    public Defendant getDefendant() {
        return defendant;
    }

    public CaseDefendantDetailsCommand setCaseId(UUID caseId) {
        this.caseId = caseId;
        return this;
    }

    public CaseDefendantDetailsCommand setDefendant(Defendant defendant) {
        this.defendant = defendant;
        return this;
    }

    public static CaseDefendantDetailsCommand caseDefendantDetailsCommand() {
        return new CaseDefendantDetailsCommand();
    }
}
