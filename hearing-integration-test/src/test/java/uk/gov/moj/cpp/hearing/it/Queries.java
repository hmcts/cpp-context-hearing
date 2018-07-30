package uk.gov.moj.cpp.hearing.it;

import static javax.ws.rs.core.Response.Status.OK;
import static uk.gov.justice.services.test.utils.core.http.RequestParamsBuilder.requestParams;
import static uk.gov.justice.services.test.utils.core.http.RestPoller.poll;
import static uk.gov.justice.services.test.utils.core.matchers.ResponseStatusMatcher.status;
import static uk.gov.moj.cpp.hearing.it.AbstractIT.CPP_UID_HEADER;
import static uk.gov.moj.cpp.hearing.it.AbstractIT.getURL;
import static uk.gov.moj.cpp.hearing.it.AbstractIT.jsonPayloadMatchesBean;
import static uk.gov.moj.cpp.hearing.it.AbstractIT.print;

import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.HearingDetailsResponse;
import uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class Queries {

    public static void getHearingPollForMatch(final UUID hearingId, final long timeout, final BeanMatcher<HearingDetailsResponse> resultMatcher) {
        poll(requestParams(getURL("hearing.get.hearing", hearingId), "application/vnd.hearing.get.hearing+json")
                .withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue()).build())
                .timeout(timeout, TimeUnit.SECONDS)
                .until(status().is(OK),
                        print(),
                        jsonPayloadMatchesBean(HearingDetailsResponse.class, resultMatcher)
                );
    }
}
