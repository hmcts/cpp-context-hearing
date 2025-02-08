package uk.gov.moj.cpp.hearing.it;

import static java.text.MessageFormat.format;
import static java.util.concurrent.TimeUnit.SECONDS;
import static javax.json.Json.createReader;
import static javax.ws.rs.core.Response.Status.OK;
import static javax.ws.rs.core.Response.Status.fromStatusCode;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static uk.gov.justice.services.common.http.HeaderConstants.USER_ID;
import static uk.gov.justice.services.test.utils.core.http.BaseUriProvider.getBaseUri;
import static uk.gov.justice.services.test.utils.core.http.RequestParamsBuilder.requestParams;
import static uk.gov.justice.services.test.utils.core.matchers.ResponsePayloadMatcher.payload;
import static uk.gov.justice.services.test.utils.core.matchers.ResponseStatusMatcher.status;
import static uk.gov.moj.cpp.hearing.it.AbstractIT.ENDPOINT_PROPERTIES;
import static uk.gov.moj.cpp.hearing.it.AbstractIT.getLoggedInUser;
import static uk.gov.moj.cpp.hearing.it.AbstractIT.getURL;
import static uk.gov.moj.cpp.hearing.it.AbstractIT.print;
import static uk.gov.moj.cpp.hearing.test.matchers.MapJsonObjectToTypeMatcher.convertTo;
import static uk.gov.moj.cpp.hearing.utils.RestUtils.DEFAULT_POLL_TIMEOUT_IN_SEC;
import static uk.gov.moj.cpp.hearing.utils.RestUtils.DEFAULT_WAIT_TIME_IN_SEC;
import static uk.gov.moj.cpp.hearing.utils.RestUtils.poll;

import uk.gov.justice.hearing.courts.GetHearings;
import uk.gov.justice.services.common.http.HeaderConstants;
import uk.gov.justice.services.test.utils.core.http.RequestParams;
import uk.gov.justice.services.test.utils.core.http.ResponseData;
import uk.gov.justice.services.test.utils.core.http.RestPoller;
import uk.gov.justice.services.test.utils.core.rest.RestClient;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.HearingDetailsResponse;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.TargetListResponse;
import uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher;

import java.io.StringReader;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.UUID;

import javax.json.JsonObject;
import javax.ws.rs.core.Response;

import com.jayway.jsonpath.ReadContext;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

@SuppressWarnings({"squid:S2925"})
public class Queries {

    public static void getHearingPollForMatch(final UUID hearingId, final BeanMatcher<HearingDetailsResponse> resultMatcher) {
        getHearingPollForMatch(hearingId, DEFAULT_POLL_TIMEOUT_IN_SEC, resultMatcher);
    }

    public static void getHearingPollForMatch(final UUID hearingId, final long timeout, final BeanMatcher<HearingDetailsResponse> resultMatcher) {
        /*
        You might be wondering why I don't use the Framework's poll stuff.  Its because that stuff polls and if the matcher
        doesn't match, then it prints out a completely rubbish error message doesn't consult the matcher for the error
        description.  This has cost us A LOT of time in development.

        Do not use the framework matcher.
         */

        final RequestParams requestParams = requestParams(getURL("hearing.get.hearing", hearingId), "application/vnd.hearing.get.hearing+json")
                .withHeader(HeaderConstants.USER_ID, getLoggedInUser())
                .build();

        pollQueryEndpoint(jsonPayloadMatchesBean(HearingDetailsResponse.class, resultMatcher), timeout, requestParams);
    }

    public static void getHearingsByDatePollForMatch(final UUID courtCentreId, final UUID roomId, final String date, final String startTime, final String endTime, final BeanMatcher<GetHearings> resultMatcher) {

        final RequestParams requestParams = requestParams(getURL("hearing.get.hearings", date, startTime, endTime, courtCentreId, roomId), "application/vnd.hearing.get.hearings+json")
                .withHeader(HeaderConstants.USER_ID, getLoggedInUser())
                .build();

        pollQueryEndpoint(jsonPayloadMatchesBean(GetHearings.class, resultMatcher), DEFAULT_POLL_TIMEOUT_IN_SEC, requestParams);
    }

    public static void getDraftResultsPollForMatch(final UUID hearingId, final BeanMatcher<TargetListResponse> resultMatcher) {

        final RequestParams requestParams = requestParams(getURL("hearing.get-draft-result", hearingId), "application/vnd.hearing.get-draft-result+json")
                .withHeader(HeaderConstants.USER_ID, getLoggedInUser())
                .build();

        pollQueryEndpoint(jsonPayloadMatchesBean(TargetListResponse.class, resultMatcher), DEFAULT_POLL_TIMEOUT_IN_SEC, requestParams);

    }

    public static void getDraftResultsForHearingDayPollForMatch(final UUID hearingId, final String hearingDay, final BeanMatcher<TargetListResponse> resultMatcher) {

        final RequestParams requestParams = requestParams(getURL("hearing.get-results", hearingId, hearingDay), "application/vnd.hearing.results+json")
                .withHeader(HeaderConstants.USER_ID, getLoggedInUser())
                .build();

        pollQueryEndpoint(jsonPayloadMatchesBean(TargetListResponse.class, resultMatcher), DEFAULT_POLL_TIMEOUT_IN_SEC, requestParams);

    }

