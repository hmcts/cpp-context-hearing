package uk.gov.moj.cpp.hearing.event.relist;

import static org.junit.Assert.assertEquals;
import static uk.gov.moj.cpp.hearing.event.relist.RelistTestHelper.arbitraryNextHearingMetaData;
import static uk.gov.moj.cpp.hearing.event.relist.RelistTestHelper.getArbitrarySharedResultWithNextHearingResult;
import static uk.gov.moj.cpp.hearing.event.relist.metadata.NextHearingPromptReference.HDATE;
import static uk.gov.moj.cpp.hearing.event.relist.metadata.NextHearingPromptReference.HEST;
import static uk.gov.moj.cpp.hearing.event.relist.metadata.NextHearingPromptReference.HTIME;
import static uk.gov.moj.cpp.hearing.event.relist.metadata.NextHearingPromptReference.HTYPE;

import org.junit.Ignore;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared;

import org.junit.Test;


public class HearingAdjournHelperTest {

    @Test
    public void getAllPromptUuidsByPromptReference() throws Exception {
        assertEquals(1, HearingAdjournHelper.getAllPromptUuidsByPromptReference(arbitraryNextHearingMetaData(), HDATE).size());

        assertEquals(1, HearingAdjournHelper.getAllPromptUuidsByPromptReference(arbitraryNextHearingMetaData(), HEST).size());

        assertEquals(1, HearingAdjournHelper.getAllPromptUuidsByPromptReference(arbitraryNextHearingMetaData(), HTIME).size());

        assertEquals(1, HearingAdjournHelper.getAllPromptUuidsByPromptReference(arbitraryNextHearingMetaData(), HTYPE).size());
    }

    @Test
    public void getDistinctPromptValue() throws Exception {
        assertEquals(1, HearingAdjournHelper.getDistinctPromptValue(getArbitrarySharedResultWithNextHearingResult().getHearing().getTargets().get(0).getResultLines()
                , arbitraryNextHearingMetaData(), HearingAdjournHelper.getAllPromptUuidsByPromptReference(arbitraryNextHearingMetaData(), HDATE)).size());

        assertEquals(1, HearingAdjournHelper.getDistinctPromptValue(getArbitrarySharedResultWithNextHearingResult().getHearing().getTargets().get(0).getResultLines()
                , arbitraryNextHearingMetaData(), HearingAdjournHelper.getAllPromptUuidsByPromptReference(arbitraryNextHearingMetaData(), HEST)).size());

        assertEquals(1, HearingAdjournHelper.getDistinctPromptValue(getArbitrarySharedResultWithNextHearingResult().getHearing().getTargets().get(0).getResultLines()
                , arbitraryNextHearingMetaData(), HearingAdjournHelper.getAllPromptUuidsByPromptReference(arbitraryNextHearingMetaData(), HTIME)).size());

        assertEquals(1, HearingAdjournHelper.getDistinctPromptValue(getArbitrarySharedResultWithNextHearingResult().getHearing().getTargets().get(0).getResultLines()
                , arbitraryNextHearingMetaData(), HearingAdjournHelper.getAllPromptUuidsByPromptReference(arbitraryNextHearingMetaData(), HTYPE)).size());

    }

    @Test
    public void getOffencesHaveResultNextHearing() throws Exception {
        //given
        ResultsShared arbitraryResultsShared = getArbitrarySharedResultWithNextHearingResult();

        //when and then
        assertEquals(1, HearingAdjournHelper.getOffencesHaveResultNextHearing(
                arbitraryResultsShared.getHearing().getProsecutionCases().get(0).getDefendants().get(0),
                arbitraryResultsShared.getHearing().getTargets(),
                arbitraryResultsShared.getHearing().getTargets().get(0).getResultLines(),
                arbitraryNextHearingMetaData()).size());

    }

}