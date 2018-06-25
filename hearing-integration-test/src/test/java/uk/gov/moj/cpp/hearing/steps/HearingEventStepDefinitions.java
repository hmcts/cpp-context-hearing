package uk.gov.moj.cpp.hearing.steps;

import static com.google.common.collect.Lists.newArrayList;
import static com.jayway.jsonassert.impl.matcher.IsCollectionWithSize.hasSize;
import static org.hamcrest.Matchers.greaterThan;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withoutJsonPath;
import static com.jayway.restassured.RestAssured.given;
import static java.lang.String.CASE_INSENSITIVE_ORDER;
import static java.lang.String.format;
import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.OK;
import static org.apache.http.HttpStatus.SC_ACCEPTED;
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
import uk.gov.moj.cpp.hearing.domain.HearingEventDefinition;
import uk.gov.moj.cpp.hearing.it.AbstractIT;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingEvent;
import uk.gov.moj.cpp.hearing.steps.data.HearingEventDefinitionData;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;
import com.jayway.jsonpath.ReadContext;
import com.jayway.restassured.response.Response;
import org.hamcrest.Matcher;

public class HearingEventStepDefinitions extends AbstractIT {

    private static final String MEDIA_TYPE_CREATE_EVENT_LOG = "application/vnd.hearing.log-hearing-event+json";
    private static final String MEDIA_TYPE_CREATE_EVENT_DEFINITIONS = "application/vnd.hearing.create-hearing-event-definitions+json";
    private static final String MEDIA_TYPE_QUERY_EVENT_DEFINITIONS = "application/vnd.hearing.hearing-event-definitions+json";

    private static final String FIELD_HEARING_EVENT_ID = "hearingEventId";
    private static final String FIELD_HEARING_EVENT_DEFINITION_ID = "hearingEventDefinitionId";
    private static final String FIELD_RECORDED_LABEL = "recordedLabel";
    private static final String FIELD_EVENT_TIME = "eventTime";
    private static final String FIELD_LAST_MODIFIED_TIME = "lastModifiedTime";
    private static final String FIELD_EVENT_DEFINITIONS = "eventDefinitions";
    private static final String FIELD_GENERIC_ID = "id";
    private static final String FIELD_CASE_ATTRIBUTE = "caseAttribute";
    private static final String FIELD_SEQUENCE = "sequence";
    private static final String FIELD_SEQUENCE_TYPE = "sequenceType";
    private static final String FIELD_GROUP_LABEL = "groupLabel";
    private static final String FIELD_ACTION_LABEL_EXTENSION = "actionLabelExtension";
    private static final String FIELD_ALTERABLE = "alterable";
    private static final String FIELD_ACTION_LABEL = "actionLabel";

    private static final String SEQUENCE_TYPE_SENTENCING = "SENTENCING";
    private static final String SEQUENCE_TYPE_PAUSE_RESUME = "PAUSE_RESUME";
    private static final String SEQUENCE_TYPE_NOT_REGISTERED = "NOT_REGISTERED";

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

    public static HearingEventDefinitionData andHearingEventDefinitionsAreAvailable(final HearingEventDefinitionData hearingEventDefinitions) {
        final String createEventDefinitionsEndPoint = ENDPOINT_PROPERTIES.getProperty("hearing.create-hearing-event-definitions");

        final JsonArrayBuilder eventDefinitionsArrayBuilder = createArrayBuilder();
        hearingEventDefinitions.getEventDefinitions().forEach(eventDefinition -> {
                    final JsonObjectBuilder eventDefinitionBuilder = createObjectBuilder();

                    eventDefinitionBuilder.add(FIELD_GENERIC_ID, eventDefinition.getId().toString());

                    if (eventDefinition.getCaseAttribute() != null) {
                        eventDefinitionBuilder.add(FIELD_CASE_ATTRIBUTE, eventDefinition.getCaseAttribute());
                    }
                    if (eventDefinition.getSequence() != null) {
                        eventDefinitionBuilder.add(FIELD_SEQUENCE, eventDefinition.getSequence());
                    }
                    if (eventDefinition.getSequenceType() != null) {
                        eventDefinitionBuilder.add(FIELD_SEQUENCE_TYPE, eventDefinition.getSequenceType());
                    }
                    if (eventDefinition.getGroupLabel() != null) {
                        eventDefinitionBuilder.add(FIELD_GROUP_LABEL, eventDefinition.getGroupLabel());
                    }
                    if (eventDefinition.getActionLabelExtension() != null) {
                        eventDefinitionBuilder.add(FIELD_ACTION_LABEL_EXTENSION, eventDefinition.getActionLabelExtension());
                    }

                    eventDefinitionBuilder.add(FIELD_ALTERABLE, eventDefinition.isAlterable());

                    eventDefinitionsArrayBuilder.add(eventDefinitionBuilder
                            .add(FIELD_ACTION_LABEL, eventDefinition.getActionLabel())
                            .add(FIELD_RECORDED_LABEL, eventDefinition.getRecordedLabel())
                    );
                }
        );

        final JsonObjectBuilder hearingEventDefinitionsPayloadBuilder = createObjectBuilder()
                .add(FIELD_GENERIC_ID, hearingEventDefinitions.getId().toString())
                .add(FIELD_EVENT_DEFINITIONS, eventDefinitionsArrayBuilder);

        final Response response = given().spec(requestSpec)
                .and().contentType(MEDIA_TYPE_CREATE_EVENT_DEFINITIONS)
                .and().header(USER_ID, getLoggedInUser())
                .and().body(hearingEventDefinitionsPayloadBuilder.build().toString())
                .when().post(createEventDefinitionsEndPoint)
                .then().extract().response();

        assertThat(response.getStatusCode(), equalTo(SC_ACCEPTED));

        final String queryEventDefinitionsUrl = getQueryEventDefinitionsUrl();
        poll(requestParams(queryEventDefinitionsUrl, MEDIA_TYPE_QUERY_EVENT_DEFINITIONS).withHeader(USER_ID, getLoggedInUser()))
                .until(
                        status().is(OK),
                        payload().isJson(
                                withJsonPath("$.eventDefinitions", hasSize(hearingEventDefinitions.getEventDefinitions().size()))
                        )
                );

        return hearingEventDefinitions;
    }


    public static void thenHearingEventDefinitionsAreRecorded(final HearingEventDefinitionData hearingEventDefinitions) {
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
                        conditionsOnJson.add(withJsonPath(format("$.eventDefinitions[%s].caseAttributes", index), hasSize(greaterThan(0))));
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

        poll(requestParams(getQueryEventDefinitionsUrl(), MEDIA_TYPE_QUERY_EVENT_DEFINITIONS).withHeader(USER_ID, getLoggedInUser()))
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


    private static String getQueryEventDefinitionsUrl() {
        final String queryEventDefinitionsEndPoint = ENDPOINT_PROPERTIES.getProperty("hearing.get-hearing-event-definitions");
        return format("%s/%s", getBaseUri(), queryEventDefinitionsEndPoint);
    }


}
