package uk.gov.moj.cpp.hearing.domain.aggregate;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.match;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.otherwiseDoNothing;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.when;

import uk.gov.justice.domain.aggregate.Aggregate;
import uk.gov.moj.cpp.hearing.domain.HearingDetails;
import uk.gov.moj.cpp.hearing.domain.ResultLine;
import uk.gov.moj.cpp.hearing.domain.event.CaseAssociated;
import uk.gov.moj.cpp.hearing.domain.event.CourtAssigned;
import uk.gov.moj.cpp.hearing.domain.event.DefenceCounselAdded;
import uk.gov.moj.cpp.hearing.domain.event.HearingAdjournDateUpdated;
import uk.gov.moj.cpp.hearing.domain.event.HearingInitiated;
import uk.gov.moj.cpp.hearing.domain.event.ProsecutionCounselAdded;
import uk.gov.moj.cpp.hearing.domain.event.ResultAmended;
import uk.gov.moj.cpp.hearing.domain.event.ResultsShared;
import uk.gov.moj.cpp.hearing.domain.event.RoomBooked;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

public class HearingAggregate implements Aggregate {

    private UUID hearingId;
    private boolean resultsShared;
    private final Set<UUID> sharedResultIds = new HashSet<>();

    public Stream<Object> initiateHearing(final HearingDetails hearingDetails) {
        final Stream.Builder<Object> streamBuilder = Stream.builder();

        streamBuilder.add(new HearingInitiated(hearingDetails.getHearingId(), hearingDetails.getStartDateTime(), hearingDetails.getDuration(), hearingDetails.getHearingType()));

        if (hearingDetails.getCaseId() != null) {
            streamBuilder.add(new CaseAssociated(hearingDetails.getHearingId(), hearingDetails.getCaseId()));
        }

        if (hearingDetails.getCourtCentreName() != null) {
            streamBuilder.add(new CourtAssigned(hearingDetails.getHearingId(), hearingDetails.getCourtCentreId(), hearingDetails.getCourtCentreName()));
        }

        if (hearingDetails.getRoomName() != null) {
            streamBuilder.add(new RoomBooked(hearingDetails.getHearingId(), hearingDetails.getRoomId(), hearingDetails.getRoomName()));
        }

        return apply(streamBuilder.build());
    }

    public Stream<Object> allocateCourt(final UUID hearingId, final String courtCentreName) {
        return apply(Stream.of(new CourtAssigned(hearingId, courtCentreName)));
    }

    public Stream<Object> bookRoom(final UUID hearingId, final String roomName) {
        return apply(Stream.of(new RoomBooked(hearingId, roomName)));
    }

    public Stream<Object> adjournHearingDate(final UUID hearingId, final LocalDate startDate) {
        return apply(Stream.of(new HearingAdjournDateUpdated(hearingId, startDate)));
    }

    public Stream<Object> addCaseToHearing(final UUID hearingId, final UUID caseId) {
        return apply(Stream.of(new CaseAssociated(hearingId, caseId)));
    }

    public Stream<Object> addProsecutionCounsel(final UUID hearingId, final UUID attendeeId,
                                                final UUID personId, final String status) {
        return apply(Stream.of(new ProsecutionCounselAdded(hearingId, attendeeId, personId, status)));
    }

    public Stream<Object> addDefenceCounsel(final UUID hearingId, final UUID attendeeId,
                                            final UUID personId, final List<UUID> defendantIds, final String status) {
        return apply(Stream.of(new DefenceCounselAdded(hearingId, attendeeId, personId, defendantIds, status)));
    }

    public Stream<Object> shareResults(final UUID hearingId, final ZonedDateTime sharedTime, final List<ResultLine> resultLines) {
        final LinkedList<Object> events = new LinkedList<>();

        if (resultsShared) {
            events.addAll(resultLines.stream()
                    .filter(resultLine -> !sharedResultIds.contains(resultLine.getLastSharedResultId())
                            ||
                            !sharedResultIds.contains(resultLine.getId()))
                    .map(resultLine -> new ResultAmended(resultLine.getId(), resultLine.getLastSharedResultId(),
                            sharedTime, hearingId, resultLine.getCaseId(), resultLine.getPersonId(), resultLine.getOffenceId(),
                            resultLine.getLevel(), resultLine.getResultLabel(), resultLine.getPrompts(), resultLine.getCourt(), resultLine.getCourtRoom(), resultLine.getClerkOfTheCourtId(), resultLine.getClerkOfTheCourtFirstName(), resultLine.getClerkOfTheCourtLastName())
                    )
                    .collect(toList()));
        } else {
            events.add(new ResultsShared(hearingId, sharedTime, resultLines));
        }

        return apply(events.stream());
    }

    @Override
    public Object apply(final Object event) {
        return match(event).with(
                when(HearingInitiated.class)
                        .apply(hearingInitiated -> this.hearingId = hearingInitiated.getHearingId()),
                when(CourtAssigned.class)
                        .apply(courtAssigned -> this.hearingId = courtAssigned.getHearingId()),
                when(RoomBooked.class)
                        .apply(roomBooked -> this.hearingId = roomBooked.getHearingId()),
                when(CaseAssociated.class)
                        .apply(caseAssociated -> this.hearingId = caseAssociated.getHearingId()),
                when(ProsecutionCounselAdded.class)
                        .apply(prosecutionCounselAdded -> this.hearingId = prosecutionCounselAdded.getHearingId()),
                when(DefenceCounselAdded.class)
                        .apply(defenceCounselAdded -> this.hearingId = defenceCounselAdded.getHearingId()),
                when(ResultsShared.class)
                        .apply(resultsSharedResult -> recordSharedResults(resultsSharedResult.getResultLines())),
                when(ResultAmended.class)
                        .apply(this::recordAmendedResult),
                otherwiseDoNothing()
        );
    }

    private void recordAmendedResult(final ResultAmended resultAmended) {
        sharedResultIds.add(resultAmended.getId());
    }

    private void recordSharedResults(final List<ResultLine> resultLines) {
        resultsShared = true;
        sharedResultIds.addAll(resultLines.stream().map(ResultLine::getId).collect(toSet()));
    }
}
