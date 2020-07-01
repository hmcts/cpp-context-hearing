package uk.gov.moj.cpp.hearing.event.relist;

import static org.junit.Assert.assertEquals;
import static uk.gov.moj.cpp.hearing.event.relist.RelistTestHelper.ARBITRARY_WITHDRAWN_META_DATA;
import static uk.gov.moj.cpp.hearing.event.relist.RelistTestHelper.arbitraryNextHearingMetaData;
import static uk.gov.moj.cpp.hearing.event.relist.RelistTestHelper.getArbitraryApplicationSharedResultWithNextHearingResult;
import static uk.gov.moj.cpp.hearing.event.relist.RelistTestHelper.getArbitraryApplicationSharedResultWithNextHearingResultWithExcludedPrompt;
import static uk.gov.moj.cpp.hearing.event.relist.RelistTestHelper.getArbitrarySharedResult;
import static uk.gov.moj.cpp.hearing.event.relist.RelistTestHelper.getArbitrarySharedResultWithNextHearingResult;
import static uk.gov.moj.cpp.hearing.event.relist.RelistTestHelper.getArbitrarySharedResultWithNextHearingResultWithExcludedPrompt;

import org.junit.Test;


public class HearingAdjournValidatorTest {

    HearingAdjournValidator testObj = new HearingAdjournValidator();


    @Test
    public void validate_when_next_hearing_result_provided() throws Exception {
        assertEquals(true, testObj.checkSharedResultHaveNextHearingOrWithdrawnOffenceResult(getArbitrarySharedResultWithNextHearingResult(), ARBITRARY_WITHDRAWN_META_DATA, arbitraryNextHearingMetaData()));
    }

    @Test
    public void validate_when_next_hearing_result_not_provided() throws Exception {
        assertEquals(false, testObj.checkSharedResultHaveNextHearingOrWithdrawnOffenceResult(getArbitrarySharedResult(), ARBITRARY_WITHDRAWN_META_DATA, arbitraryNextHearingMetaData()));
    }

    @Test
    public void checkNextHearingEstimatedDurationIsSameForAllOffences_when_next_hearing_result_provided() throws Exception {
        assertEquals(true, testObj.checkNextHearingEstimatedDurationIsSameForAllOffences(getArbitrarySharedResultWithNextHearingResult().getTargets().get(0).getResultLines(), arbitraryNextHearingMetaData()));
    }

    @Test
    public void checkNextHearingEstimatedDurationIsSameForAllOffences_when_next_hearing_result_not_provided() throws Exception {
        assertEquals(false, testObj.checkNextHearingEstimatedDurationIsSameForAllOffences(getArbitrarySharedResult().getTargets().get(0).getResultLines(), arbitraryNextHearingMetaData()));
    }

    @Test
    public void checkNextHearingTypeIsSameForAllOffences_when_next_hearing_result_provided() throws Exception {
        assertEquals(true, testObj.checkNextHearingTypeIsSameForAllOffences(getArbitrarySharedResultWithNextHearingResult().getTargets().get(0).getResultLines(), arbitraryNextHearingMetaData()));

    }

    @Test
    public void checkNextHearingTypeIsSameForAllOffences_when_next_hearing_result_not_provided() throws Exception {
        assertEquals(false, testObj.checkNextHearingTypeIsSameForAllOffences(getArbitrarySharedResult().getTargets().get(0).getResultLines(), arbitraryNextHearingMetaData()));
    }

    @Test
    public void checkNextHearingDateOfHearingIsSameForAllOffences_when_next_hearing_result_provided() throws Exception {
        assertEquals(true, testObj.checkNextHearingDateOfHearingIsSameForAllOffences(getArbitrarySharedResultWithNextHearingResult().getTargets().get(0).getResultLines(), arbitraryNextHearingMetaData()));
    }

    @Test
    public void checkNextHearingDateOfHearingIsSameForAllOffences_when_next_hearing_result_not_provided() throws Exception {
        assertEquals(false, testObj.checkNextHearingDateOfHearingIsSameForAllOffences(getArbitrarySharedResult().getTargets().get(0).getResultLines(), arbitraryNextHearingMetaData()));
    }

    @Test
    public void checkSharedResultHaveNextHearingResult_when_next_hearing_result_provided() throws Exception {
        assertEquals(true, testObj.checkSharedResultHaveNextHearingOrWithdrawnOffenceResult(getArbitrarySharedResultWithNextHearingResult(), ARBITRARY_WITHDRAWN_META_DATA, arbitraryNextHearingMetaData()));
    }

    @Test
    public void checkSharedResultHaveNextHearingResult_when_next_hearing_result_not_provided() throws Exception {
        assertEquals(false, testObj.checkSharedResultHaveNextHearingOrWithdrawnOffenceResult(getArbitrarySharedResult(), ARBITRARY_WITHDRAWN_META_DATA, arbitraryNextHearingMetaData()));
    }

    @Test
    public void checkValidateProsecutionCaseWithNextHearingResults() {
        assertEquals(true, testObj.validateProsecutionCase(getArbitrarySharedResultWithNextHearingResult(), ARBITRARY_WITHDRAWN_META_DATA, arbitraryNextHearingMetaData()));
    }

    @Test
    public void checkValidateProsecutionCaseWithNextHearingResultsWithExcludedPrompt() {
        assertEquals(false, testObj.validateProsecutionCase(getArbitrarySharedResultWithNextHearingResultWithExcludedPrompt(), ARBITRARY_WITHDRAWN_META_DATA, arbitraryNextHearingMetaData()));
    }

    @Test
    public void checkValidateApplicationWithNextHearingResults() {
        assertEquals(true, testObj.validateApplication(getArbitraryApplicationSharedResultWithNextHearingResult(), arbitraryNextHearingMetaData()));
    }

    @Test
    public void checkValidateApplicationWithNextHearingWithExcludedPrompt() {
        assertEquals(false, testObj.validateApplication(getArbitraryApplicationSharedResultWithNextHearingResultWithExcludedPrompt(), arbitraryNextHearingMetaData()));
    }
}