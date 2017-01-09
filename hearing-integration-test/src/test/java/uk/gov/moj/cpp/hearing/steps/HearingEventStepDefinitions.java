package uk.gov.moj.cpp.hearing.steps;

import static com.jayway.jsonassert.impl.matcher.IsCollectionWithSize.hasSize;
import com.jayway.jsonpath.ReadContext;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static com.jayway.restassured.RestAssured.given;
import static java.lang.String.format;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import static javax.json.Json.createObjectBuilder;
import static javax.ws.rs.core.Response.Status.OK;
import static org.apache.http.HttpStatus.SC_ACCEPTED;
import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import org.hamcrest.Matcher;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static uk.gov.justice.services.common.http.HeaderConstants.USER_ID;
import static uk.gov.justice.services.test.utils.core.http.BaseUriProvider.getBaseUri;
import static uk.gov.justice.services.test.utils.core.http.RequestParamsBuilder.requestParams;
import static uk.gov.justice.services.test.utils.core.http.RestPoller.poll;
import static uk.gov.justice.services.test.utils.core.matchers.ResponsePayloadMatcher.payload;
import static uk.gov.justice.services.test.utils.core.matchers.ResponseStatusMatcher.status;

import uk.gov.justice.services.common.converter.ZonedDateTimes;
import uk.gov.moj.cpp.hearing.it.AbstractIT;
import uk.gov.moj.cpp.hearing.persist.entity.HearingEvent;

import java.text.MessageFormat;
import java.util.UUID;

import javax.json.JsonObjectBuilder;

import com.jayway.restassured.response.Response;
import static uk.gov.moj.cpp.hearing.steps.data.HearingEventDataFactory.hearingStartedEvent;

public class HearingEventStepDefinitions extends AbstractIT {

    private static final String MEDIA_TYPE_CREATE_EVENT_LOG = "application/vnd.hearing.log-hearing-event+json";
    private static final String MEDIA_TYPE_QUERY_EVENT_LOG = "application/vnd.hearing.hearing-event-log+json";
    private static final String MEDIA_TYPE_CORRECT_HEARING_EVENT = "application/vnd.hearing.correct-hearing-event+json";

    private static final String FIELD_HEARING_EVENT_ID = "id";
    private static final String FIELD_RECORDED_LABEL = "recordedLabel";
    private static final String FIELD_TIMESTAMP = "timestamp";

    public static void andHearingIsNotStarted(final UUID userId, final UUID hearingId) {
        final String queryEventLogUrl = getQueryEventLogUrl(hearingId);

        // fail fast when events for not empty for the hearing
        poll(requestParams(queryEventLogUrl, MEDIA_TYPE_QUERY_EVENT_LOG).withHeader(USER_ID, userId))
                .ignoring(
                        status().is(OK),
                        payload().isJson(allOf(
                                withJsonPath("$.hearingId", equalTo(hearingId.toString())),
                                withJsonPath("$.events", hasSize(0))
                        ))
                )
                .until(
                        status().is(OK),
                        payload().isJson(allOf(
                                withJsonPath("$.hearingId", equalTo(hearingId.toString())),
                                withJsonPath("$.events", hasSize(0))
                        ))
                );
    }

    public static Response whenUserAttemptsToStartAHearing(final UUID userId, final HearingEvent hearingEvent) {
        final String createEventLogEndPoint = MessageFormat.format(ENDPOINT_PROPERTIES.getProperty("hearing.log-hearing-event"), hearingEvent.getHearingId());

        final JsonObjectBuilder hearingEventPayloadBuilder = createObjectBuilder()
                .add(FIELD_HEARING_EVENT_ID, hearingEvent.getId().toString())
                .add(FIELD_RECORDED_LABEL, hearingEvent.getRecordedLabel());

        if (hearingEvent.getTimestamp() != null) {
            hearingEventPayloadBuilder.add(FIELD_TIMESTAMP, ZonedDateTimes.toString(hearingEvent.getTimestamp()));
        }

        return given().spec(requestSpec)
                .and().contentType(MEDIA_TYPE_CREATE_EVENT_LOG)
                .and().header(USER_ID, userId)
                .and().body(hearingEventPayloadBuilder.build().toString())
                .when().post(createEventLogEndPoint)
                .then().extract().response();
    }

    public static void whenUserLogsAnEvent(final UUID userId, final HearingEvent hearingEvent) {
        final Response response = whenUserAttemptsToStartAHearing(userId, hearingEvent);

        assertThat(response.getStatusCode(), equalTo(SC_ACCEPTED));
    }

