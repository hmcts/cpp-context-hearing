package uk.gov.justice.ccr.notepad.common;


import uk.gov.justice.ccr.notepad.result.cache.ResultCache;

import java.util.concurrent.ExecutionException;

import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class MidnightScheduler {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(MidnightScheduler.class.getName());


    @Inject
    ResultCache resultCache;


    @Schedule(hour = "23", minute = "59", second = "59", persistent = false)
    public void runJob() {
        LOGGER.info("Running job... to reload result cache ");
        try {
            resultCache.reload();
        } catch (ExecutionException e) {
            LOGGER.error("Error while reloading result cache ");
            LOGGER.error("{}",e);
        }
    }

}
