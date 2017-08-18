package uk.gov.moj.cpp.hearing.steps;

import static com.google.common.collect.Lists.newArrayList;
import static com.jayway.jsonassert.impl.matcher.IsCollectionWithSize.hasSize;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withoutJsonPath;
import static com.jayway.restassured.RestAssured.given;
import static java.lang.String.CASE_INSENSITIVE_ORDER;
import static java.lang.String.format;
import static java.util.UUID.randomUUID;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.Collectors.toList;
import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;
import static javax.ws.rs.core.Response.Status.OK;
import static org.apache.http.HttpStatus.SC_ACCEPTED;
import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.hamcrest.core.IsEqual.equalTo;
import static uk.gov.justice.services.common.http.HeaderConstants.USER_ID;
import static uk.gov.justice.services.test.utils.core.http.BaseUriProvider.getBaseUri;
import static uk.gov.justice.services.test.utils.core.http.RequestParamsBuilder.requestParams;
import static uk.gov.justice.services.test.utils.core.http.RestPoller.poll;
import static uk.gov.justice.services.test.utils.core.matchers.ResponsePayloadMatcher.payload;
import static uk.gov.justice.services.test.utils.core.matchers.ResponseStatusMatcher.status;

import uk.gov.justice.services.common.converter.ZonedDateTimes;
import uk.gov.moj.cpp.hearing.domain.HearingEventDefinition;
import uk.gov.moj.cpp.hearing.it.AbstractIT;
import uk.gov.moj.cpp.hearing.persist.entity.HearingEvent;
import uk.gov.moj.cpp.hearing.steps.data.DefenceCounselData;
import uk.gov.moj.cpp.hearing.steps.data.HearingEventDefinitionData;

import java.io.StringReader;
import java.text.MessageFormat;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;
import com.jayway.jsonpath.ReadContext;
import com.jayway.restassured.response.Response;
import org.hamcrest.Matcher;

public class HearingEventStepDefinitions extends AbstractIT {

    private static final String MEDIA_TYPE_CREATE_EVENT_LOG = "application/vnd.hearing.log-hearing-event+json";
    private static final String MEDIA_TYPE_QUERY_EVENT_LOG = "application/vnd.hearing.hearing-event-log+json";
    private static final String MEDIA_TYPE_CORRECT_HEARING_EVENT = "application/vnd.hearing.correct-hearing-event+json";
    private static final String MEDIA_TYPE_CREATE_EVENT_DEFINITIONS = "application/vnd.hearing.create-hearing-event-definitions+json";
    private static final String MEDIA_TYPE_QUERY_EVENT_DEFINITIONS = "application/vnd.hearing.hearing-event-definitions+json";
    private static final String MEDIA_TYPE_QUERY_EVENT_DEFINITION = "application/vnd.hearing.hearing-event-definition+json";

    private static final String FIELD_HEARING_EVENT_ID = "hearingEventId";
    private static final String FIELD_HEARING_EVENT_DEFINITION_ID = "hearingEventDefinitionId";
    private static final String FIELD_LATEST_HEARING_EVENT_ID = "latestHearingEventId";
    private static final String FIELD_RECORDED_LABEL = "recordedLabel";
    private static final String FIELD_EVENT_TIME = "eventTime";
    private static final String FIELD_LAST_MODIFIED_TIME = "lastModifiedTime";
    private static final String FIELD_ID = "id";
    private static final String FIELD_EVENT_DEFINITIONS = "eventDefinitions";

    private static final String SEQUENCE_TYPE_SENTENCING = "SENTENCING";
    private static final String SEQUENCE_TYPE_PAUSE_RESUME = "PAUSE_RESUME";
    private static final String SEQUENCE_TYPE_NOT_REGISTERED = "NOT_REGISTERED";

