package uk.gov.moj.cpp.hearing.query.view.convertor;

import static org.junit.Assert.assertEquals;

import uk.gov.moj.cpp.hearing.persist.entity.Hearing;
import uk.gov.moj.cpp.hearing.query.view.HearingTestUtils;
import uk.gov.moj.cpp.hearing.query.view.response.HearingView;

import java.util.UUID;

import org.junit.Test;

public class HearingEntityToHearingTest {

    private static final UUID caseId = UUID.randomUUID();

    private static final String HEARING_TYPE = "PTP";

    @Test
    public void convertTest() {

        final Hearing hearingA = HearingTestUtils.getHearing().get();

        final HearingView hearingVo = HearingEntityToHearing.convert(hearingA);
        assertEquals(hearingVo.getHearingId(), hearingA.geHearingId().toString());
        assertEquals(hearingVo.getStartDate(), hearingA.getStartdate());
        assertEquals(hearingVo.getDuration(), hearingA.getDuration());
    }

}
