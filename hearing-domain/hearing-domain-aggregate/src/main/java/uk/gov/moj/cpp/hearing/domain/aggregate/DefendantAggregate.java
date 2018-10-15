package uk.gov.moj.cpp.hearing.domain.aggregate;

import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.match;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.otherwiseDoNothing;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.when;

import uk.gov.justice.domain.aggregate.Aggregate;
import uk.gov.justice.json.schemas.core.Offence;
import uk.gov.moj.cpp.hearing.command.defendant.Defendant;
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

    public Stream<Object> registerHearing(final UUID defendantId, final UUID hearingId) {
        return apply(Stream.of(
                RegisteredHearingAgainstDefendant.builder()
                        .withDefendantId(defendantId)
                        .withHearingId(hearingId)
                        .build()
        ));
    }

    public Stream<Object> enrichCaseDefendantDetailsWithHearingIds(final Defendant defendant) {

        final CaseDefendantDetailsWithHearings caseDefendantDetailsWithHearings =
                CaseDefendantDetailsWithHearings.caseDefendantDetailsWithHearings()
                        .setDefendant(defendant)
                        .setHearingIds(hearingIds);

        return apply(Stream.of(caseDefendantDetailsWithHearings));
    }

    public Stream<Object> lookupHearingsForNewOffenceOnDefendant(final UUID defendantId, final UUID prosecutionCaseId, final Offence offence) {
        return apply(Stream.of(FoundHearingsForNewOffence.foundHearingsForNewOffence()
                .withDefendantId(defendantId)
                .withProsecutionCaseId(prosecutionCaseId)
                .withOffence(offence)
                .withHearingIds(hearingIds)
        ));
    }
}