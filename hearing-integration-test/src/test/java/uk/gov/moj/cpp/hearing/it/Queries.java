package uk.gov.moj.cpp.hearing.it;

import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.CoreMatchers.allOf;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.common.http.HeaderConstants.USER_ID;
import static uk.gov.justice.services.test.utils.core.http.RequestParamsBuilder.requestParams;
import static uk.gov.justice.services.test.utils.core.matchers.ResponsePayloadMatcher.payload;
import static uk.gov.justice.services.test.utils.core.matchers.ResponseStatusMatcher.status;
import static uk.gov.moj.cpp.hearing.it.AbstractIT.getURL;
import static uk.gov.moj.cpp.hearing.test.matchers.MapJsonObjectToTypeMatcher.convertTo;
import static uk.gov.moj.cpp.hearing.utils.RestUtils.poll;

import uk.gov.justice.hearing.courts.GetHearings;
import uk.gov.justice.services.common.http.HeaderConstants;
import uk.gov.justice.services.test.utils.core.http.RequestParams;
import uk.gov.justice.services.test.utils.core.http.ResponseData;
import uk.gov.justice.services.test.utils.core.http.RestPoller;
import uk.gov.justice.services.test.utils.core.rest.RestClient;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.ApplicationTargetListResponse;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.HearingDetailsResponse;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.TargetListResponse;
import uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher;

import java.io.StringReader;
import java.time.ZonedDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.core.Response;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

@SuppressWarnings({"squid:S2925"})
public class Queries {

    public static void getHearingForTodayPollForMatch(final UUID userId, final long timeout, final BeanMatcher<GetHearings> resultMatcher) {
        final RequestParams requestParams = requestParams(getURL("hearing.get.hearings-for-today"), "application/vnd.hearing.get.hearings-for-today+json")
                .withHeader(HeaderConstants.USER_ID, userId)
                .build();

        final Matcher<ResponseData> expectedConditions = Matchers.allOf(status().is(OK), jsonPayloadMatchesBean(GetHearings.class, resultMatcher));
         poll(requestParams)
                .timeout(timeout, TimeUnit.SECONDS)
                .until(
                        status().is(OK),
                        expectedConditions
                );
    }

    public static void getHearingPollForMatch(final UUID hearingId, final long timeout, final BeanMatcher<HearingDetailsResponse> resultMatcher) {
        getHearingPollForMatch(hearingId, timeout, 3, resultMatcher);

    }

    public static void getHearingPollForMatch(final UUID hearingId, final long timeout, final long waitTime, final BeanMatcher<HearingDetailsResponse> resultMatcher) {

        /*
        You might be wondering why I don't use the Framework's poll stuff.  Its because that stuff polls and if the matcher
        doesn't match, then it prints out a completely rubbish error message doesn't consult the matcher for the error
        description.  This has cost us A LOT of time in development.

        Do not use the framework matcher.
         */

        waitForFewSeconds(waitTime);

        final RequestParams requestParams = requestParams(getURL("hearing.get.hearing", hearingId), "application/vnd.hearing.get.hearing+json")
               .withHeader(HeaderConstants.USER_ID, AbstractIT.getLoggedInUser())
                .build();

        final Matcher<ResponseData> expectedConditions = Matchers.allOf(status().is(OK), jsonPayloadMatchesBean(HearingDetailsResponse.class, resultMatcher));

        final ZonedDateTime expiryTime = ZonedDateTime.now().plusSeconds(timeout);

        ResponseData responseData = makeRequest(requestParams);

        while (!expectedConditions.matches(responseData) && ZonedDateTime.now().isBefore(expiryTime)) {
            sleep();
            responseData = makeRequest(requestParams);
        }

        if (!expectedConditions.matches(responseData)) {
            assertThat(responseData, expectedConditions);
        }
    }

    public static void getHearingsByDatePollForMatch(final UUID courtCentreId, final UUID roomId, final String date, final String startTime, final String endTime, final long timeout, final BeanMatcher<GetHearings> resultMatcher) {

        final RequestParams requestParams = requestParams(getURL("hearing.get.hearings", date, startTime, endTime, courtCentreId, roomId), "application/vnd.hearing.get.hearings+json")
               .withHeader(HeaderConstants.USER_ID, AbstractIT.getLoggedInUser())
                .build();

        final Matcher<ResponseData> expectedConditions = Matchers.allOf(status().is(OK), jsonPayloadMatchesBean(GetHearings.class, resultMatcher));

        final ZonedDateTime expiryTime = ZonedDateTime.now().plusSeconds(timeout);

        ResponseData responseData = makeRequest(requestParams);

        while (!expectedConditions.matches(responseData) && ZonedDateTime.now().isBefore(expiryTime)) {
            sleep();
            responseData = makeRequest(requestParams);
        }

        if (!expectedConditions.matches(responseData)) {
            assertThat(responseData, expectedConditions);
        }
    }

