package uk.gov.moj.cpp.hearing.domain.aggregate;

import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.match;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.otherwiseDoNothing;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.when;

import uk.gov.justice.domain.aggregate.Aggregate;
import uk.gov.moj.cpp.hearing.command.defendant.CaseDefendantDetailsCommand;
import uk.gov.moj.cpp.hearing.command.initiate.RegisterHearingAgainstDefendantCommand;
import uk.gov.moj.cpp.hearing.command.offence.DefendantOffence;
import uk.gov.moj.cpp.hearing.domain.event.CaseDefendantDetailsWithHearings;
import uk.gov.moj.cpp.hearing.domain.event.FoundHearingsForNewOffence;
import uk.gov.moj.cpp.hearing.domain.event.RegisteredHearingAgainstDefendant;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@SuppressWarnings({"squid:S00107", "squid:S1948"})
public class DefendantAggregate implements Aggregate {

    private static final long serialVersionUID = 1L;

    private List<UUID> hearingIds = new ArrayList<>();

    @Override
    public Object apply(Object event) {

        return match(event)
                .with(
                        when(RegisteredHearingAgainstDefendant.class).apply(defendant -> hearingIds.add(defendant.getHearingId())),
                        otherwiseDoNothing()
                );
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

        final CaseDefendantDetailsWithHearings caseDefendantDetailsWithHearings = CaseDefendantDetailsWithHearings.caseDefendantDetailsWithHearings()
                .setCaseId(caseDefendantDetails.getCaseId())
                .setDefendant(caseDefendantDetails.getDefendant())
                .setHearingIds(hearingIds);

        return apply(Stream.of(caseDefendantDetailsWithHearings));
    }

    public Stream<Object> lookupHearingsForNewOffenceOnDefendant(UUID defendantId, UUID caseId, DefendantOffence offence) {
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