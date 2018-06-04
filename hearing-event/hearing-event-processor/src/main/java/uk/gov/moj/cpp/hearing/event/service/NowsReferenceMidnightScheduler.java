package uk.gov.moj.cpp.hearing.event.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.inject.Inject;

@Singleton
public class NowsReferenceMidnightScheduler {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(NowsReferenceMidnightScheduler.class.getName());

    @Inject
    NowsReferenceCache nowsReferenceCache;

    @Schedule(hour = "23", minute = "59", second = "59", persistent = false)
    public void runJob() {
        LOGGER.error("Running job... to reload result cache ");
            nowsReferenceCache.reload();
        }

}