    public static void andHearingIsNotStarted(final UUID hearingId) {
        final String queryEventLogUrl = getQueryEventLogUrl(hearingId);

        // fail fast when hearing already has some events to begin with
        poll(requestParams(queryEventLogUrl, MEDIA_TYPE_QUERY_EVENT_LOG).withHeader(USER_ID, getLoggedInUser()))
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

    public static Response whenUserAttemptsToLogAHearingEvent(final HearingEvent hearingEvent) {
        final String createEventLogEndPoint = MessageFormat.format(ENDPOINT_PROPERTIES.getProperty("hearing.log-hearing-event"), hearingEvent.getHearingId());

        final JsonObjectBuilder hearingEventPayloadBuilder = createObjectBuilder()
                .add(FIELD_HEARING_EVENT_ID, hearingEvent.getId().toString())
                .add(FIELD_HEARING_EVENT_DEFINITION_ID, hearingEvent.getHearingEventDefinitionId().toString())
                .add(FIELD_RECORDED_LABEL, hearingEvent.getRecordedLabel())
                .add(FIELD_LAST_MODIFIED_TIME, ZonedDateTimes.toString(hearingEvent.getLastModifiedTime()));

        if (hearingEvent.getEventTime() != null) {
            hearingEventPayloadBuilder.add(FIELD_EVENT_TIME, ZonedDateTimes.toString(hearingEvent.getEventTime()));
        }

        return given().spec(requestSpec)
                .and().contentType(MEDIA_TYPE_CREATE_EVENT_LOG)
                .and().header(USER_ID, getLoggedInUser())
                .and().body(hearingEventPayloadBuilder.build().toString())
                .when().post(createEventLogEndPoint)
                .then().extract().response();
    }

    public static void whenUserLogsMultipleEvents(final List<HearingEvent> hearingEvent) {
        final Optional<Response> errors = hearingEvent.stream()
                .map(HearingEventStepDefinitions::whenUserAttemptsToLogAHearingEvent)
                .filter((Response response) -> response.getStatusCode() != SC_ACCEPTED)
                .findAny();

        assertThat(errors.isPresent(), is(false));
    }

    public static void whenUserLogsAnEvent(final HearingEvent hearingEvent) {
        final Response response = whenUserAttemptsToLogAHearingEvent(hearingEvent);

        assertThat(response.getStatusCode(), equalTo(SC_ACCEPTED));
    }

    public static void andUserLogsAnEvent(final HearingEvent hearingEvent) {
        whenUserLogsAnEvent(hearingEvent);
    }

    public static void andLogsAnotherEvent(final HearingEvent hearingEvent) {
        whenUserLogsAnEvent(hearingEvent);
    }

    public static Response whenUserCorrectsTheTimeOfTheHearingEvent(final HearingEvent hearingEvent, final ZonedDateTime newEventTime,
                                                                    final ZonedDateTime newLastModifiedTime, final UUID newHearingEventId) {
        final String correctEventEndPoint = MessageFormat.format(ENDPOINT_PROPERTIES.getProperty("hearing.correct-hearing-event"), hearingEvent.getHearingId(), hearingEvent.getId());

        final JsonObjectBuilder correctEventPayloadBuilder = createObjectBuilder()
                .add(FIELD_EVENT_TIME, ZonedDateTimes.toString(newEventTime))
                .add(FIELD_LAST_MODIFIED_TIME, ZonedDateTimes.toString(newLastModifiedTime))
                .add(FIELD_HEARING_EVENT_DEFINITION_ID, hearingEvent.getHearingEventDefinitionId().toString())
                .add(FIELD_RECORDED_LABEL, hearingEvent.getRecordedLabel())
                .add(FIELD_LATEST_HEARING_EVENT_ID, newHearingEventId.toString());

        return given().spec(requestSpec)
                .and().contentType(MEDIA_TYPE_CORRECT_HEARING_EVENT)
                .and().header(USER_ID, getLoggedInUser())
                .and().body(correctEventPayloadBuilder.build().toString())
                .when().post(correctEventEndPoint)
                .then().extract().response();
    }

    public static void thenOnlySpecifiedHearingEventIsRecorded(final HearingEvent hearingEvent) {
        final String queryEventLogUrl = getQueryEventLogUrl(hearingEvent.getHearingId());

        poll(requestParams(queryEventLogUrl, MEDIA_TYPE_QUERY_EVENT_LOG).withHeader(USER_ID, getLoggedInUser()))
                .until(
                        status().is(OK),
                        payload().isJson(allOf(
                                withJsonPath("$.hearingId", equalTo(hearingEvent.getHearingId().toString())),
                                withJsonPath("$.events", hasSize(1)),
                                withJsonPath("$.events[0].hearingEventId", equalTo(hearingEvent.getId().toString())),
                                withJsonPath("$.events[0].recordedLabel", equalTo(hearingEvent.getRecordedLabel())),
                                withJsonPath("$.events[0].eventTime", equalTo(ZonedDateTimes.toString(hearingEvent.getEventTime()))),
                                withJsonPath("$.events[0].lastModifiedTime", equalTo(ZonedDateTimes.toString(hearingEvent.getLastModifiedTime())))
                        ))
                );
    }

    public static void thenHearingEventIsRecorded(final HearingEvent hearingEvent) {
        final String queryEventLogUrl = getQueryEventLogUrl(hearingEvent.getHearingId());

        poll(requestParams(queryEventLogUrl, MEDIA_TYPE_QUERY_EVENT_LOG).withHeader(USER_ID, getLoggedInUser()))
                .until(
                        status().is(OK),
                        payload().isJson(allOf(
                                withJsonPath("$.hearingId", equalTo(hearingEvent.getHearingId().toString())),
                                withJsonPath("$.events[*].hearingEventId", hasItems(hearingEvent.getId().toString())),
                                withJsonPath("$.events[*].recordedLabel", hasItems(hearingEvent.getRecordedLabel())),
                                withJsonPath("$.events[*].eventTime", hasItems(ZonedDateTimes.toString(hearingEvent.getEventTime()))),
                                withJsonPath("$.events[*].lastModifiedTime", hasItems(ZonedDateTimes.toString(hearingEvent.getLastModifiedTime())))
                        ))
                );
    }


    public static void thenTheHearingEventHasTheUpdatedEventTime(final HearingEvent hearingEvent, final ZonedDateTime newEventTime,
                                                                 final ZonedDateTime newLastModifiedTime, final UUID newHearingEventId) {
        final String queryEventLogUrl = getQueryEventLogUrl(hearingEvent.getHearingId());

        poll(requestParams(queryEventLogUrl, MEDIA_TYPE_QUERY_EVENT_LOG).withHeader(USER_ID, getLoggedInUser()))
                .until(
                        status().is(OK),
                        payload().isJson(allOf(
                                withJsonPath("$.hearingId", equalTo(hearingEvent.getHearingId().toString())),
                                withJsonPath("$.events[*].hearingEventId", hasItems(newHearingEventId.toString())),
                                withJsonPath("$.events[*].recordedLabel", hasItems(hearingEvent.getRecordedLabel())),
                                withJsonPath("$.events[*].eventTime", hasItems(ZonedDateTimes.toString(newEventTime))),
                                withJsonPath("$.events[*].lastModifiedTime", hasItems(ZonedDateTimes.toString(newLastModifiedTime)))
                        ))
                );
    }

    public static void thenItFailsForMissingEventTime(final Response response) {
        assertThat(response.getStatusCode(), equalTo(SC_BAD_REQUEST));
        assertThat(response.getBody().jsonPath().prettify(), is(allOf(
                isJson(),
                hasJsonPath("$.validationErrors.message", equalTo("#: required key [eventTime] not found"))
        )));
    }

    public static void thenTheEventsShouldBeListedInTheSpecifiedOrder(final UUID hearingId, final List<HearingEvent> events) {
        final String queryEventLogUrl = getQueryEventLogUrl(hearingId);

        final List<Matcher<? super ReadContext>> conditionsOnJson = new ArrayList<>();
        conditionsOnJson.add(withJsonPath("$.hearingId", equalTo(hearingId.toString())));
        conditionsOnJson.add(withJsonPath("$.events", hasSize(events.size())));

        IntStream.range(0, events.size())
                .forEach(ix ->
                        {
                            final HearingEvent hearingEvent = events.get(ix);
                            conditionsOnJson.add(withJsonPath(format("$.events[%s].hearingEventId", ix), equalTo(hearingEvent.getId().toString())));
                            conditionsOnJson.add(withJsonPath(format("$.events[%s].recordedLabel", ix), equalTo(hearingEvent.getRecordedLabel())));
                            conditionsOnJson.add(withJsonPath(format("$.events[%s].eventTime", ix), equalTo(ZonedDateTimes.toString(hearingEvent.getEventTime()))));
                            conditionsOnJson.add(withJsonPath(format("$.events[%s].lastModifiedTime", ix), equalTo(ZonedDateTimes.toString(hearingEvent.getLastModifiedTime()))));
                        }
                );

        poll(requestParams(queryEventLogUrl, MEDIA_TYPE_QUERY_EVENT_LOG).withHeader(USER_ID, getLoggedInUser()))
                .with().timeout(20, SECONDS)
                .with().logging()
                .until(
                        status().is(OK),
                        payload().isJson(allOf(conditionsOnJson))
                );
    }

    public static void andHearingEventDefinitionsAreAvailable(final HearingEventDefinitionData hearingEventDefinitions) {
        final String createEventDefinitionsEndPoint = ENDPOINT_PROPERTIES.getProperty("hearing.create-hearing-event-definitions");

        final JsonArrayBuilder eventDefinitionsArrayBuilder = createArrayBuilder();
        hearingEventDefinitions.getEventDefinitions().forEach(eventDefinition -> {
                    final JsonObjectBuilder eventDefinitionBuilder = createObjectBuilder();

                    eventDefinitionBuilder.add("id", eventDefinition.getId().toString());

                    if (eventDefinition.getCaseAttribute() != null) {
                        eventDefinitionBuilder.add("caseAttribute", eventDefinition.getCaseAttribute());
                    }
                    if (eventDefinition.getSequence() != null) {
                        eventDefinitionBuilder.add("sequence", eventDefinition.getSequence());
                    }
                    if (eventDefinition.getSequenceType() != null) {
                        eventDefinitionBuilder.add("sequenceType", eventDefinition.getSequenceType());
                    }
                    if (eventDefinition.getGroupLabel() != null) {
                        eventDefinitionBuilder.add("groupLabel", eventDefinition.getGroupLabel());
                    }
                    if (eventDefinition.getActionLabelExtension() != null) {
                        eventDefinitionBuilder.add("actionLabelExtension", eventDefinition.getActionLabelExtension());
                    }

                    eventDefinitionBuilder.add("alterable", eventDefinition.isAlterable());

                    if (eventDefinition.getActionLabelExtension() != null) {
                        eventDefinitionBuilder.add("actionLabelExtension", eventDefinition.getActionLabelExtension());
                    }

                    eventDefinitionsArrayBuilder.add(eventDefinitionBuilder
                            .add("actionLabel", eventDefinition.getActionLabel())
                            .add("recordedLabel", eventDefinition.getRecordedLabel())
                    );
                }
        );

        final JsonObjectBuilder hearingEventDefinitionsPayloadBuilder = createObjectBuilder()
                .add(FIELD_ID, hearingEventDefinitions.getId().toString())
                .add(FIELD_EVENT_DEFINITIONS, eventDefinitionsArrayBuilder);

        final Response response = given().spec(requestSpec)
                .and().contentType(MEDIA_TYPE_CREATE_EVENT_DEFINITIONS)
                .and().header(USER_ID, getLoggedInUser())
                .and().body(hearingEventDefinitionsPayloadBuilder.build().toString())
                .when().post(createEventDefinitionsEndPoint)
                .then().extract().response();

        assertThat(response.getStatusCode(), equalTo(SC_ACCEPTED));

        String queryEventDefinitionsUrl = getQueryEventDefinitionsUrl(randomUUID());
        poll(requestParams(queryEventDefinitionsUrl, MEDIA_TYPE_QUERY_EVENT_DEFINITIONS).withHeader(USER_ID, getLoggedInUser()))
                .until(
                        status().is(OK),
                        payload().isJson(
                                withJsonPath("$.eventDefinitions", hasSize(hearingEventDefinitions.getEventDefinitions().size()))
                        )
                );
    }

    public static void whenHearingEventDefinitionsAreUpdated(final HearingEventDefinitionData hearingEventDefinitions) {
        andHearingEventDefinitionsAreAvailable(hearingEventDefinitions);
    }

    public static void thenHearingEventDefinitionsAreRecorded(final UUID hearingId, final HearingEventDefinitionData hearingEventDefinitions) {
        final List<HearingEventDefinition> eventDefinitions = newArrayList(hearingEventDefinitions.getEventDefinitions());

        sortBasedOnSequenceTypeSequenceAndActionLabel(eventDefinitions);

        final List<Matcher<? super ReadContext>> conditionsOnJson = new ArrayList<>();
        conditionsOnJson.add(withJsonPath("$.eventDefinitions", hasSize(eventDefinitions.size())));

        IntStream.range(0, eventDefinitions.size())
                .forEach(index -> {
                    final HearingEventDefinition eventDefinition = eventDefinitions.get(index);
                    conditionsOnJson.add(withJsonPath(format("$.eventDefinitions[%s].id", index), equalTo(eventDefinition.getId().toString())));
                    conditionsOnJson.add(withJsonPath(format("$.eventDefinitions[%s].actionLabel", index), equalTo(eventDefinition.getActionLabel())));
                    conditionsOnJson.add(withJsonPath(format("$.eventDefinitions[%s].recordedLabel", index), equalTo(eventDefinition.getRecordedLabel())));
                    conditionsOnJson.add(withJsonPath(format("$.eventDefinitions[%s].alterable", index), equalTo(eventDefinition.isAlterable())));

                    if (eventDefinition.getCaseAttribute() != null) {
                        conditionsOnJson.add(withJsonPath(format("$.eventDefinitions[%s].caseAttributes", index), hasSize(0)));
                    } else {
                        conditionsOnJson.add(withoutJsonPath(format("$.eventDefinitions[%s].caseAttributes", index)));
                    }

                    if (eventDefinition.getSequence() != null) {
                        conditionsOnJson.add(withJsonPath(format("$.eventDefinitions[%s].sequence.id", index), equalTo(eventDefinition.getSequence())));
                        conditionsOnJson.add(withJsonPath(format("$.eventDefinitions[%s].sequence.type", index), equalTo(eventDefinition.getSequenceType())));
                    } else {
                        conditionsOnJson.add(withoutJsonPath(format("$.eventDefinitions[%s].sequence", index)));
                    }

                    if (eventDefinition.getGroupLabel() != null) {
                        conditionsOnJson.add(withJsonPath(format("$.eventDefinitions[%s].groupLabel", index), equalTo(eventDefinition.getGroupLabel())));
                    } else {
                        conditionsOnJson.add(withoutJsonPath(format("$.eventDefinitions[%s].groupLabel", index)));
                    }

                    if (eventDefinition.getActionLabelExtension() != null) {
                        conditionsOnJson.add(withJsonPath(format("$.eventDefinitions[%s].actionLabelExtension", index), equalTo(eventDefinition.getActionLabelExtension())));
                    } else {
                        conditionsOnJson.add(withoutJsonPath(format("$.eventDefinitions[%s].actionLabelExtension", index)));
                    }

                });

        poll(requestParams(getQueryEventDefinitionsUrl(hearingId), MEDIA_TYPE_QUERY_EVENT_DEFINITIONS).withHeader(USER_ID, getLoggedInUser()))
                .until(
                        status().is(OK),
                        payload().isJson(allOf(conditionsOnJson))
                );
    }

    public static void thenHearingEventAlterableFlagIs(final HearingEvent hearingEvent, final boolean alterableExpectation) {
        final String payload = poll(requestParams(getQueryEventLogUrl(hearingEvent.getHearingId()), MEDIA_TYPE_QUERY_EVENT_LOG).withHeader(USER_ID, getLoggedInUser()))
                .with().logging()
                .until(
                        status().is(OK),
                        payload().isJson(
                                withJsonPath("$.events[*].hearingEventId", hasItems(hearingEvent.getId().toString()))
                        ))
                .getPayload();

        final JsonObject jsonPayload = getJsonObjectFromString(payload);
        final boolean actualAlterableFlag = jsonPayload.getJsonArray("events").getValuesAs(JsonObject.class).stream()
                .filter(event -> event.getString("hearingEventId").equals(hearingEvent.getId().toString()))
                .map(event -> event.getBoolean("alterable"))
                .findFirst().orElseThrow(() -> new AssertionError(format("Hearing event %s could not be found in the hearing event logs for hearing %s", hearingEvent.getId(), hearingEvent.getHearingId())));

        assertThat(actualAlterableFlag, is(alterableExpectation));
    }

    public static void thenHearingEventDefinitionsShouldProvideOptionToLogEventWithDefendantAndDefenceCouncil(final UUID hearingId, final List<DefenceCounselData> defenceCounsels) {
        final List<String> counselIds = defenceCounsels.stream().map(defenceCounsel -> defenceCounsel.getPersonId().toString()).collect(toList());
        final List<String> defendantIds = defenceCounsels.stream().map(defenceCounsel -> defenceCounsel.getMapOfDefendantIdToNames().keySet().stream().findFirst().get().toString()).collect(toList());

        poll(requestParams(getQueryEventDefinitionsUrl(hearingId), MEDIA_TYPE_QUERY_EVENT_DEFINITIONS).withHeader(USER_ID, getLoggedInUser()))
                .until(
                        status().is(OK),
                        payload().isJson(allOf(
                                withJsonPath("$.eventDefinitions[*].actionLabel", hasItems("<counsel.name>")),
                                withJsonPath("$.eventDefinitions[*].recordedLabel", hasItems("Defence <counsel.name> mitigated for <defendant.name>")),
                                withJsonPath("$.eventDefinitions[*].caseAttributes[*].['defendant.name']", containsInAnyOrder(defendantIds.toArray())),
                                withJsonPath("$.eventDefinitions[*].caseAttributes[*].['counsel.name']", containsInAnyOrder(counselIds.toArray()))
                        ))
                );
    }

    public static void thenHearingEventDefinitionIsStillAvailable(final HearingEventDefinition hearingEventDefinition) {
        final List<Matcher<? super ReadContext>> conditionsOnJson = new ArrayList<>();
        conditionsOnJson.add(withJsonPath("$.id", equalTo(hearingEventDefinition.getId().toString())));
        conditionsOnJson.add(withJsonPath("$.actionLabel", equalTo(hearingEventDefinition.getActionLabel())));
        conditionsOnJson.add(withJsonPath("$.recordedLabel", equalTo(hearingEventDefinition.getRecordedLabel())));
        conditionsOnJson.add(withJsonPath("$.alterable", equalTo(hearingEventDefinition.isAlterable())));

        if (hearingEventDefinition.getCaseAttribute() != null) {
            conditionsOnJson.add(withJsonPath("$.caseAttributes", hasSize(0)));
        } else {
            conditionsOnJson.add(withoutJsonPath("$.caseAttributes"));
        }

        if (hearingEventDefinition.getSequence() != null) {
            conditionsOnJson.add(withJsonPath("$.sequence.id", equalTo(hearingEventDefinition.getSequence())));
            conditionsOnJson.add(withJsonPath("$.sequence.type", equalTo(hearingEventDefinition.getSequenceType())));
        } else {
            conditionsOnJson.add(withoutJsonPath("$.sequence"));
        }

        if (hearingEventDefinition.getGroupLabel() != null) {
            conditionsOnJson.add(withJsonPath("$.groupLabel", equalTo(hearingEventDefinition.getGroupLabel())));
        } else {
            conditionsOnJson.add(withoutJsonPath("$.groupLabel"));
        }

        if (hearingEventDefinition.getActionLabelExtension() != null) {
            conditionsOnJson.add(withJsonPath("$.actionLabelExtension", equalTo(hearingEventDefinition.getActionLabelExtension())));
        } else {
            conditionsOnJson.add(withoutJsonPath("$.actionLabelExtension"));
        }

        poll(requestParams(getQueryEventDefinitionUrl(randomUUID(), hearingEventDefinition.getId()), MEDIA_TYPE_QUERY_EVENT_DEFINITION).withHeader(USER_ID, getLoggedInUser()))
                .until(
                        status().is(OK),
                        payload().isJson(allOf(conditionsOnJson))
                );

    }

    private static void sortBasedOnSequenceTypeSequenceAndActionLabel(final List<HearingEventDefinition> eventDefinitions) {
        eventDefinitions.sort((ed1, ed2) -> ComparisonChain.start()
                .compare(ed1.getSequenceType(), ed2.getSequenceType(), Ordering.explicit(SEQUENCE_TYPE_SENTENCING, SEQUENCE_TYPE_PAUSE_RESUME, SEQUENCE_TYPE_NOT_REGISTERED).nullsLast())
                .compare(ed1.getSequence(), ed2.getSequence(), Ordering.natural().nullsLast())
                .compare(ed1.getActionLabel(), ed2.getActionLabel(), Ordering.from(CASE_INSENSITIVE_ORDER))
                .result());
    }

    private static String getQueryEventLogUrl(final UUID hearingId) {
        final String queryEventLogEndPoint = MessageFormat.format(ENDPOINT_PROPERTIES.getProperty("hearing.get-hearing-event-log"), hearingId);
        return format("%s/%s", getBaseUri(), queryEventLogEndPoint);
    }

    private static String getQueryEventDefinitionsUrl(final UUID hearingId) {
        final String queryEventDefinitionsEndPoint = MessageFormat.format(ENDPOINT_PROPERTIES.getProperty("hearing.get-hearing-event-definitions"), hearingId);
        return format("%s/%s", getBaseUri(), queryEventDefinitionsEndPoint);
    }

    private static String getQueryEventDefinitionUrl(final UUID hearingId, final UUID hearingEventDefinitionId) {
        final String queryEventDefinitionsEndPoint = MessageFormat.format(ENDPOINT_PROPERTIES.getProperty("hearing.get-hearing-event-definition"), hearingId, hearingEventDefinitionId);
        return format("%s/%s", getBaseUri(), queryEventDefinitionsEndPoint);
    }

    private static JsonObject getJsonObjectFromString(final String message) {
        try (final JsonReader reader = Json.createReader(new StringReader(message))) {
            return reader.readObject();
        }
    }

}
