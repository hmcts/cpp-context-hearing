package uk.gov.justice.ccr.notepad.common;

import uk.gov.justice.ccr.notepad.result.cache.ResultCache;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MidnightSchedulerTest {

    @Mock
    ResultCache resultCache;


    @Test
    public void runJobCalledReloadOnlyOnce() throws Exception {
        MidnightScheduler testObj = new MidnightScheduler();
        testObj.resultCache = resultCache;

        testObj.runJob();

        Mockito.verify(resultCache, Mockito.times(1)).reload();

    }

}