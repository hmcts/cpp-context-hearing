package uk.gov.moj.cpp.hearing.domain.aggregate;

import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.match;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.otherwiseDoNothing;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.when;

import uk.gov.justice.domain.aggregate.Aggregate;
import uk.gov.moj.cpp.hearing.domain.event.CaseAssociated;
import uk.gov.moj.cpp.hearing.domain.event.CourtAssigned;
import uk.gov.moj.cpp.hearing.domain.event.DefenceCounselAdded;
import uk.gov.moj.cpp.hearing.domain.event.HearingAdjournDateUpdated;
import uk.gov.moj.cpp.hearing.domain.event.HearingEnded;
import uk.gov.moj.cpp.hearing.domain.event.HearingInitiated;
import uk.gov.moj.cpp.hearing.domain.event.HearingStarted;
import uk.gov.moj.cpp.hearing.domain.event.ProsecutionCounselAdded;
import uk.gov.moj.cpp.hearing.domain.event.RoomBooked;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

public class HearingAggregate implements Aggregate {

    private UUID hearingId;

    public Stream<Object> initiateHearing(final UUID hearingId, final ZonedDateTime startDateTime,
                                          final int duration, final String hearingType, final String courtCentreName,
                                          final String roomName, final UUID caseId) {
        final Stream.Builder<Object> streamBuilder = Stream.builder();

        streamBuilder.add(new HearingInitiated(hearingId, startDateTime, duration, hearingType));

        if (caseId != null) {
            streamBuilder.add(new CaseAssociated(hearingId, caseId));
        }

        if (courtCentreName != null) {
            streamBuilder.add(new CourtAssigned(hearingId, courtCentreName));
        }

        if (roomName != null) {
            streamBuilder.add(new RoomBooked(hearingId, roomName));
        }

        return apply(streamBuilder.build());
    }

    public Stream<Object> allocateCourt(final UUID hearingId, final String courtCentreName) {
        return apply(Stream.of(new CourtAssigned(hearingId, courtCentreName)));
    }

    public Stream<Object> bookRoom(final UUID hearingId, final String roomName) {
        return apply(Stream.of(new RoomBooked(hearingId, roomName)));
    }

    public Stream<Object> startHearing(final UUID hearingId, final ZonedDateTime startTime) {
        return apply(Stream.of(new HearingStarted(hearingId, startTime)));
    }

    public Stream<Object> adjournHearingDate(final UUID hearingId, final LocalDate startDate) {
        return apply(Stream.of(new HearingAdjournDateUpdated(hearingId, startDate)));
    }

    public Stream<Object> addCaseToHearing(final UUID hearingId, final UUID caseId) {
        return apply(Stream.of(new CaseAssociated(hearingId, caseId)));
    }

    public Stream<Object> endHearing(final UUID hearingId, final ZonedDateTime endTime) {
        return apply(Stream.of(new HearingEnded(hearingId, endTime)));
    }

    public Stream<Object> addProsecutionCounsel(final UUID hearingId, final UUID attendeeId,
                                                final UUID personId, final String status) {
        return apply(Stream.of(new ProsecutionCounselAdded(hearingId, attendeeId, personId, status)));
    }

    public Stream<Object> addDefenceCounsel(final UUID hearingId, final UUID attendeeId,
                                            final UUID personId, final List<UUID> defendantIds, final String status) {
        return apply(Stream.of(new DefenceCounselAdded(hearingId, attendeeId, personId, defendantIds, status)));
    }

    @Override
    public Object apply(Object event) {
        return match(event).with(
                when(HearingInitiated.class)
                        .apply(hearingInitiated -> this.hearingId = hearingInitiated.getHearingId()),
                when(CourtAssigned.class)
                        .apply(courtAssigned -> this.hearingId = courtAssigned.getHearingId()),
                when(RoomBooked.class)
                        .apply(roomBooked -> this.hearingId = roomBooked.getHearingId()),
                when(HearingEnded.class)
                        .apply(hearingEnded -> this.hearingId = hearingEnded.getHearingId()),
                when(CaseAssociated.class)
                        .apply(caseAssociated -> this.hearingId = caseAssociated.getHearingId()),
                when(HearingStarted.class)
                        .apply(hearingStarted -> this.hearingId = hearingStarted.getHearingId()),
                when(ProsecutionCounselAdded.class)
                        .apply(prosecutionCounselAdded -> this.hearingId = prosecutionCounselAdded.getHearingId()),
                when(DefenceCounselAdded.class)
                        .apply(defenceCounselAdded -> this.hearingId = defenceCounselAdded.getHearingId()),
                otherwiseDoNothing()
        );
    }
}
