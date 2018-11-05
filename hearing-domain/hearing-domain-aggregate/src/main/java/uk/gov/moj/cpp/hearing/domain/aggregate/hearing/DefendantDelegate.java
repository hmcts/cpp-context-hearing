package uk.gov.moj.cpp.hearing.domain.aggregate.hearing;

import static java.util.Optional.ofNullable;

import uk.gov.moj.cpp.hearing.command.defendant.CaseDefendantDetailsWithHearingCommand;
import uk.gov.moj.cpp.hearing.command.defendant.Defendant;
import uk.gov.moj.cpp.hearing.command.initiate.Address;
import uk.gov.moj.cpp.hearing.command.initiate.Interpreter;
import uk.gov.moj.cpp.hearing.domain.event.DefendantDetailsUpdated;

import java.io.Serializable;
import java.util.stream.Stream;

@SuppressWarnings({"squid:S1188"})
public class DefendantDelegate implements Serializable {

    private static final long serialVersionUID = 1L;

    private final HearingAggregateMomento momento;

    public DefendantDelegate(final HearingAggregateMomento momento) {
        this.momento = momento;
    }

    public void handleDefendantDetailsUpdated(DefendantDetailsUpdated defendantDetailsUpdated) {
        this.momento.getHearing().getDefendants().stream()
                .filter(d -> d.getId().equals(defendantDetailsUpdated.getDefendant().getId()))
                .forEach(d -> d.setDefenceOrganisation(defendantDetailsUpdated.getDefendant().getDefenceOrganisation())
                        .setPersonId(defendantDetailsUpdated.getDefendant().getPerson().getId())
                        .setFirstName(defendantDetailsUpdated.getDefendant().getPerson().getFirstName())
                        .setLastName(defendantDetailsUpdated.getDefendant().getPerson().getLastName())
                        .setGender(defendantDetailsUpdated.getDefendant().getPerson().getGender())
                        .setNationality(defendantDetailsUpdated.getDefendant().getPerson().getNationality())
                        .setDateOfBirth(defendantDetailsUpdated.getDefendant().getPerson().getDateOfBirth())
                        .setAddress(ofNullable(defendantDetailsUpdated.getDefendant().getPerson().getAddress())
                                .map(a -> Address.address()
                                        .setAddress1(defendantDetailsUpdated.getDefendant().getPerson().getAddress().getAddress1())
                                        .setAddress2(defendantDetailsUpdated.getDefendant().getPerson().getAddress().getAddress2())
                                        .setAddress3(defendantDetailsUpdated.getDefendant().getPerson().getAddress().getAddress3())
                                        .setAddress4(defendantDetailsUpdated.getDefendant().getPerson().getAddress().getAddress4())
                                        .setPostCode(defendantDetailsUpdated.getDefendant().getPerson().getAddress().getPostCode())
                                )
                                .orElse(null))
                        .setInterpreter(ofNullable(defendantDetailsUpdated.getDefendant().getInterpreter())
                                .map(i -> Interpreter.interpreter()
                                        .setLanguage(defendantDetailsUpdated.getDefendant().getInterpreter().getLanguage())
                                )
                                .orElse(null))
                        .getDefendantCases().stream()
                        .filter(dc -> dc.getCaseId().equals(defendantDetailsUpdated.getCaseId()))
                        .forEach(dc -> {
                            dc.setBailStatus(defendantDetailsUpdated.getDefendant().getBailStatus());
                            dc.setCustodyTimeLimitDate(defendantDetailsUpdated.getDefendant().getCustodyTimeLimitDate());
                        })
                );
    }

    public Stream<Object> updateDefendantDetails(final CaseDefendantDetailsWithHearingCommand command) {

        if (!this.momento.isPublished()) {
            return Stream.of(DefendantDetailsUpdated.builder()
                    .withCaseId(command.getCaseId())
                    .withHearingId(command.getHearingIds().get(0))
                    .withDefendant(Defendant.builder(command.getDefendant()))
                    .build());
        }

        return Stream.empty();
    }
}
