package uk.gov.moj.cpp.hearing.steps;

import static com.google.common.collect.Lists.newArrayList;
import static com.jayway.jsonassert.impl.matcher.IsCollectionWithSize.hasSize;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withoutJsonPath;
import static com.jayway.restassured.RestAssured.given;
import static java.lang.String.CASE_INSENSITIVE_ORDER;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.UUID.fromString;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;
import static javax.ws.rs.core.Response.Status.OK;
import static org.apache.http.HttpStatus.SC_ACCEPTED;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.IsEqual.equalTo;
import static uk.gov.justice.services.common.http.HeaderConstants.USER_ID;
import static uk.gov.justice.services.test.utils.core.http.BaseUriProvider.getBaseUri;
import static uk.gov.justice.services.test.utils.core.http.RequestParamsBuilder.requestParams;
import static uk.gov.justice.services.test.utils.core.matchers.ResponsePayloadMatcher.payload;
import static uk.gov.justice.services.test.utils.core.matchers.ResponseStatusMatcher.status;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.INTEGER;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.moj.cpp.hearing.it.AbstractIT.ENDPOINT_PROPERTIES;
import static uk.gov.moj.cpp.hearing.it.AbstractIT.getLoggedInUser;
import static uk.gov.moj.cpp.hearing.it.AbstractIT.getRequestSpec;
import static uk.gov.moj.cpp.hearing.steps.HearingStepDefinitions.givenAUserHasLoggedInAsACourtClerk;
import static uk.gov.moj.cpp.hearing.utils.RestUtils.poll;

import uk.gov.moj.cpp.hearing.domain.HearingEventDefinition;
import uk.gov.moj.cpp.hearing.steps.data.HearingEventDefinitionData;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.jayway.restassured.response.Response;
import org.hamcrest.Matcher;

public class HearingEventStepDefinitions {

    public static final String SEQUENCE_TYPE_PAUSE_RESUME = "PAUSE_RESUME";
    public static final String SEQUENCE_TYPE_NOT_REGISTERED = "NOT_REGISTERED";
    public static final String RECORDED_LABEL_START_HEARING = "Start Hearing";
    public static final String RECORDED_LABEL_END_HEARING = "Hearing ended";
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
    private static final String FIELD_ACTION_SEQUENCE = "actionSequence";
    private static final String FIELD_GROUP_SEQUENCE = "groupSequence";
    private static final String FIELD_GROUP_LABEL = "groupLabel";
    private static final String FIELD_ALTERABLE = "alterable";
    private static final String FIELD_ACTION_LABEL = "actionLabel";
    private static final UUID START_HEARING_EVENT_DEFINITION_ID = fromString("b71e7d2a-d3b3-4a55-a393-6d451767fc05");
    private static final UUID RESUME_HEARING_EVENT_DEFINITION_ID = fromString("64476e43-2138-46d5-b58b-848582cf9b07");
    private static final UUID PAUSE_HEARING_EVENT_DEFINITION_ID = fromString("160ecb51-29ee-4954-bbbf-daab18a24fbb");
    private static final UUID END_HEARING_EVENT_DEFINITION_ID = fromString("0df93f18-0a21-40f5-9fb3-da4749cd70fe");

    private static HearingEventDefinitionData hearingEventDefinitionData;

    public static void stubHearingEventDefinitions() {
        UUID userId = randomUUID();
        givenAUserHasLoggedInAsACourtClerk(userId);
        hearingEventDefinitionData = andHearingEventDefinitionsAreAvailable(hearingDefinitionData(hearingDefinitions()), getLoggedInUser());
    }

