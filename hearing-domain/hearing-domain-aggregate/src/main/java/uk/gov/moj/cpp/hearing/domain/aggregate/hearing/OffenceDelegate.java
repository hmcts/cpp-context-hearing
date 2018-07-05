package uk.gov.moj.cpp.hearing.domain.aggregate.hearing;

import uk.gov.moj.cpp.hearing.command.initiate.Offence;
import uk.gov.moj.cpp.hearing.command.offence.UpdatedOffence;
import uk.gov.moj.cpp.hearing.domain.event.OffenceAdded;
import uk.gov.moj.cpp.hearing.domain.event.OffenceDeleted;
import uk.gov.moj.cpp.hearing.domain.event.OffenceUpdated;

import java.util.UUID;
import java.util.stream.Stream;

@SuppressWarnings("pmd:BeanMembersShouldSerialize")
public class OffenceDelegate {

    private final HearingAggregateMomento momento;

    public OffenceDelegate(final HearingAggregateMomento momento) {
        this.momento = momento;
    }

    public void handleOffenceAdded(OffenceAdded offenceAdded) {
        this.momento.getHearing().getDefendants().stream()
                .filter(d -> d.getId().equals(offenceAdded.getDefendantId()))
                .forEach(d -> d.getOffences().add(Offence.offence()
                        .setId(offenceAdded.getId())
                        .setCaseId(offenceAdded.getCaseId())
                        .setOffenceCode(offenceAdded.getOffenceCode())
                        .setWording(offenceAdded.getWording())
                        .setStartDate(offenceAdded.getStartDate())
                        .setEndDate(offenceAdded.getEndDate())
                        .setCount(offenceAdded.getCount())
                        .setConvictionDate(offenceAdded.getConvictionDate())
                ));
    }

    public void handleOffenceUpdated(OffenceUpdated offenceUpdated) {
        this.momento.getHearing().getDefendants().forEach(d -> d.getOffences().stream()
                .filter(o -> o.getId().equals(offenceUpdated.getId()))
                .forEach(o -> {
                    o.setOffenceCode(offenceUpdated.getOffenceCode());
                    o.setWording(offenceUpdated.getWording());
                    o.setStartDate(offenceUpdated.getStartDate());
                    o.setEndDate(offenceUpdated.getEndDate());
                    o.setCount(offenceUpdated.getCount());
                    o.setConvictionDate(offenceUpdated.getConvictionDate());
                }));
    }

    public void handleOffenceDeleted(OffenceDeleted offenceDeleted) {
        this.momento.getHearing().getDefendants()
                .forEach(d -> d.getOffences().removeIf(o -> o.getId().equals(offenceDeleted.getId())));
    }


    public Stream<Object> addOffence(final UUID hearingId, final UUID defendantId, final UUID caseId, final UpdatedOffence offence) {

        if (!this.momento.isPublished()) {
            return Stream.of(OffenceAdded.builder()
                    .withId(offence.getId())
                    .withHearingId(hearingId)
                    .withDefendantId(defendantId)
                    .withCaseId(caseId)
                    .withOffenceCode(offence.getOffenceCode())
                    .withWording(offence.getWording())
                    .withStartDate(offence.getStartDate())
                    .withEndDate(offence.getEndDate())
                    .withCount(offence.getCount())
                    .withConvictionDate(offence.getConvictionDate())
                    .build());
        }

        return Stream.empty();
    }

    public Stream<Object> updateOffence(final UUID hearingId, final UpdatedOffence offence) {

        if (!this.momento.isPublished()) {
            return Stream.of(OffenceUpdated.builder()
                    .withHearingId(hearingId)
                    .withId(offence.getId())
                    .withOffenceCode(offence.getOffenceCode())
                    .withWording(offence.getWording())
                    .withStartDate(offence.getStartDate())
                    .withEndDate(offence.getEndDate())
                    .withCount(offence.getCount())
                    .withConvictionDate(offence.getConvictionDate())
                    .build());
        }

        return Stream.empty();
    }

    public Stream<Object> deleteOffence(final UUID offenceId, final UUID hearingId) {

        if (!this.momento.isPublished()) {
            return Stream.of(OffenceDeleted.builder()
                    .withId(offenceId)
                    .withHearingId(hearingId)
                    .build());
        }

        return Stream.empty();
    }
}
