package uk.gov.moj.cpp.hearing.query.view.convertor;

import static org.junit.Assert.assertEquals;

import java.util.UUID;

import org.junit.Test;

import uk.gov.moj.cpp.hearing.persist.entity.Hearing;
import uk.gov.moj.cpp.hearing.query.view.HearingTestUtils;
import uk.gov.moj.cpp.hearing.query.view.response.HearingView;

public class HearingEntityToHearingTest {

    private static final UUID caseId = UUID.randomUUID();
    
    private static final String HEARING_TYPE = "PTP";

    @Test
    public void convertTest() {

        final Hearing hearingA = HearingTestUtils.getHearing(caseId, HEARING_TYPE);

        final HearingView hearingVo = HearingEntityToHearing.convert(hearingA);
        assertEquals(hearingVo.getCaseId(), hearingA.getCaseId().toString());
        assertEquals(hearingVo.getHearingId(), hearingA.geHearingtId().toString());
        assertEquals(hearingVo.getHearingType(), hearingA.getHearingType().toString());
        assertEquals(hearingVo.getStartDate(), hearingA.getStartDate());
        assertEquals(hearingVo.getDuration(), hearingA.getDuration());
        assertEquals(hearingVo.getCourtCentreName(), HearingTestUtils.CENTRE_NAME);
    }

}