    public static HearingEventDefinitionData andHearingEventDefinitionsAreAvailable(final HearingEventDefinitionData hearingEventDefinitions, final UUID userId) {
        final String createEventDefinitionsEndPoint = ENDPOINT_PROPERTIES.getProperty("hearing.create-hearing-event-definitions");

        final JsonArrayBuilder eventDefinitionsArrayBuilder = createArrayBuilder();
        hearingEventDefinitions.getEventDefinitions().forEach(eventDefinition -> {
                    final JsonObjectBuilder eventDefinitionBuilder = createObjectBuilder();

                    eventDefinitionBuilder.add(FIELD_GENERIC_ID, eventDefinition.getId().toString());

                    if (eventDefinition.getCaseAttribute() != null) {
                        eventDefinitionBuilder.add(FIELD_CASE_ATTRIBUTE, eventDefinition.getCaseAttribute());
                    }
                    if (eventDefinition.getActionLabel() != null) {
                        eventDefinitionBuilder.add(FIELD_ACTION_LABEL, eventDefinition.getActionLabel());
                    }
                    if (eventDefinition.getActionSequence() != null) {
                        eventDefinitionBuilder.add(FIELD_ACTION_SEQUENCE, eventDefinition.getActionSequence());
                    }
                    if (eventDefinition.getGroupLabel() != null) {
                        eventDefinitionBuilder.add(FIELD_GROUP_LABEL, eventDefinition.getGroupLabel());
                    }
                    if (eventDefinition.getGroupSequence() != null) {
                        eventDefinitionBuilder.add(FIELD_GROUP_SEQUENCE, eventDefinition.getGroupSequence());
                    }

                    eventDefinitionBuilder.add(FIELD_ALTERABLE, eventDefinition.isAlterable());

                    eventDefinitionsArrayBuilder.add(eventDefinitionBuilder
                            .add(FIELD_RECORDED_LABEL, eventDefinition.getRecordedLabel())
                    );
                }
        );

        final JsonObjectBuilder hearingEventDefinitionsPayloadBuilder = createObjectBuilder()
                .add(FIELD_GENERIC_ID, hearingEventDefinitions.getId().toString())
                .add(FIELD_EVENT_DEFINITIONS, eventDefinitionsArrayBuilder);

        final Response response = given().spec(getRequestSpec())
                .and().contentType(MEDIA_TYPE_CREATE_EVENT_DEFINITIONS)
                .and().header(USER_ID, userId)
                .and().body(hearingEventDefinitionsPayloadBuilder.build().toString())
                .when().post(createEventDefinitionsEndPoint)
                .then().extract().response();

        assertThat(response.getStatusCode(), equalTo(SC_ACCEPTED));

        final String queryEventDefinitionsUrl = getQueryEventDefinitionsUrl();

        poll(requestParams(queryEventDefinitionsUrl, MEDIA_TYPE_QUERY_EVENT_DEFINITIONS).withHeader(USER_ID, userId))
                .until(
                        status().is(OK),
                        payload().isJson(allOf(
                                getMatchers(hearingEventDefinitions)))

                );

        return hearingEventDefinitions;
    }

    private static HearingEventDefinitionData hearingDefinitionData(final List<HearingEventDefinition> hearingEventDefinitions) {
        return new HearingEventDefinitionData(randomUUID(), hearingEventDefinitions);
    }

    private static List<HearingEventDefinition> hearingDefinitions() {
        return asList(
                new HearingEventDefinition(randomUUID(), RECORDED_LABEL_START_HEARING, INTEGER.next(), STRING.next(), "SENTENCING", STRING.next(), INTEGER.next(), false),
                new HearingEventDefinition(randomUUID(), RECORDED_LABEL_END_HEARING, INTEGER.next(), RECORDED_LABEL_END_HEARING, "SENTENCING", STRING.next(), INTEGER.next(), false),
                new HearingEventDefinition(START_HEARING_EVENT_DEFINITION_ID, "Start Hearing", INTEGER.next(), STRING.next(), "SENTENCING", STRING.next(), INTEGER.next(), false),
                new HearingEventDefinition(randomUUID(), "Identify defendant", INTEGER.next(), STRING.next(), "SENTENCING", STRING.next(), INTEGER.next(), true),
                new HearingEventDefinition(randomUUID(), "Take Plea", INTEGER.next(), STRING.next(), "SENTENCING", STRING.next(), INTEGER.next(), true),
                new HearingEventDefinition(randomUUID(), "Prosecution Opening", INTEGER.next(), STRING.next(), "SENTENCING", STRING.next(), INTEGER.next(), true),
                new HearingEventDefinition(randomUUID(), "<counsel.name>", INTEGER.next(), STRING.next(), "SENTENCING", STRING.next(), INTEGER.next(), true),
                new HearingEventDefinition(randomUUID(), "Sentencing", INTEGER.next(), STRING.next(), "SENTENCING", STRING.next(), INTEGER.next(), true),
                new HearingEventDefinition(END_HEARING_EVENT_DEFINITION_ID, "End Hearing", INTEGER.next(), STRING.next(), "SENTENCING", STRING.next(), INTEGER.next(), false),
                new HearingEventDefinition(PAUSE_HEARING_EVENT_DEFINITION_ID, "Pause", INTEGER.next(), STRING.next(), "PAUSE_RESUME", STRING.next(), INTEGER.next(), false),
                new HearingEventDefinition(RESUME_HEARING_EVENT_DEFINITION_ID, "Resume", INTEGER.next(), STRING.next(), "PAUSE_RESUME", STRING.next(), INTEGER.next(), false),
                new HearingEventDefinition(fromString("50fb4a64-943d-4a2a-afe6-4b5c9e99e043"), "Appellant opens", INTEGER.next(), STRING.next(), "APPEAL", STRING.next(), INTEGER.next(), false),
                new HearingEventDefinition(fromString("e9060336-4821-4f46-969c-e08b33b48071"), "Open case prosecution", INTEGER.next(), STRING.next(), "OPEN_CASE", STRING.next(), INTEGER.next(), false),
                new HearingEventDefinition(fromString("a3a9fe0c-a9a7-4e17-b0cd-42606722bbb0"), "Defence counsel.name opens case regarding defendant defendant.name", INTEGER.next(), STRING.next(), "OPEN_CASE", STRING.next(), INTEGER.next(), false),
                new HearingEventDefinition(fromString("cc00cca8-39ba-431c-b08f-8c6f9be185d1"), "Defence counsel.name closes case regarding defendant defendant.name", INTEGER.next(), STRING.next(), "CLOSE_CASE", STRING.next(), INTEGER.next(), false),
                new HearingEventDefinition(fromString("b335327a-7f58-4f26-a2ef-7e07134ba60b"), "Point of law discussion prosecution", INTEGER.next(), STRING.next(), "DISCUSSION", STRING.next(), INTEGER.next(), false)

        );
    }

