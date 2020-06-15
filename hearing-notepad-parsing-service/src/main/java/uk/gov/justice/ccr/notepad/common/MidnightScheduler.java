package uk.gov.justice.ccr.notepad.common;


import uk.gov.justice.ccr.notepad.result.cache.ResultCache;

import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class MidnightScheduler {

    @Inject
    private ResultCache resultCache;

    private static final Logger LOGGER = LoggerFactory.getLogger(MidnightScheduler.class.getName());

    @Schedule(hour = "00", minute = "10", second = "00", persistent = false)
    public void runJob() {
        LOGGER.info("Running job to reload result cache");
        resultCache.reloadCache();
    }
}