    public static void getDraftResultsPollForMatch(final UUID hearingId, final long timeout, final BeanMatcher<TargetListResponse> resultMatcher) {

        final RequestParams requestParams = requestParams(getURL("hearing.get-draft-result", hearingId), "application/vnd.hearing.get-draft-result+json")
               .withHeader(HeaderConstants.USER_ID, AbstractIT.getLoggedInUser())
                .build();

        final Matcher<ResponseData> expectedConditions = Matchers.allOf(status().is(OK), jsonPayloadMatchesBean(TargetListResponse.class, resultMatcher));

        final ZonedDateTime expiryTime = ZonedDateTime.now().plusSeconds(timeout);

        ResponseData responseData = makeRequest(requestParams);

        while (!expectedConditions.matches(responseData) && ZonedDateTime.now().isBefore(expiryTime)) {
            sleep();
            responseData = makeRequest(requestParams);
        }

        if (!expectedConditions.matches(responseData)) {
            assertThat(responseData, expectedConditions);
        }

    }

    public static void getApplicationDraftResultsPollForMatch(final UUID hearingId, final long timeout, final BeanMatcher<ApplicationTargetListResponse> resultMatcher) {

        final RequestParams requestParams = requestParams(getURL("hearing.get-application-draft-result", hearingId), "application/vnd.hearing.get-application-draft-result+json")
               .withHeader(HeaderConstants.USER_ID, AbstractIT.getLoggedInUser())
                .build();

        final Matcher<ResponseData> expectedConditions = Matchers.allOf(status().is(OK), jsonPayloadMatchesBean(ApplicationTargetListResponse.class, resultMatcher));

        final ZonedDateTime expiryTime = ZonedDateTime.now().plusSeconds(timeout);

        ResponseData responseData = makeRequest(requestParams);

        while (!expectedConditions.matches(responseData) && ZonedDateTime.now().isBefore(expiryTime)) {
            sleep();
            responseData = makeRequest(requestParams);
        }

        if (!expectedConditions.matches(responseData)) {
            assertThat(responseData, expectedConditions);
        }

    }

    private static void sleep() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            //ignore
        }
    }

    public static void waitForFewSeconds(long numberOfSeconds) {
        try {
            TimeUnit.SECONDS.sleep(numberOfSeconds);
        } catch (InterruptedException ex) {
            //ignore
        }
    }

    private static ResponseData makeRequest(RequestParams requestParams) {
        Response response = new RestClient().query(requestParams.getUrl(), requestParams.getMediaType(), requestParams.getHeaders());
        String responseData = (String) response.readEntity(String.class);
        return new ResponseData(Response.Status.fromStatusCode(response.getStatus()), responseData, response.getHeaders());
    }

    private static <T> Matcher<ResponseData> jsonPayloadMatchesBean(Class<T> theClass, BeanMatcher<T> beanMatcher) {
        final BaseMatcher<JsonObject> jsonObjectMatcher = convertTo(theClass, beanMatcher);
        return new BaseMatcher<ResponseData>() {
            @Override
            public boolean matches(final Object o) {
                if (o instanceof ResponseData) {
                    final ResponseData responseData = (ResponseData) o;
                    if (responseData.getPayload() != null) {
                        JsonObject jsonObject = Json.createReader(new StringReader(responseData.getPayload())).readObject();
                        return jsonObjectMatcher.matches(jsonObject);
                    }
                }
                return false;
            }

            @Override
            public void describeMismatch(Object item, Description description) {
                ResponseData responseData = (ResponseData) item;
                JsonObject jsonObject = Json.createReader(new StringReader(responseData.getPayload())).readObject();
                jsonObjectMatcher.describeMismatch(jsonObject, description);
            }

            @Override
            public void describeTo(final Description description) {
                jsonObjectMatcher.describeTo(description);
            }
        };
    }
}
