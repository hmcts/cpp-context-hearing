package uk.gov.justice.ccr.notepad.common;

import uk.gov.justice.ccr.notepad.result.cache.ResultCache;

import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;


public class MidnightSchedulerTest {
    @Test
    public void midnightScheduler_InitialDelayElapsed() throws InterruptedException {
        StubResultCache stubResultCache = new StubResultCache();
        MidnightScheduler testObj = new MidnightScheduler(stubResultCache, 1, 1, TimeUnit.SECONDS);
        Thread.sleep(2000);
        Assert.assertEquals("xyz", stubResultCache.arbitrary);

    }

    @Test
    public void midnightScheduler_InitialDelayNotElapsed() throws InterruptedException {
        StubResultCache stubResultCache = new StubResultCache();
        MidnightScheduler testObj = new MidnightScheduler(stubResultCache);
        testObj.resultCache = stubResultCache;
        Thread.sleep(1000);
        Assert.assertEquals("foo", stubResultCache.arbitrary);

    }

}

class StubResultCache extends ResultCache {
    String arbitrary = "foo";

    @Override
    public void run() {
        arbitrary = "xyz";
    }
}