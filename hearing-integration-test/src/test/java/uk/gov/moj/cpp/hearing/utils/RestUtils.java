package uk.gov.moj.cpp.hearing.utils;

import uk.gov.justice.services.test.utils.core.http.RequestParams;
import uk.gov.justice.services.test.utils.core.http.RequestParamsBuilder;
import uk.gov.justice.services.test.utils.core.http.RestPoller;

import java.util.concurrent.TimeUnit;

public class RestUtils {

    public static final int DEFAULT_POLL_TIMEOUT_IN_SEC = 120;
    public static final int DEFAULT_POLL_TIMEOUT_IN_MILLIS = DEFAULT_POLL_TIMEOUT_IN_SEC * 1000;
    public static final int DEFAULT_NOT_HAPPENED_TIMEOUT_IN_MILLIS = DEFAULT_POLL_TIMEOUT_IN_MILLIS / 6;

    public static RestPoller poll(final RequestParams requestParams) {
        return RestPoller.poll(requestParams).timeout(DEFAULT_POLL_TIMEOUT_IN_SEC, TimeUnit.SECONDS);
    }

    /**
     * Instantiates a new rest poller
     *
     * @param requestParamsBuilder request parameters builder
     * @return this
     */
    public static RestPoller poll(final RequestParamsBuilder requestParamsBuilder) {
        return RestPoller.poll(requestParamsBuilder).timeout(DEFAULT_POLL_TIMEOUT_IN_SEC, TimeUnit.SECONDS);
    }
}
