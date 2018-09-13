package uk.gov.moj.cpp.hearing.command.defendant;

import java.util.UUID;

public class CaseDefendantDetailsWithHearingCommand {

    private Defendant defendant;

    private UUID hearingId;

    public Defendant getDefendant() {
        return defendant;
    }

    public UUID getHearingId() {
        return this.hearingId;
    }

    public CaseDefendantDetailsWithHearingCommand setDefendant(Defendant defendant) {
        this.defendant = defendant;
        return this;
    }

    public CaseDefendantDetailsWithHearingCommand setHearingId(UUID hearingId) {
        this.hearingId = hearingId;
        return this;
    }

    public static CaseDefendantDetailsWithHearingCommand caseDefendantDetailsWithHearingCommand() {
        return new CaseDefendantDetailsWithHearingCommand();
    }
}
