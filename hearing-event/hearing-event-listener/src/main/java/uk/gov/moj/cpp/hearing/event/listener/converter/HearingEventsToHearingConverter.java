package uk.gov.moj.cpp.hearing.event.listener.converter;

import uk.gov.moj.cpp.hearing.domain.event.CaseAssociated;
import uk.gov.moj.cpp.hearing.domain.event.CourtAssigned;
import uk.gov.moj.cpp.hearing.domain.event.HearingEnded;
import uk.gov.moj.cpp.hearing.domain.event.HearingInitiated;
import uk.gov.moj.cpp.hearing.domain.event.HearingStarted;
import uk.gov.moj.cpp.hearing.domain.event.ProsecutionCounselAdded;
import uk.gov.moj.cpp.hearing.domain.event.RoomBooked;
import uk.gov.moj.cpp.hearing.persist.entity.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.HearingCase;
import uk.gov.moj.cpp.hearing.persist.entity.ProsecutionCounsel;

import java.util.UUID;

public class HearingEventsToHearingConverter {

    public Hearing convert(HearingInitiated hearingInitiated) {
        final Hearing hearing = new Hearing();
        hearing.setHearingId(hearingInitiated.getHearingId());
        hearing.setStartdate(hearingInitiated.getStartDateTime().toLocalDate());
        hearing.setStartTime(hearingInitiated.getStartDateTime().toLocalTime());
        hearing.setDuration(hearingInitiated.getDuration());
        hearing.setHearingType(hearingInitiated.getHearingType());
        return hearing;
    }

    public Hearing convert(CourtAssigned courtAssigned) {
        final Hearing hearing = new Hearing();
        hearing.setHearingId(courtAssigned.getHearingId());
        hearing.setCourtCentreName(courtAssigned.getCourtCentreName());
        return hearing;
    }

    public Hearing convert(RoomBooked roomBooked) {
        final Hearing hearing = new Hearing();
        hearing.setHearingId(roomBooked.getHearingId());
        hearing.setRoomName(roomBooked.getRoomName());
        return hearing;
    }

    public Hearing convert(HearingStarted hearingStarted) {
        final Hearing hearing = new Hearing();
        hearing.setHearingId(hearingStarted.getHearingId());
        hearing.setStartedAt(hearingStarted.getStartTime());
        return hearing;
    }

    public Hearing convert(HearingEnded hearingEnded) {
        final Hearing hearing = new Hearing();
        hearing.setHearingId(hearingEnded.getHearingId());
        hearing.setEndedAt(hearingEnded.getEndTime());
        return hearing;
    }

    public HearingCase convert(CaseAssociated caseAssociated) {
        final HearingCase hearingCase = new HearingCase();
        hearingCase.setId(UUID.randomUUID());
        hearingCase.setHearingId(caseAssociated.getHearingId());
        hearingCase.setCaseId(caseAssociated.getCaseId());
        return hearingCase;
    }

    public ProsecutionCounsel convert(final ProsecutionCounselAdded prosecutionCounselAdded) {
        final ProsecutionCounsel prosecutionCounsel =
                new ProsecutionCounsel(prosecutionCounselAdded.getAttendeeId(),
                        prosecutionCounselAdded.getHearingId(),
                        prosecutionCounselAdded.getPersonId(),
                        prosecutionCounselAdded.getStatus());
        return prosecutionCounsel;
    }
}
