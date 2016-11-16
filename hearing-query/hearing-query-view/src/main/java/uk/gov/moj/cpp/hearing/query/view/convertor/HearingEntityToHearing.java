package uk.gov.moj.cpp.hearing.query.view.convertor;

import uk.gov.moj.cpp.hearing.persist.entity.Hearing;
import uk.gov.moj.cpp.hearing.query.view.response.HearingView;

public class HearingEntityToHearing {
    private HearingEntityToHearing() {
        // Private constructor
    }

    public static HearingView convert(Hearing hearing) {
        final HearingView hearingVo = new HearingView();
        hearingVo.setHearingId(hearing.geHearingtId().toString());
        hearingVo.setCaseId(hearing.getCaseId().toString());
        hearingVo.setDuration(hearing.getDuration());
        hearingVo.setHearingType(hearing.getHearingType().toString());
        hearingVo.setStartDate(hearing.getStartDate());
        hearingVo.setCourtCentreName(hearing.getCourtCentreName());
        return hearingVo;
    }

}
