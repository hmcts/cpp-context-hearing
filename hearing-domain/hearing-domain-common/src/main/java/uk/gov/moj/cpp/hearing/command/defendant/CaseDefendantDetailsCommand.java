package uk.gov.moj.cpp.hearing.command.defendant;

public class CaseDefendantDetailsCommand {

    private Defendant defendant;

    public static CaseDefendantDetailsCommand caseDefendantDetailsCommand() {
        return new CaseDefendantDetailsCommand();
    }

    public Defendant getDefendant() {
        return defendant;
    }

    public CaseDefendantDetailsCommand setDefendant(Defendant defendant) {
        this.defendant = defendant;
        return this;
    }
}
