package uk.gov.moj.cpp.hearing.domain.aggregate.hearing;

import static java.util.stream.Stream.empty;

import uk.gov.justice.json.schemas.core.Offence;
import uk.gov.moj.cpp.hearing.domain.event.OffenceAdded;
import uk.gov.moj.cpp.hearing.domain.event.OffenceDeleted;
import uk.gov.moj.cpp.hearing.domain.event.OffenceUpdated;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

public class OffenceDelegate implements Serializable {

    private static final long serialVersionUID = 1L;

    private final HearingAggregateMomento momento;

    public OffenceDelegate(final HearingAggregateMomento momento) {
        this.momento = momento;
    }

    public void handleOffenceAdded(final OffenceAdded offenceAdded) {
        this.momento.getHearing().getProsecutionCases().stream()
                .flatMap(prosecutionCase -> prosecutionCase.getDefendants().stream())
                .filter(defendant -> defendant.getId().equals(offenceAdded.getDefendantId()))
                .forEach(defendant -> defendant.getOffences().add(offenceAdded.getOffence()));
    }

    public void handleOffenceUpdated(final OffenceUpdated offenceUpdated) {
        final UUID offenceId = offenceUpdated.getOffence().getId();
        this.momento.getHearing().getProsecutionCases().stream()
                .flatMap(prosecutionCase -> prosecutionCase.getDefendants().stream())
                .forEach(defendant -> {
                        final List<Offence> offences = defendant.getOffences();
                        if (offences.removeIf(offence -> offence.getId().equals(offenceId))) {
                            offences.add(offenceUpdated.getOffence());
                        }
                });
    }

    public void handleOffenceDeleted(final OffenceDeleted offenceDeleted) {
        final UUID offenceId = offenceDeleted.getId();
        this.momento.getHearing().getProsecutionCases().stream()
                .flatMap(prosecutionCase -> prosecutionCase.getDefendants().stream())
                .forEach(defendant -> defendant.getOffences().removeIf(offence -> offence.getId().equals(offenceId)));
    }

    public Stream<Object> addOffence(final UUID hearingId, final UUID defendantId, final UUID prosecutionCaseId, 
            final Offence offence) {
        if (this.momento.isPublished()) {
            return empty();
        }
        return Stream.of(OffenceAdded.offenceAdded()
                .withHearingId(hearingId)
                .withDefendantId(defendantId)
                .withProsecutionCaseId(prosecutionCaseId)
                .withOffence(offence));
    }

    public Stream<Object> updateOffence(final UUID hearingId, final UUID defendantId, final Offence offence) {
        if (this.momento.isPublished()) {
            return empty();
        }
        return Stream.of(OffenceUpdated.offenceUpdated()
                .withHearingId(hearingId)
                .withDefendantId(defendantId)
                .withOffence(offence)
                .withPlea(this.momento.getPleas().get(offence.getId()))
                .withVerdict(this.momento.getVerdicts().get(offence.getId()))
                .withConvictionDate(this.momento.getConvictionDates().get(offence.getId())));
    }

    public Stream<Object> deleteOffence(final UUID offenceId, final UUID hearingId) {
        if (this.momento.isPublished()) {
            return empty();
        }
        return Stream.of(OffenceDeleted.builder()
                .withId(offenceId)
                .withHearingId(hearingId)
                .build());
    }
}
