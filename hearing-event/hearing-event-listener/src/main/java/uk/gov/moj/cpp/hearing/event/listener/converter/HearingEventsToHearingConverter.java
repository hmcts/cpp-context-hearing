package uk.gov.moj.cpp.hearing.event.listener.converter;

import static java.util.UUID.randomUUID;

import uk.gov.moj.cpp.hearing.domain.event.*;
import uk.gov.moj.cpp.hearing.persist.entity.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.HearingCase;
import uk.gov.moj.cpp.hearing.persist.entity.ProsecutionCounsel;

public class HearingEventsToHearingConverter {

    public Hearing convert(HearingInitiated hearingInitiated) {
        return new Hearing(hearingInitiated.getHearingId(),
                hearingInitiated.getStartDateTime().toLocalDate(),
                hearingInitiated.getStartDateTime().toLocalTime(),
                hearingInitiated.getDuration(), null, hearingInitiated.getHearingType(),
                null, null, null);
    }

    public Hearing convert(CourtAssigned courtAssigned) {
        return new Hearing(courtAssigned.getHearingId(), null, null, null, null, null,
                courtAssigned.getCourtCentreName(), null, null);
    }

    public Hearing convert(RoomBooked roomBooked) {
        return new Hearing(roomBooked.getHearingId(), null, null, null,
                roomBooked.getRoomName(), null, null, null, null);
    }

    public Hearing convert(HearingStarted hearingStarted) {
        return new Hearing(hearingStarted.getHearingId(), null, null, null,
                null, null, null, hearingStarted.getStartTime(), null);
    }

    public Hearing convert(HearingAdjournDateUpdated hearingAdjournDateUpdated) {
        return new Hearing(hearingAdjournDateUpdated.getHearingId(), hearingAdjournDateUpdated.getStartDate(), null, null,
                null, null, null, null, null);
    }

    public Hearing convert(HearingEnded hearingEnded) {
        return new Hearing(hearingEnded.getHearingId(), null, null, null, null,
                null, null, null, hearingEnded.getEndTime());
    }

    public HearingCase convert(CaseAssociated caseAssociated) {
        return new HearingCase(randomUUID(), caseAssociated.getHearingId(),
                caseAssociated.getCaseId());
    }

    public ProsecutionCounsel convert(final ProsecutionCounselAdded prosecutionCounselAdded) {
        return new ProsecutionCounsel(prosecutionCounselAdded.getAttendeeId(),
                prosecutionCounselAdded.getHearingId(),
                prosecutionCounselAdded.getPersonId(),
                prosecutionCounselAdded.getStatus());
    }
}
