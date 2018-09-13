package uk.gov.moj.cpp.hearing.domain.aggregate.hearing;

import uk.gov.justice.json.schemas.core.Defendant;
import uk.gov.moj.cpp.hearing.command.defendant.CaseDefendantDetailsWithHearingCommand;
import uk.gov.moj.cpp.hearing.domain.event.DefendantDetailsUpdated;

import java.io.Serializable;
import java.util.stream.Stream;

public class DefendantDelegate implements Serializable {

    private static final long serialVersionUID = 1L;

    private final HearingAggregateMomento momento;

    public DefendantDelegate(final HearingAggregateMomento momento) {
        this.momento = momento;
    }

    public void handleDefendantDetailsUpdated(final DefendantDetailsUpdated defendantDetailsUpdated) {

        this.momento.getHearing().getProsecutionCases().forEach(prosecutionCase -> prosecutionCase.getDefendants().forEach(defendant -> {

            if (matchDefendant(defendant, defendantDetailsUpdated)) {

                final uk.gov.moj.cpp.hearing.command.defendant.Defendant defendantIn = defendantDetailsUpdated.getDefendant();

                defendant.setAssociatedPersons(defendantIn.getAssociatedPersons())
                        .setDefenceOrganisation(defendantIn.getDefenceOrganisation())
                        .setLegalEntityDefendant(defendantIn.getLegalEntityDefendant())
                        .setMitigation(defendantIn.getMitigation())
                        .setMitigationWelsh(defendantIn.getMitigationWelsh())
                        .setNumberOfPreviousConvictionsCited(defendantIn.getNumberOfPreviousConvictionsCited())
                        .setPersonDefendant(defendantIn.getPersonDefendant())
                        .setProsecutionAuthorityReference(defendantIn.getProsecutionAuthorityReference())
                        .setWitnessStatement(defendantIn.getWitnessStatement())
                        .setWitnessStatementWelsh(defendantIn.getWitnessStatementWelsh())
                        .setProsecutionCaseId(defendantIn.getProsecutionCaseId());
            }

        }));

    }

    public Stream<Object> updateDefendantDetails(final CaseDefendantDetailsWithHearingCommand command) {

        if (!this.momento.isPublished()) {
            return Stream.of(DefendantDetailsUpdated.defendantDetailsUpdated()
                    .setHearingId(command.getHearingId())
                    .setDefendant(command.getDefendant())
            );
        }

        return Stream.empty();
    }

    private boolean matchDefendant(final Defendant defendant, final DefendantDetailsUpdated defendantDetailsUpdated) {
        return defendant.getId().equals(defendantDetailsUpdated.getDefendant().getId()) &&
                defendant.getProsecutionCaseId().equals(defendantDetailsUpdated.getDefendant().getProsecutionCaseId());
    }
}
