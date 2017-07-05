package uk.gov.moj.cpp.hearing.query.view.convertor;

import uk.gov.moj.cpp.hearing.persist.entity.Hearing;
import uk.gov.moj.cpp.hearing.query.view.response.HearingView;

public class HearingEntityToHearing {
    private HearingEntityToHearing() {
        // Private constructor
    }

    public static HearingView convert(Hearing hearing) {
        final HearingView hearingView = new HearingView();
        hearingView.setHearingId(hearing.getHearingId().toString());
        hearingView.setDuration(hearing.getDuration());
        hearingView.setStartDate(hearing.getStartDate());
        hearingView.setStartTime(hearing.getStartTime());
        hearingView.setCourtCentreName(hearing.getCourtCentreName());
        hearingView.setRoomName(hearing.getRoomName());
        hearingView.setHearingType(hearing.getHearingType());
        return hearingView;
    }

}
