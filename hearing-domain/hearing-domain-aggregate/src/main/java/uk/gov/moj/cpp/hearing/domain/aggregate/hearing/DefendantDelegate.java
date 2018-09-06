package uk.gov.moj.cpp.hearing.domain.aggregate.hearing;

import static java.util.Optional.ofNullable;

import uk.gov.justice.json.schemas.core.Address;
import uk.gov.justice.json.schemas.core.Organisation;
import uk.gov.moj.cpp.hearing.command.defendant.CaseDefendantDetailsWithHearingCommand;
import uk.gov.moj.cpp.hearing.command.defendant.Interpreter;
import uk.gov.moj.cpp.hearing.domain.event.DefendantDetailsUpdated;

import java.io.Serializable;
import java.util.UUID;
import java.util.stream.Stream;

@SuppressWarnings({"squid:S1188"})
public class DefendantDelegate implements Serializable {

    private static final long serialVersionUID = 1L;

    private final HearingAggregateMomento momento;

    public DefendantDelegate(final HearingAggregateMomento momento) {
        this.momento = momento;
    }

    public void handleDefendantDetailsUpdated(DefendantDetailsUpdated defendantDetailsUpdated) {
        this.momento.getHearing().getProsecutionCases().forEach(prosecutionCase -> prosecutionCase.getDefendants().stream()
                .filter(defendant -> defendant.getId().equals(defendantDetailsUpdated.getDefendant().getId()))
                .forEach(defendant -> defendant.setDefenceOrganisation(
                        Organisation.organisation()
                                .withId(UUID.randomUUID()) //TODO: GPE-5789 fix Defence Organisation Id
                                .withName(defendantDetailsUpdated.getDefendant().getDefenceOrganisation())
                                .build())
                        .getPersonDefendant()
                        .setBailStatus(defendantDetailsUpdated.getDefendant().getBailStatus())
                        .setCustodyTimeLimit(defendantDetailsUpdated.getDefendant().getCustodyTimeLimitDate())
                        .getPersonDetails()
                        .setFirstName(defendantDetailsUpdated.getDefendant().getPerson().getFirstName())
                        .setLastName(defendantDetailsUpdated.getDefendant().getPerson().getLastName())
                        .setGender(defendantDetailsUpdated.getDefendant().getPerson().getGender())
                        .setNationalityCode(defendantDetailsUpdated.getDefendant().getPerson().getNationality())
                        .setDateOfBirth(defendantDetailsUpdated.getDefendant().getPerson().getDateOfBirth())
                        .setAddress(ofNullable(defendantDetailsUpdated.getDefendant().getPerson().getAddress())
                                .map(address ->
                                        Address.address()
                                                .withAddress1(address.getAddress1())
                                                .withAddress2(address.getAddress2())
                                                .withAddress3(address.getAddress3())
                                                .withAddress4(address.getAddress4())
                                                .withAddress5(null)
                                                .withPostcode(address.getPostCode())
                                                .build())
                                .orElse(null))
                        .setInterpreterLanguageNeeds(ofNullable(defendantDetailsUpdated.getDefendant().getInterpreter())
                                .map(Interpreter::getLanguage)
                                .orElse(null))));
    }

    public Stream<Object> updateDefendantDetails(final CaseDefendantDetailsWithHearingCommand command) {

        if (!this.momento.isPublished()) {
            return Stream.of(DefendantDetailsUpdated.defendantDetailsUpdated()
                    .setCaseId(command.getCaseId())
                    .setHearingId(command.getHearingIds().get(0))
                    .setDefendant(command.getDefendant())
            );
        }

        return Stream.empty();
    }
}
