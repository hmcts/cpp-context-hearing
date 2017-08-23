package uk.gov.justice.ccr.notepad.common;


import uk.gov.justice.ccr.notepad.result.cache.ResultCache;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.ejb.Startup;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@Startup
@ApplicationScoped
public class MidnightScheduler {

    private final int period;
    private final long initialDelay;

    @Inject
    ResultCache resultCache;

    private final ScheduledExecutorService scheduledThreadPoolExecutor = Executors.newSingleThreadScheduledExecutor();


    public MidnightScheduler() {
        initialDelay = LocalDateTime.now().until(LocalDate.now().plusDays(1).atStartOfDay(), ChronoUnit.MINUTES);
        period = 1440;
        scheduledThreadPoolExecutor.scheduleAtFixedRate(resultCache, initialDelay, period, TimeUnit.MINUTES);
    }

    public MidnightScheduler(final Runnable runnable, final long initialDelay, final int period, final TimeUnit unit) {
        this.period = period;
        this.initialDelay = initialDelay;
        scheduledThreadPoolExecutor.scheduleAtFixedRate(runnable, initialDelay, period, unit);
    }

    public MidnightScheduler(final Runnable runnable) {
        initialDelay = LocalDateTime.now().until(LocalDate.now().plusDays(1).atStartOfDay(), ChronoUnit.MINUTES);
        period = 1440;
        scheduledThreadPoolExecutor.scheduleAtFixedRate(runnable, initialDelay, period, TimeUnit.MINUTES);
    }
}
