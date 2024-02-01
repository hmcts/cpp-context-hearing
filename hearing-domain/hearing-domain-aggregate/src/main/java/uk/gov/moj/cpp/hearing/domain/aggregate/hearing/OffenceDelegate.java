package uk.gov.moj.cpp.hearing.domain.aggregate.hearing;

import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.empty;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

import uk.gov.justice.core.courts.CustodyTimeLimit;
import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.Offence;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.moj.cpp.hearing.domain.event.CustodyTimeLimitClockStopped;
import uk.gov.moj.cpp.hearing.domain.event.CustodyTimeLimitExtended;
import uk.gov.moj.cpp.hearing.domain.event.ExistingHearingUpdated;
import uk.gov.moj.cpp.hearing.domain.event.HearingDeleted;
import uk.gov.moj.cpp.hearing.domain.event.OffenceAdded;
import uk.gov.moj.cpp.hearing.domain.event.OffenceDeleted;
import uk.gov.moj.cpp.hearing.domain.event.OffenceUpdated;
import uk.gov.moj.cpp.hearing.domain.event.OffencesRemovedFromExistingHearing;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OffenceDelegate implements Serializable {

    private static final long serialVersionUID = 3L;

    private final HearingAggregateMomento momento;

    public OffenceDelegate(final HearingAggregateMomento momento) {
        this.momento = momento;
    }

    public void handleOffenceAdded(final OffenceAdded offenceAdded) {
        final Optional<Offence> offenceInHearing = this.momento.getHearing().getProsecutionCases().stream()
                .flatMap(prosecutionCase -> prosecutionCase.getDefendants().stream())
                .flatMap(defendant -> defendant.getOffences().stream())
                .filter(offence1 -> offence1.getId().equals(offenceAdded.getOffence().getId()))
                .findAny();

        if (!offenceInHearing.isPresent()) {
            this.momento.getHearing().getProsecutionCases().stream()
                    .flatMap(prosecutionCase -> prosecutionCase.getDefendants().stream())
                    .filter(defendant -> defendant.getId().equals(offenceAdded.getDefendantId()))
                    .forEach(defendant -> defendant.getOffences().add(offenceAdded.getOffence()));
        }
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

    @SuppressWarnings("squid:S1612")
    public void handleExistingHearingUpdated(ExistingHearingUpdated existingHearingUpdated) {
        existingHearingUpdated.getProsecutionCases().forEach(prosecutionCase ->
                addProsecutionCaseToMomentoHearing(prosecutionCase));
    }

    private void addProsecutionCaseToMomentoHearing(final ProsecutionCase prosecutionCase) {
        final Optional<ProsecutionCase> prosecutionCaseInAggregate = this.momento.getHearing().getProsecutionCases().stream()
                .filter(pc -> pc.getId().equals(prosecutionCase.getId()))
                .findFirst();

        if (prosecutionCaseInAggregate.isPresent()) {
            prosecutionCase.getDefendants().forEach(defendant ->
                    addDefendantToMomentoHearing(prosecutionCaseInAggregate.get(), defendant));
        } else {
            momento.getHearing().getProsecutionCases().add(prosecutionCase);
        }
    }

    private void addDefendantToMomentoHearing(final ProsecutionCase prosecutionCaseInAggregate, final Defendant defendant) {
        final Optional<Defendant> defendantInAggregate = prosecutionCaseInAggregate.getDefendants().stream()
                .filter(d -> d.getId().equals(defendant.getId()))
                .findFirst();
        if (defendantInAggregate.isPresent()) {
            defendant.getOffences().forEach(offence -> addOffenceToMomentoHearing(defendantInAggregate.get(), offence));
        } else {
            prosecutionCaseInAggregate.getDefendants().add(defendant);
        }
    }

    private void addOffenceToMomentoHearing(final Defendant defendantInAggregate, final Offence offence) {
        final Optional<Offence> offenceInAggregate = defendantInAggregate.getOffences().stream()
                .filter(o -> o.getId().equals(offence.getId()))
                .findFirst();
        if (!offenceInAggregate.isPresent()) {
            defendantInAggregate.getOffences().add(offence);
        }
    }

    public void handleOffencesRemovedFromExistingHearing(final OffencesRemovedFromExistingHearing offencesRemovedFromExistingHearing) {

        final List<UUID> offencesToBeRemoved = offencesRemovedFromExistingHearing.getOffenceIds();
        final List<UUID> defendantsToBeRemoved = offencesRemovedFromExistingHearing.getDefendantIds();
        final List<UUID> prosecutionCasesToBeRemoved = offencesRemovedFromExistingHearing.getProsecutionCaseIds();

        // Remove offences from all defendants
        momento.getHearing().getProsecutionCases().forEach(
                prosecutionCase -> prosecutionCase.getDefendants().forEach(
                        defendant -> defendant.getOffences()
                                .removeIf(offence -> offencesToBeRemoved.contains(offence.getId()))
                )
        );

        // Remove defendants with no offences from all prosecution cases
        momento.getHearing().getProsecutionCases().forEach(
                prosecutionCase -> prosecutionCase.getDefendants()
                        .removeIf(defendant -> defendantsToBeRemoved.contains(defendant.getId()))
        );

        // Remove prosecution cases with no defendants
        momento.getHearing().getProsecutionCases()
                .removeIf(prosecutionCase -> prosecutionCasesToBeRemoved.contains(prosecutionCase.getId()));

    }

    public Stream<Object> addOffence(final UUID hearingId, final UUID defendantId, final UUID prosecutionCaseId,
                                     final Offence offence) {
        if (this.momento.isPublished()) {
            return empty();
        }

        final Optional<Offence> offenceInHearing = this.momento.getHearing().getProsecutionCases().stream()
                .flatMap(prosecutionCase -> prosecutionCase.getDefendants().stream())
                .flatMap(defendant -> defendant.getOffences().stream())
                .filter(offence1 -> offence1.getId().equals(offence.getId()))
                .findAny();

        if (offenceInHearing.isPresent()) {
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

    public Stream<Object> removeOffencesFromAllocatedHearing(final UUID hearingId, final List<UUID> offenceIds, final String source) {

        if (this.momento.isPublished()) {
            return empty();
        }

        final List<UUID> existingOffenceIds = ofNullable(momento.getHearing()).map(hearing -> hearing.getProsecutionCases().stream()).orElseGet(Stream::empty)
                .flatMap(prosecutionCase -> prosecutionCase.getDefendants().stream())
                .flatMap(defendant -> defendant.getOffences().stream())
                .map(Offence::getId)
                .filter(offenceIds::contains)
                .collect(toList());

        if(existingOffenceIds.isEmpty()){
            return Stream.empty();
        }

        final List<UUID> defendantsToBeRemoved = momento.getHearing().getProsecutionCases()
                .stream()
                .flatMap(prosecutionCase -> prosecutionCase.getDefendants().stream())
                .filter(defendant -> defendant
                        .getOffences()
                        .stream()
                        .filter(offence -> !offenceIds.contains(offence.getId()))
                        .collect(toList()).isEmpty())
                .map(Defendant::getId)
                .collect(toList());

        final List<UUID> prosecutionCasesToBeRemoved = momento.getHearing().getProsecutionCases()
                .stream()
                .filter(prosecutionCase -> prosecutionCase
                        .getDefendants()
                        .stream().filter(defendant -> !defendantsToBeRemoved.contains(defendant.getId()))
                        .collect(toList()).isEmpty())
                .map(ProsecutionCase::getId)
                .collect(Collectors.toList());

        final List<UUID> prosecutionCaseRemainingList = ofNullable(momento.getHearing()).map(hearing -> hearing.getProsecutionCases().stream()).orElseGet(Stream::empty)
                .map(prosecutionCase -> prosecutionCase.getId())
                .filter(caseId -> !prosecutionCasesToBeRemoved.contains(caseId))
                .collect(toList());

        if(prosecutionCaseRemainingList.isEmpty()) {
            return Stream.of(new HearingDeleted(prosecutionCasesToBeRemoved, defendantsToBeRemoved, offenceIds, null, hearingId));
        }


        return Stream.of(new OffencesRemovedFromExistingHearing(hearingId, prosecutionCasesToBeRemoved, defendantsToBeRemoved, offenceIds, source));

    }

    public void handleCustodyTimeLimitClockStopped(final CustodyTimeLimitClockStopped event) {
        final List<UUID> offenceIds = event.getOffenceIds();
        this.momento.getHearing().getProsecutionCases().stream()
                .flatMap(prosecutionCase -> prosecutionCase.getDefendants().stream())
                .flatMap(defendant -> defendant.getOffences().stream())
                .forEach(offence -> {
                    if (offenceIds.contains(offence.getId())) {
                        offence.setCustodyTimeLimit(null);
                        offence.setCtlClockStopped(true);
                    }
                });
    }

    public void handleCustodyTimeLimitExtended(final CustodyTimeLimitExtended event) {

        final UUID offenceId = event.getOffenceId();

        if (momento.getHearing() != null && isNotEmpty(momento.getHearing().getProsecutionCases())) {
            this.momento.getHearing().getProsecutionCases().stream()
                    .flatMap(prosecutionCase -> prosecutionCase.getDefendants().stream())
                    .flatMap(defendant -> defendant.getOffences().stream())
                    .forEach(offence -> {
                        if (offenceId.equals(offence.getId())) {
                            if (nonNull(offence.getCustodyTimeLimit())) {
                                offence.getCustodyTimeLimit().setTimeLimit(event.getExtendedTimeLimit());
                                offence.getCustodyTimeLimit().setIsCtlExtended(true);
                            } else {
                                offence.setCustodyTimeLimit(CustodyTimeLimit.custodyTimeLimit()
                                        .withTimeLimit(event.getExtendedTimeLimit())
                                        .withIsCtlExtended(true)
                                        .build());
                            }

                        }
                    });
        }
    }

}
