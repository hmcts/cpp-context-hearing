package uk.gov.moj.cpp.hearing.event.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class NowsReferenceMidnightSchedulerTest {

    @Mock
    private NowsReferenceCache nowsReferenceCache;

    @Test
    public void runJobCalledReloadOnlyOnce() throws Exception {
        NowsReferenceMidnightScheduler testObj = new NowsReferenceMidnightScheduler();
        testObj.nowsReferenceCache = nowsReferenceCache;
        testObj.runJob();
        Mockito.verify(nowsReferenceCache, Mockito.times(1)).reload();
    }

}