    public static ResponseData getCalculatedCustodyTimeLimitExpiryDate(final UUID hearingId, final String hearingDay, final UUID offenceId, final String bailStatusCode) {

        String url = getURL("hearing.custody-time-limit", hearingId.toString(), hearingDay, offenceId.toString(), bailStatusCode);

        final RequestParams requestParams = requestParams(url, "application/vnd.hearing.custody-time-limit+json")
                .withHeader(HeaderConstants.USER_ID, getLoggedInUser())
                .build();

        final Matcher<ResponseData> expectedConditions = allOf(status().is(OK));

        final ZonedDateTime expiryTime = ZonedDateTime.now().plusSeconds(DEFAULT_POLL_TIMEOUT_IN_SEC);

        ResponseData responseData = makeRequest(requestParams);

        while (!expectedConditions.matches(responseData) && ZonedDateTime.now().isBefore(expiryTime)) {
            waitFor();
            responseData = makeRequest(requestParams);
        }

        return responseData;

    }

    private static void waitFor() {
        waitFor(DEFAULT_WAIT_TIME_IN_SEC);
    }

    public static void waitFor(long numberOfSeconds) {
        try {
            SECONDS.sleep(numberOfSeconds);
        } catch (InterruptedException ex) {
            //ignore
        }
    }

    private static void pollQueryEndpoint(final Matcher<ResponseData> resultMatcher, final long timeout, final RequestParams requestParams) {
        final Matcher<ResponseData> expectedConditions = allOf(status().is(OK), resultMatcher);

        final ZonedDateTime expiryTime = ZonedDateTime.now().plusSeconds(timeout);

        ResponseData responseData = makeRequest(requestParams);

        while (!expectedConditions.matches(responseData) && ZonedDateTime.now().isBefore(expiryTime)) {
            waitFor();
            responseData = makeRequest(requestParams);
        }

        if (!expectedConditions.matches(responseData)) {
            assertThat(responseData, expectedConditions);
        }
    }

    private static ResponseData makeRequest(RequestParams requestParams) {
        Response response = new RestClient().query(requestParams.getUrl(), requestParams.getMediaType(), requestParams.getHeaders());
        String responseData = response.readEntity(String.class);
        return new ResponseData(fromStatusCode(response.getStatus()), responseData, response.getHeaders());
    }

    public static <T> Matcher<ResponseData> jsonPayloadMatchesBean(Class<T> theClass, BeanMatcher<T> beanMatcher) {
        final BaseMatcher<JsonObject> jsonObjectMatcher = convertTo(theClass, beanMatcher);
        return new BaseMatcher<>() {
            @Override
            public boolean matches(final Object o) {
                if (o instanceof final ResponseData responseData) {
                    if (responseData.getPayload() != null) {
                        JsonObject jsonObject = createReader(new StringReader(responseData.getPayload())).readObject();
                        return jsonObjectMatcher.matches(jsonObject);
                    }
                }
                return false;
            }

            @Override
            public void describeMismatch(Object item, Description description) {
                ResponseData responseData = (ResponseData) item;
                JsonObject jsonObject = createReader(new StringReader(responseData.getPayload())).readObject();
                jsonObjectMatcher.describeMismatch(jsonObject, description);
            }

            @Override
            public void describeTo(final Description description) {
                jsonObjectMatcher.describeTo(description);
            }
        };
    }

    public static void pollForFutureHearings(final UUID userId, final UUID prosecutionCaseId, final BeanMatcher<GetHearings> resultMatcher) {
        final String queryPart = format(ENDPOINT_PROPERTIES.getProperty("hearing.get.hearings-for-future"), prosecutionCaseId);
        final String searchCourtListUrl = String.format("%s/%s", getBaseUri(), queryPart);
        final Matcher<ResponseData> expectedConditions = Matchers.allOf(status().is(OK), jsonPayloadMatchesBean(GetHearings.class, resultMatcher));
        final RequestParams requestParams = requestParams(searchCourtListUrl, "application/vnd.hearing.get.future-hearings+json")
                .withHeader(HeaderConstants.USER_ID, userId)
                .build();

        makeRequest(requestParams);

        poll(requestParams)
                .timeout(DEFAULT_POLL_TIMEOUT_IN_SEC, SECONDS)
                .until(
                        status().is(OK),
                        expectedConditions
                );
    }

    public static void pollForHearing(final String hearingId, final Matcher<? super ReadContext>... matchers) {
        poll(requestParams(getURL("hearing.get.hearing", hearingId), "application/vnd.hearing.get.hearing+json")
                .withHeader(HeaderConstants.USER_ID, getLoggedInUser()).build())
                .timeout(DEFAULT_POLL_TIMEOUT_IN_SEC, SECONDS)
                .until(status().is(OK),
                        print(),
                        payload().isJson(allOf(matchers))
                );
    }

    public static void pollForHearingEvents(final String hearingId, final LocalDate localDate, final Matcher[] matchers) {
        poll(requestParams(getURL("hearing.get-hearing-event-log", hearingId, localDate),
                "application/vnd.hearing.hearing-event-log+json")
                .withHeader(USER_ID, getLoggedInUser()))
                .timeout(DEFAULT_POLL_TIMEOUT_IN_SEC, SECONDS)
                .until(
                        status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                matchers
                        )));
    }

    public static void pollForOutstandingFines(final String defendantId, final Matcher<? super ReadContext>... matchers) {
        RestPoller.poll(requestParams(getURL("hearing.defendant.outstanding-fines", defendantId),
                        "application/vnd.hearing.defendant.outstanding-fines+json")
                        .withHeader(HeaderConstants.USER_ID, getLoggedInUser()).build())
                .timeout(DEFAULT_POLL_TIMEOUT_IN_SEC, SECONDS)
                .until(status().is(OK),
                        print(),
                        payload().isJson(allOf(matchers)
                        ));
    }
}
