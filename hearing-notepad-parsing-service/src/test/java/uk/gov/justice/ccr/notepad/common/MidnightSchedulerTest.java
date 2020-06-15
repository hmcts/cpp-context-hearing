package uk.gov.justice.ccr.notepad.common;

import org.mockito.InjectMocks;
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

    @InjectMocks
    MidnightScheduler target;

    @Test
    public void runJobCalledReloadOnlyOnce() {
        target.runJob();
        Mockito.verify(resultCache).reloadCache();
    }
}