    public static Response andCorrectsTheTimeOfThatHearingEvent(final UUID userId, final HearingEvent hearingEvent, final ZonedDateTime newTimestamp) {
        final String correctEventEndPoint = MessageFormat.format(ENDPOINT_PROPERTIES.getProperty("hearing.correct-hearing-event"), hearingEvent.getHearingId(), hearingEvent.getId());

        final JsonObjectBuilder correctEventPayloadBuilder = createObjectBuilder()
                .add(FIELD_TIMESTAMP, ZonedDateTimes.toString(newTimestamp));

        return given().spec(requestSpec)
                .and().contentType(MEDIA_TYPE_CORRECT_HEARING_EVENT)
                .and().header(USER_ID, userId)
                .and().body(correctEventPayloadBuilder.build().toString())
                .when().post(correctEventEndPoint)
                .then().extract().response();
    }

    public static void thenHearingEventIsRecorded(final UUID userId, final HearingEvent hearingEvent) {
        final String queryEventLogUrl = getQueryEventLogUrl(hearingEvent.getHearingId());

        poll(requestParams(queryEventLogUrl, MEDIA_TYPE_QUERY_EVENT_LOG).withHeader(USER_ID, userId))
                .until(
                        status().is(OK),
                        payload().isJson(allOf(
                                withJsonPath("$.hearingId", equalTo(hearingEvent.getHearingId().toString())),
                                withJsonPath("$.events", hasSize(1)),
                                withJsonPath("$.events[0].id", equalTo(hearingEvent.getId().toString())),
                                withJsonPath("$.events[0].recordedLabel", equalTo(hearingEvent.getRecordedLabel())),
                                withJsonPath("$.events[0].timestamp", equalTo(ZonedDateTimes.toString(hearingEvent.getTimestamp())))
                        ))
                );
    }

    public static void thenTheHearingEventHasTheUpdatedTimestamp(final UUID userId, final UUID hearingId, final ZonedDateTime newTimestamp) {
        final String queryEventLogUrl = getQueryEventLogUrl(hearingId);

        poll(requestParams(queryEventLogUrl, MEDIA_TYPE_QUERY_EVENT_LOG).withHeader(USER_ID, userId))
                .until(
                        status().is(OK),
                        payload().isJson(allOf(
                                withJsonPath("$.hearingId", equalTo(hearingId.toString())),
                                withJsonPath("$.events", hasSize(1)),
                                withJsonPath("$.events[0].timestamp", equalTo(ZonedDateTimes.toString(newTimestamp)))
                        ))
                );
    }

    public static void thenItFailsForMissingTimestamp(final Response response) {
        assertThat(response.getStatusCode(), equalTo(SC_BAD_REQUEST));
        assertThat(response.getBody().jsonPath().prettify(), is(allOf(
                isJson(),
                hasJsonPath("$.validationErrors.message", equalTo("#: required key [timestamp] not found"))
        )));
    }

    public static void thenTheEventsShouldBeListedInTheSpecifiedOrder(final UUID userId, final UUID hearingId, final List<HearingEvent> events) {
        final String queryEventLogUrl = getQueryEventLogUrl(hearingId);

        final List<Matcher<? super ReadContext>> conditionsOnJson = new ArrayList<>();
        conditionsOnJson.add(withJsonPath("$.hearingId", equalTo(hearingId.toString())));
        conditionsOnJson.add(withJsonPath("$.events", hasSize(events.size())));

        IntStream.range(0, events.size())
            .forEach(ix ->
                    {
                        conditionsOnJson.add(withJsonPath("$.events[" + ix + "].id", equalTo(events.get(ix).getId().toString())));
                        conditionsOnJson.add(withJsonPath("$.events[" + ix + "].recordedLabel", equalTo(events.get(ix).getRecordedLabel())));
                        conditionsOnJson.add(withJsonPath("$.events[" + ix + "].timestamp", equalTo(ZonedDateTimes.toString(events.get(ix).getTimestamp()))));
                    }
            );

        poll(requestParams(queryEventLogUrl, MEDIA_TYPE_QUERY_EVENT_LOG).withHeader(USER_ID, userId))
                .until(
                        status().is(OK),
                        payload().isJson(allOf(conditionsOnJson))
                );
    }

    private static String getQueryEventLogUrl(final UUID hearingId) {
        final String queryEventLogEndPoint = MessageFormat.format(ENDPOINT_PROPERTIES.getProperty("hearing.get-hearing-event-log"), hearingId);
        return format("%s/%s", getBaseUri(), queryEventLogEndPoint);
    }
}
