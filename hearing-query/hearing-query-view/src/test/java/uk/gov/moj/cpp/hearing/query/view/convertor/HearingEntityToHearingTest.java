package uk.gov.moj.cpp.hearing.query.view.convertor;

import static org.junit.Assert.assertEquals;

import uk.gov.moj.cpp.hearing.persist.entity.Hearing;
import uk.gov.moj.cpp.hearing.query.view.HearingTestUtils;
import uk.gov.moj.cpp.hearing.query.view.response.HearingView;

import org.junit.Test;

public class HearingEntityToHearingTest {

    @Test
    public void convertTest() {

        final Hearing hearingA = HearingTestUtils.getHearing().get();

        final HearingView hearingVo = HearingEntityToHearing.convert(hearingA);
        assertEquals(hearingVo.getHearingId(), hearingA.getHearingId().toString());
        assertEquals(hearingVo.getStartDate(), hearingA.getStartDate());
        assertEquals(hearingVo.getDuration(), hearingA.getDuration());
    }

}
