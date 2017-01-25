package uk.gov.moj.cpp.hearing.domain.aggregate;

import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.match;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.otherwiseDoNothing;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.when;

import uk.gov.justice.domain.aggregate.Aggregate;
import uk.gov.moj.cpp.hearing.domain.command.AddCaseToHearing;
import uk.gov.moj.cpp.hearing.domain.command.AllocateCourt;
import uk.gov.moj.cpp.hearing.domain.command.BookRoom;
import uk.gov.moj.cpp.hearing.domain.command.EndHearing;
import uk.gov.moj.cpp.hearing.domain.command.InitiateHearing;
import uk.gov.moj.cpp.hearing.domain.command.StartHearing;
import uk.gov.moj.cpp.hearing.domain.event.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

public class HearingAggregate implements Aggregate {

    private UUID hearingId;

    public Stream<Object> initiateHearing(InitiateHearing initiateHearing) {
        Stream.Builder<Object> streamBuilder = Stream.builder();
        HearingInitiated hearingInitiated = new HearingInitiated(
                initiateHearing.getHearingId(),
                initiateHearing.getStartDateTime(),
                initiateHearing.getDuration(),initiateHearing.getHearingType());

        streamBuilder.add(hearingInitiated);
        if (null != initiateHearing.getCaseId()) {
            streamBuilder.add(new CaseAssociated(
                    initiateHearing.getHearingId(),
                    initiateHearing.getCaseId()));
        }
        if (null != initiateHearing.getCourtCentreName()) {
            streamBuilder.add(new CourtAssigned(
                    initiateHearing.getHearingId(),
                    initiateHearing.getCourtCentreName()));
        }
        if (null != initiateHearing.getRoomName()) {
            streamBuilder.add(new RoomBooked(
                    initiateHearing.getHearingId(),
                    initiateHearing.getRoomName()));
        }
        return streamBuilder.build();
    }

    public Stream<Object> allocateCourt(AllocateCourt allocateCourt) {
        return Stream.of(new CourtAssigned(
                allocateCourt.getHearingId(),
                allocateCourt.getCourtCentreName()));
    }

    public Stream<Object> bookRoom(BookRoom bookRoom) {
        return Stream.of(new RoomBooked(
                bookRoom.getHearingId(),
                bookRoom.getRoomName()));
    }

    public Stream<Object> startHearing(StartHearing startHearing) {
        return Stream.of(new HearingStarted(
                startHearing.getHearingId(),
                startHearing.getStartTime()));
    }

    public Stream<Object> adjournHearingDate(UUID hearingId, LocalDate startDate) {
        return Stream.of(new HearingAdjournDateUpdated(hearingId,startDate));
    }

    public Stream<Object> addCaseToHearing(AddCaseToHearing addCaseToHearing) {
        return Stream.of(new CaseAssociated(
                addCaseToHearing.getHearingId(),
                addCaseToHearing.getCaseId()));
    }

    public Stream<Object> endHearing(EndHearing endHearing) {
        return Stream.of(new HearingEnded(
                endHearing.getHearingId(),
                endHearing.getEndTime()));
    }

    public Stream<Object> addProsecutionCounsel(final UUID hearingId, final UUID attendeeId, final UUID personId, final String status) {
        return Stream.of(new ProsecutionCounselAdded(hearingId, attendeeId, personId, status));
    }

    public Stream<Object> addDefenceCounsel(final UUID hearingId, final UUID attendeeId,
            final UUID personId, final List<UUID> defendantIds, final String status) {
        return Stream.of(new DefenceCounselAdded(hearingId, attendeeId, personId, defendantIds, status));
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
