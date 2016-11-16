package uk.gov.moj.cpp.hearing.query.view;

import java.time.LocalDate;
import java.util.UUID;

import uk.gov.moj.cpp.hearing.domain.HearingTypeEnum;
import uk.gov.moj.cpp.hearing.persist.entity.Hearing;

public class HearingTestUtils {
    public static final String ROOM_NAME = "Blackfriar";
    public static final String CENTRE_NAME = "Liverpool";
    public static final String VERSION = "1";

    

    public static Hearing getHearing(UUID caseId, String hearingType) {

        final Hearing hearingA = new Hearing();
        hearingA.setCaseId(caseId);
        hearingA.setHearingId(UUID.randomUUID());
        hearingA.setHearingType(hearingType.equalsIgnoreCase("PTP") ? HearingTypeEnum.PTP : HearingTypeEnum.TRIAL);
        hearingA.setStartDate(LocalDate.now());
        hearingA.setDuration(1);
        hearingA.setCourtCentreName(CENTRE_NAME);
        return hearingA;
    }
}
