package uk.gov.moj.cpp.hearing.query.view.convertor;

import uk.gov.moj.cpp.hearing.persist.entity.Hearing;
import uk.gov.moj.cpp.hearing.query.view.response.HearingView;

public class HearingEntityToHearing {
    private HearingEntityToHearing() {
        // Private constructor
    }

    public static HearingView convert(final Hearing hearing) {
        final HearingView hearingView = new HearingView();
        hearingView.setHearingId(hearing.getHearingId().toString());
        hearingView.setDuration(hearing.getDuration());
        hearingView.setStartDate(hearing.getStartDate());
        hearingView.setStartTime(hearing.getStartTime());
        hearingView.setCourtCentreName(hearing.getCourtCentreName());
        if (hearing.getCourtCentreId() != null) {
            hearingView.setCourtCentreId(hearing.getCourtCentreId().toString());
        }
        hearingView.setRoomName(hearing.getRoomName());
        if (hearing.getRoomId() != null) {
            hearingView.setRoomId(hearing.getRoomId().toString());
        }
        hearingView.setHearingType(hearing.getHearingType());
        return hearingView;
    }

}