    public static HearingEventDefinition findEventDefinitionWithActionLabel(final String actionLabel) {
        return hearingEventDefinitionData.getEventDefinitions().stream().filter(d -> d.getActionLabel().equals(actionLabel)).findFirst().get();
    }

    private static Matcher[] getMatchers(final HearingEventDefinitionData hearingEventDefinitions) {
        final List<String> hearingDefinitionsId = hearingEventDefinitions.getEventDefinitions().stream().map(ed -> ed.getId().toString()).collect(Collectors.toList());
        List<Matcher> matchers = Lists.newArrayList();
        for (String hearingDefinitionId : hearingDefinitionsId) {
            matchers.add(withJsonPath("$.eventDefinitions[*].id", hasItem(hearingDefinitionId)));
        }
        return matchers.toArray(new Matcher[0]);
    }


    public static void thenHearingEventDefinitionsAreRecorded(final HearingEventDefinitionData hearingEventDefinitions) {
        final List<HearingEventDefinition> eventDefinitions = newArrayList(hearingEventDefinitions.getEventDefinitions());

        sortBasedOnSequenceAndActionLabel(eventDefinitions);

        final List<Matcher> conditionsOnJson = new ArrayList<>();
        conditionsOnJson.add(withJsonPath("$.eventDefinitions", hasSize(greaterThanOrEqualTo(eventDefinitions.size()))));

        IntStream.range(0, eventDefinitions.size())
                .forEach(index -> {
                    final HearingEventDefinition eventDefinition = eventDefinitions.get(index);
                    final String eventDefinitionId = eventDefinition.getId().toString();
                    conditionsOnJson.add(withJsonPath(format("$.eventDefinitions[?(@.id=='%s')].id", eventDefinitionId), hasItem(eventDefinitionId)));
                    conditionsOnJson.add(withJsonPath(format("$.eventDefinitions[?(@.id=='%s')].actionLabel", eventDefinitionId), hasItem(eventDefinition.getActionLabel())));
                    conditionsOnJson.add(withJsonPath(format("$.eventDefinitions[?(@.id=='%s')].actionSequence", eventDefinitionId), hasItem(eventDefinition.getActionSequence())));
                    conditionsOnJson.add(withJsonPath(format("$.eventDefinitions[?(@.id=='%s')].groupLabel", eventDefinitionId), hasItem(eventDefinition.getGroupLabel())));
                    conditionsOnJson.add(withJsonPath(format("$.eventDefinitions[?(@.id=='%s')].groupSequence", eventDefinitionId), hasItem(eventDefinition.getGroupSequence())));
                    conditionsOnJson.add(withJsonPath(format("$.eventDefinitions[?(@.id=='%s')].recordedLabel", eventDefinitionId), hasItem(eventDefinition.getRecordedLabel())));
                    conditionsOnJson.add(withJsonPath(format("$.eventDefinitions[?(@.id=='%s')].alterable", eventDefinitionId), hasItem(eventDefinition.isAlterable())));

                    if (eventDefinition.getCaseAttribute() != null) {
                        conditionsOnJson.add(withJsonPath(format("$.eventDefinitions[?(@.id=='%s')].caseAttributes", eventDefinitionId), hasSize(greaterThan(0))));
                    } else {
                        conditionsOnJson.add(withoutJsonPath(format("$.eventDefinitions?(@.id=='%s')].caseAttributes", eventDefinitionId)));
                    }
                });

        poll(requestParams(getQueryEventDefinitionsUrl(), MEDIA_TYPE_QUERY_EVENT_DEFINITIONS).withHeader(USER_ID, getLoggedInUser()))
                .until(
                        status().is(OK),
                        payload().isJson(allOf(conditionsOnJson.toArray(new Matcher[0])))
                );
    }

    private static void sortBasedOnSequenceAndActionLabel(final List<HearingEventDefinition> eventDefinitions) {
        eventDefinitions.sort((ed1, ed2) -> ComparisonChain.start()
                .compare(ed1.getGroupSequence(), ed2.getGroupSequence(), Ordering.natural().nullsLast())
                .compare(ed1.getActionSequence(), ed2.getActionSequence(), Ordering.natural().nullsLast())
                .compare(ed1.getActionLabel(), ed2.getActionLabel(), Ordering.from(CASE_INSENSITIVE_ORDER))
                .result());
    }


    private static String getQueryEventDefinitionsUrl() {
        final String queryEventDefinitionsEndPoint = ENDPOINT_PROPERTIES.getProperty("hearing.get-hearing-event-definitions");
        return format("%s/%s", getBaseUri(), queryEventDefinitionsEndPoint);
    }


}
