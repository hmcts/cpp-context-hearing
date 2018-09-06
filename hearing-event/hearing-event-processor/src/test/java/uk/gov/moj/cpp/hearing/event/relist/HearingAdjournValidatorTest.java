package uk.gov.moj.cpp.hearing.event.relist;

import static org.junit.Assert.assertEquals;
import static uk.gov.moj.cpp.hearing.event.relist.RelistTestHelper.ARBITRARY_WITHDRAWN_META_DATA;
import static uk.gov.moj.cpp.hearing.event.relist.RelistTestHelper.arbitraryNextHearingMetaData;
import static uk.gov.moj.cpp.hearing.event.relist.RelistTestHelper.getArbitrarySharedResult;
import static uk.gov.moj.cpp.hearing.event.relist.RelistTestHelper.getArbitrarySharedResultWithNextHearingResult;

import org.junit.Ignore;
import org.junit.Test;


public class HearingAdjournValidatorTest {

    HearingAdjournValidator testObj = new HearingAdjournValidator();

    @Ignore("GPE-5480 - share results model needs to be updated")
    @Test
    public void validate_when_next_hearing_result_provided() throws Exception {
        assertEquals(true, testObj.checkSharedResultHaveNextHearingResult(getArbitrarySharedResultWithNextHearingResult(), ARBITRARY_WITHDRAWN_META_DATA, arbitraryNextHearingMetaData()));
    }

    @Ignore("GPE-5480 - share results model needs to be updated")
    @Test
    public void validate_when_next_hearing_result_not_provided() throws Exception {
        assertEquals(false, testObj.checkSharedResultHaveNextHearingResult(getArbitrarySharedResult(), ARBITRARY_WITHDRAWN_META_DATA, arbitraryNextHearingMetaData()));
    }

    @Ignore("GPE-5480 - share results model needs to be updated")
    @Test
    public void checkNextHearingEstimatedDurationIsSameForAllOffences_when_next_hearing_result_provided() throws Exception {
        assertEquals(true, testObj.checkNextHearingEstimatedDurationIsSameForAllOffences(getArbitrarySharedResultWithNextHearingResult().getCompletedResultLines(), arbitraryNextHearingMetaData()));
    }

    @Ignore("GPE-5480 - share results model needs to be updated")
    @Test
    public void checkNextHearingEstimatedDurationIsSameForAllOffences_when_next_hearing_result_not_provided() throws Exception {
        assertEquals(false, testObj.checkNextHearingEstimatedDurationIsSameForAllOffences(getArbitrarySharedResult().getCompletedResultLines(), arbitraryNextHearingMetaData()));
    }

    @Ignore("GPE-5480 - share results model needs to be updated")
    @Test
    public void checkNextHearingTypeIsSameForAllOffences_when_next_hearing_result_provided() throws Exception {
        assertEquals(true, testObj.checkNextHearingTypeIsSameForAllOffences(getArbitrarySharedResultWithNextHearingResult().getCompletedResultLines(), arbitraryNextHearingMetaData()));

    }

    @Ignore("GPE-5480 - share results model needs to be updated")
    @Test
    public void checkNextHearingTypeIsSameForAllOffences_when_next_hearing_result_not_provided() throws Exception {
        assertEquals(false, testObj.checkNextHearingTypeIsSameForAllOffences(getArbitrarySharedResult().getCompletedResultLines(), arbitraryNextHearingMetaData()));
    }

    @Ignore("GPE-5480 - share results model needs to be updated")
    @Test
    public void checkNextHearingDateOfHearingIsSameForAllOffences_when_next_hearing_result_provided() throws Exception {
        assertEquals(true, testObj.checkNextHearingDateOfHearingIsSameForAllOffences(getArbitrarySharedResultWithNextHearingResult().getCompletedResultLines(), arbitraryNextHearingMetaData()));

    }

    @Ignore("GPE-5480 - share results model needs to be updated")
    @Test
    public void checkNextHearingDateOfHearingIsSameForAllOffences_when_next_hearing_result_not_provided() throws Exception {
        assertEquals(false, testObj.checkNextHearingDateOfHearingIsSameForAllOffences(getArbitrarySharedResult().getCompletedResultLines(), arbitraryNextHearingMetaData()));

    }

    @Ignore("GPE-5480 - share results model needs to be updated")
    @Test
    public void checkSharedResultHaveNextHearingResult_when_next_hearing_result_provided() throws Exception {
        assertEquals(true, testObj.checkSharedResultHaveNextHearingResult(getArbitrarySharedResultWithNextHearingResult(), ARBITRARY_WITHDRAWN_META_DATA, arbitraryNextHearingMetaData()));

    }

    @Ignore("GPE-5480 - share results model needs to be updated")
    @Test
    public void checkSharedResultHaveNextHearingResult_when_next_hearing_result_not_provided() throws Exception {
        assertEquals(false, testObj.checkSharedResultHaveNextHearingResult(getArbitrarySharedResult(), ARBITRARY_WITHDRAWN_META_DATA, arbitraryNextHearingMetaData()));

    }

}