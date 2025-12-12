package uk.gov.moj.cpp.hearing.command.defendant;

import java.util.UUID;

public class CaseDefendantDetailsWithHearingCommand {

    private Defendant defendant;

    private UUID hearingId;

    public static CaseDefendantDetailsWithHearingCommand caseDefendantDetailsWithHearingCommand() {
        return new CaseDefendantDetailsWithHearingCommand();
    }

    public Defendant getDefendant() {
        return defendant;
    }

    public CaseDefendantDetailsWithHearingCommand setDefendant(Defendant defendant) {
        this.defendant = defendant;
        return this;
    }

    public UUID getHearingId() {
        return this.hearingId;
    }

    public CaseDefendantDetailsWithHearingCommand setHearingId(UUID hearingId) {
        this.hearingId = hearingId;
        return this;
    }
}
