package uk.gov.moj.cpp.hearing.domain.aggregate.hearing;

import uk.gov.moj.cpp.hearing.command.offence.BaseDefendantOffence;
import uk.gov.moj.cpp.hearing.domain.event.OffenceAdded;
import uk.gov.moj.cpp.hearing.domain.event.OffenceDeleted;
import uk.gov.moj.cpp.hearing.domain.event.OffenceUpdated;

import java.io.Serializable;
import java.util.UUID;
import java.util.stream.Stream;

public class OffenceDelegate implements Serializable {

    private static final long serialVersionUID = 1L;

    private final HearingAggregateMomento momento;

    public OffenceDelegate(final HearingAggregateMomento momento) {
        this.momento = momento;
    }

    public void handleOffenceAdded(OffenceAdded offenceAdded) {

        this.momento.getHearing().getProsecutionCases().forEach(prosecutionCase ->
                prosecutionCase.getDefendants().stream()
                .filter(defendant -> defendant.getId().equals(offenceAdded.getDefendantId()))
                .forEach(defendant -> defendant.getOffences().add(
                        uk.gov.justice.json.schemas.core.Offence.offence()
                                .withId(offenceAdded.getId())
                                .withOffenceCode(offenceAdded.getOffenceCode())
                                .withWording(offenceAdded.getWording())
                                .withStartDate(offenceAdded.getStartDate())
                                .withCount(offenceAdded.getCount())
                                .withOffenceDefinitionId(UUID.randomUUID())//TODO: the offence definition is missing in Offence Added
                                .withOrderIndex(0)//TODO: the offence definition is missing in Offence Added
                                .withConvictionDate(offenceAdded.getConvictionDate())
                                .build()
                )));
    }

    public void handleOffenceUpdated(OffenceUpdated offenceUpdated) {

        this.momento.getHearing().getProsecutionCases().forEach(prosecutionCase ->
                prosecutionCase.getDefendants().forEach(defendant -> defendant.getOffences().stream()
                .filter(offence -> offence.getId().equals(offenceUpdated.getId()))
                .forEach(offence -> {
                    offence.setOffenceCode(offenceUpdated.getOffenceCode());
                    offence.setWording(offenceUpdated.getWording());
                    offence.setStartDate(offenceUpdated.getStartDate());
                    offence.setEndDate(offenceUpdated.getEndDate());
                    offence.setCount(offenceUpdated.getCount());
                    offence.setConvictionDate(offenceUpdated.getConvictionDate());
                })));
    }

    public void handleOffenceDeleted(OffenceDeleted offenceDeleted) {
        this.momento.getHearing().getProsecutionCases().forEach(prosecutionCase ->
                prosecutionCase.getDefendants().forEach(defendant ->
                        defendant.getOffences().removeIf(offence -> offence.getId().equals(offenceDeleted.getId()))));
    }


    public Stream<Object> addOffence(final UUID hearingId, final UUID defendantId, final UUID caseId, final BaseDefendantOffence offence) {
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

    public Stream<Object> updateOffence(final UUID hearingId, final BaseDefendantOffence offence) {
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
