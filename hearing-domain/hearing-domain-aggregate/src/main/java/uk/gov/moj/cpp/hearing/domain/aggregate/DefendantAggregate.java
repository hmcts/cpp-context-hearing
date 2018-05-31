package uk.gov.moj.cpp.hearing.domain.aggregate;

import uk.gov.justice.domain.aggregate.Aggregate;
import uk.gov.moj.cpp.hearing.command.defendant.CaseDefendantDetailsCommand;
import uk.gov.moj.cpp.hearing.command.defendant.Defendant;
import uk.gov.moj.cpp.hearing.command.initiate.LookupWitnessesOnDefendantForHearingCommand;
import uk.gov.moj.cpp.hearing.command.initiate.RegisterHearingAgainstDefendantCommand;
import uk.gov.moj.cpp.hearing.command.offence.UpdatedOffence;
import uk.gov.moj.cpp.hearing.domain.event.CaseDefendantDetailsWithHearings;
import uk.gov.moj.cpp.hearing.domain.event.FoundHearingsForNewOffence;
import uk.gov.moj.cpp.hearing.domain.event.DefenceWitnessAdded;
import uk.gov.moj.cpp.hearing.domain.event.FoundWitnessesForHearingToInherit;
import uk.gov.moj.cpp.hearing.domain.event.RegisteredHearingAgainstDefendant;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.match;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.otherwiseDoNothing;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.when;

@SuppressWarnings({"squid:S00107", "squid:S1948"})
public class DefendantAggregate implements Aggregate {

    private static final long serialVersionUID = 1L;

    private final List<DefenceWitnessAdded> defenceWitnessAdded = new ArrayList<>();

    private List<UUID> hearingIds = new ArrayList<>();

    @Override
    public Object apply(Object event) {

        return match(event)
                .with(
                        when(RegisteredHearingAgainstDefendant.class).apply(defendant -> hearingIds.add(defendant.getHearingId())),
                        when(DefenceWitnessAdded.class).apply(witnessAdded -> defenceWitnessAdded.add(witnessAdded)),
                        otherwiseDoNothing());
    }

    public Stream<Object> addWitness(final UUID witnessId, final UUID hearingId, final UUID defendantId, final String type, final String classification, final String title, final String firstName, final String lastName) {
        return apply(Stream.of(new DefenceWitnessAdded(witnessId, defendantId, hearingId, type, classification, title, firstName, lastName)));
    }

    public Stream<Object> lookupWitnessesForHearing(final LookupWitnessesOnDefendantForHearingCommand lookupWitnessesOnDefendantForHearingCommand) {

        final Stream.Builder<Object> streamBuilder = Stream.builder();
        defenceWitnessAdded.forEach(witness ->
                streamBuilder.add(
                        FoundWitnessesForHearingToInherit.foundWitnessesForHearingToInherit()
                                .setId(witness.getWitnessId())
                                .setHearingId(lookupWitnessesOnDefendantForHearingCommand.getHearingId())
                                .setDefendantId(lookupWitnessesOnDefendantForHearingCommand.getDefendantId())
                                .setFirstName(witness.getFirstName())
                                .setLastName(witness.getLastName())
                                .setTitle(witness.getTitle())
                                .setClassification(witness.getClassification())
                                .setType(witness.getType())
                ));
        return apply(streamBuilder.build());
    }

    public Stream<Object> registerHearing(RegisterHearingAgainstDefendantCommand command) {
        return apply(Stream.of(
                RegisteredHearingAgainstDefendant.builder()
                        .withDefendantId(command.getDefendantId())
                        .withHearingId(command.getHearingId())
                        .build()
        ));
    }

    public Stream<Object> enrichCaseDefendantDetailsWithHearingIds(CaseDefendantDetailsCommand caseDefendantDetails) {

        final CaseDefendantDetailsWithHearings caseDefendantDetailsWithHearings = CaseDefendantDetailsWithHearings.builder()
                .withCaseId(caseDefendantDetails.getCaseId())
                .withDefendant(Defendant.builder(caseDefendantDetails.getDefendant()))
                .withHearingIds(hearingIds)
                .build();

        return apply(Stream.of(caseDefendantDetailsWithHearings));
    }

    public Stream<Object> lookupHearingsForNewOffenceOnDefendant(UUID defendantId, UUID caseId, UpdatedOffence offence) {
        return apply(Stream.of(FoundHearingsForNewOffence.builder()
                .withId(offence.getId())
                .withDefendantId(defendantId)
                .withCaseId(caseId)
                .withOffenceCode(offence.getOffenceCode())
                .withWording(offence.getWording())
                .withStartDate(offence.getStartDate())
                .withEndDate(offence.getEndDate())
                .withCount(offence.getCount())
                .withConvictionDate(offence.getConvictionDate())
                .withHearingIds(hearingIds)
                .build()
        ));
    }
}