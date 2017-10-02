package uk.gov.moj.cpp.hearing.event;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.lang.String.format;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataWithRandomUUID;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataWithRandomUUIDAndName;
import static uk.gov.justice.services.test.utils.core.enveloper.EnvelopeFactory.createEnvelope;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloper;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.metadata;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payloadIsJson;
import static uk.gov.justice.services.test.utils.core.messaging.JsonEnvelopeBuilder.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_ZONED_DATE_TIME;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;


import uk.gov.justice.services.common.converter.ZonedDateTimes;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.time.ZonedDateTime;
import java.util.UUID;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@SuppressWarnings({"unused", "unchecked"})
@RunWith(MockitoJUnitRunner.class)
public class HearingEventProcessorTest {

    private static final String STARTDATE = "2018-11-11";

    @InjectMocks
    private HearingEventProcessor hearingEventProcessor;

    @Mock
    private Sender sender;

    @Mock
    private Requester requester;

    @Mock
    JsonEnvelope responseEnvelope;

    @Spy
    private Enveloper enveloper = createEnveloper();

    @Captor
    private ArgumentCaptor<JsonEnvelope> envelopeArgumentCaptor;

    private static final String RESULTS_SHARED_EVENT = "hearing.results-shared";
    private static final String RESULT_AMENDED_EVENT = "hearing.result-amended";

    private static final String FIELD_GENERIC_ID = "id";
    private static final String FIELD_LAST_SHARED_RESULT_ID = "lastSharedResultId";
    private static final String FIELD_LEVEL = "level";
    private static final String FIELD_RESULT_LINES = "resultLines";
    private static final String FIELD_RESULT_LABEL = "resultLabel";
    private static final String FIELD_PROMPTS = "prompts";
    private static final String FIELD_PROMPT_LABEL = "label";
    private static final String FIELD_PROMPT_VALUE = "value";
    private static final String FIELD_PERSON_ID = "personId";
    private static final String FIELD_OFFENCE_ID = "offenceId";
    private static final String FIELD_CASE_ID = "caseId";
    private static final String FIELD_HEARING_ID = "hearingId";
    private static final String FIELD_SHARED_TIME = "sharedTime";
    private static final String FIELD_HEARING_DEFINITION_ID ="hearingEventDefinitionId";
    private static final String FIELD_EVENT_TIME ="eventTime";
    private static final String FIELD_RECORDED_LABEL ="recordedLabel";
    private static final String FIELD_ALTERABLE ="alterable";
    private static final String FIELD_PRIORITY ="priority";
    private static final String FIELD_HEARING_EVENT_ID = "hearingEventId";
    private static final String FIELD_LAST_HEARING_EVENT_ID = "lastHearingEventId";
    private static final String FIELD_COURT = "court";
    private static final String FIELD_COURT_ROOM = "courtRoom";
    private static final String FIELD_CLERK_OF_THE_COURT_ID = "clerkOfTheCourtId";
    private static final String FIELD_CLERK_OF_THE_COURT_FIRST_NAME = "clerkOfTheCourtFirstName";
    private static final String FIELD_CLERK_OF_THE_COURT_LAST_NAME = "clerkOfTheCourtLastName";

    private static final String FIELD_COURT_VALUE = STRING.next();
    private static final String FIELD_COURT_ROOM_VALUE = STRING.next();
    private static final UUID FIELD_CLERK_OF_THE_COURT_ID_VALUE = randomUUID();
    private static final String FIELD_CLERK_OF_THE_COURT_FIRST_NAME_VALUE = STRING.next();
    private static final String FIELD_CLERK_OF_THE_COURT_LAST_NAME_VALUE = STRING.next();

    private static final String FIELD_LAST_MODIFIED_TIME = "lastModifiedTime";
    private static final String FIELD_CASE_URN = "caseUrn";

    private static final UUID GENERIC_ID = randomUUID();
    private static final UUID LAST_SHARED_RESULT_ID = randomUUID();
    private static final String LEVEL = "OFFENCE";
    private static final String RESULT_LABEL = "Imprisonment";
    private static final String PROMPT_LABEL_1 = "Imprisonment duration";
    private static final String PROMPT_VALUE_1 = "1 year 6 months";
    private static final String PROMPT_LABEL_2 = "Prison";
    private static final String PROMPT_VALUE_2 = "Wormwood Scrubs";
    private static final ZonedDateTime SHARED_TIME = PAST_ZONED_DATE_TIME.next();
    private static final UUID PERSON_ID = randomUUID();
    private static final UUID OFFENCE_ID = randomUUID();
    private static final UUID CASE_ID = randomUUID();
    private static final UUID HEARING_ID = randomUUID();
    private static final String LABEL_VALUE = "hearing started";
    private static final String URN_VALUE = "47GD7822616";

    @Test
    public void publishCaseStartedPublicEvent() throws Exception {
        final JsonEnvelope event = createEnvelope("hearing.hearing-initiated",
                createObjectBuilder().add("hearingId", HEARING_ID.toString()).build());

        hearingEventProcessor.publishHearingInitiatedPublicEvent(event);

        final ArgumentCaptor<JsonEnvelope> envelopeArgumentCaptor = ArgumentCaptor.forClass(JsonEnvelope.class);
        verify(sender).send(envelopeArgumentCaptor.capture());

        assertThat(envelopeArgumentCaptor.getValue(), jsonEnvelope(
                metadata().withName("public.hearing.hearing-initiated"),
                payloadIsJson(
                        withJsonPath(format("$.%s", "hearingId"), equalTo(HEARING_ID.toString()))
                )).thatMatchesSchema());
    }

    @Test
    public void publishHearingResultedPublicEvent() throws Exception {
        final JsonEnvelope event = createResultsSharedEvent();

        hearingEventProcessor.publishHearingResultsSharedPublicEvent(event);

        verify(sender).send(envelopeArgumentCaptor.capture());

        assertThat(envelopeArgumentCaptor.getValue(), jsonEnvelope(
                metadata().withName("public.hearing.resulted"),
                payloadIsJson(allOf(
                        withJsonPath(format("$.%s", FIELD_HEARING_ID), equalTo(HEARING_ID.toString())),
                        withJsonPath(format("$.%s", FIELD_SHARED_TIME), equalTo(ZonedDateTimes.toString(SHARED_TIME))),
                        withJsonPath(format("$.%s[0].%s", FIELD_RESULT_LINES, FIELD_GENERIC_ID), equalTo(GENERIC_ID.toString())),
                        withJsonPath(format("$.%s[0].%s", FIELD_RESULT_LINES, FIELD_PERSON_ID), equalTo(PERSON_ID.toString())),
                        withJsonPath(format("$.%s[0].%s", FIELD_RESULT_LINES, FIELD_OFFENCE_ID), equalTo(OFFENCE_ID.toString())),
                        withJsonPath(format("$.%s[0].%s", FIELD_RESULT_LINES, FIELD_CASE_ID), equalTo(CASE_ID.toString())),
                        withJsonPath(format("$.%s[0].%s", FIELD_RESULT_LINES, FIELD_RESULT_LABEL), equalTo(RESULT_LABEL)),
                        withJsonPath(format("$.%s[0].%s", FIELD_RESULT_LINES, FIELD_LEVEL), equalTo(LEVEL)),
                        withJsonPath(format("$.%s[0].%s", FIELD_RESULT_LINES, FIELD_COURT), equalTo(FIELD_COURT_VALUE)),
                        withJsonPath(format("$.%s[0].%s", FIELD_RESULT_LINES,FIELD_COURT_ROOM), equalTo(FIELD_COURT_ROOM_VALUE)),
                        withJsonPath(format("$.%s[0].%s", FIELD_RESULT_LINES,FIELD_CLERK_OF_THE_COURT_FIRST_NAME), equalTo(FIELD_CLERK_OF_THE_COURT_FIRST_NAME_VALUE)),
                        withJsonPath(format("$.%s[0].%s", FIELD_RESULT_LINES,FIELD_CLERK_OF_THE_COURT_LAST_NAME), equalTo(FIELD_CLERK_OF_THE_COURT_LAST_NAME_VALUE)),
                        withJsonPath(format("$.%s[0].%s", FIELD_RESULT_LINES,FIELD_CLERK_OF_THE_COURT_ID), equalTo(FIELD_CLERK_OF_THE_COURT_ID_VALUE.toString())),
                        withJsonPath(format("$.%s[0].%s[0].%s", FIELD_RESULT_LINES, FIELD_PROMPTS, FIELD_PROMPT_LABEL), equalTo(PROMPT_LABEL_1)),
                        withJsonPath(format("$.%s[0].%s[0].%s", FIELD_RESULT_LINES, FIELD_PROMPTS, FIELD_PROMPT_VALUE), equalTo(PROMPT_VALUE_1)),
                        withJsonPath(format("$.%s[0].%s[1].%s", FIELD_RESULT_LINES, FIELD_PROMPTS, FIELD_PROMPT_LABEL), equalTo(PROMPT_LABEL_2)),
                        withJsonPath(format("$.%s[0].%s[1].%s", FIELD_RESULT_LINES, FIELD_PROMPTS, FIELD_PROMPT_VALUE), equalTo(PROMPT_VALUE_2))
                        )
                )).thatMatchesSchema());
    }

    @Test
    public void publishHearingResultAmendedPublicEvent() throws Exception {
        final JsonEnvelope event = createResultAmendedEvent();

        hearingEventProcessor.publishHearingResultAmendedPublicEvent(event);

        verify(sender).send(envelopeArgumentCaptor.capture());

        assertThat(envelopeArgumentCaptor.getValue(), jsonEnvelope(
                metadata().withName("public.hearing.result-amended"),
                payloadIsJson(allOf(
                        withJsonPath(format("$.%s", FIELD_GENERIC_ID), equalTo(GENERIC_ID.toString())),
                        withJsonPath(format("$.%s", FIELD_LAST_SHARED_RESULT_ID), equalTo(LAST_SHARED_RESULT_ID.toString())),
                        withJsonPath(format("$.%s", FIELD_HEARING_ID), equalTo(HEARING_ID.toString())),
                        withJsonPath(format("$.%s", FIELD_SHARED_TIME), equalTo(ZonedDateTimes.toString(SHARED_TIME))),
                        withJsonPath(format("$.%s", FIELD_PERSON_ID), equalTo(PERSON_ID.toString())),
                        withJsonPath(format("$.%s", FIELD_OFFENCE_ID), equalTo(OFFENCE_ID.toString())),
                        withJsonPath(format("$.%s", FIELD_CASE_ID), equalTo(CASE_ID.toString())),
                        withJsonPath(format("$.%s", FIELD_RESULT_LABEL), equalTo(RESULT_LABEL)),
                        withJsonPath(format("$.%s", FIELD_LEVEL), equalTo(LEVEL)),
                        withJsonPath(format("$.%s", FIELD_COURT), equalTo(FIELD_COURT_VALUE)),
                        withJsonPath(format("$.%s", FIELD_COURT_ROOM), equalTo(FIELD_COURT_ROOM_VALUE)),
                        withJsonPath(format("$.%s", FIELD_CLERK_OF_THE_COURT_FIRST_NAME), equalTo(FIELD_CLERK_OF_THE_COURT_FIRST_NAME_VALUE)),
                        withJsonPath(format("$.%s", FIELD_CLERK_OF_THE_COURT_LAST_NAME), equalTo(FIELD_CLERK_OF_THE_COURT_LAST_NAME_VALUE)),
                        withJsonPath(format("$.%s", FIELD_CLERK_OF_THE_COURT_ID), equalTo(FIELD_CLERK_OF_THE_COURT_ID_VALUE.toString())),
                        withJsonPath(format("$.%s[0].%s", FIELD_PROMPTS, FIELD_PROMPT_LABEL), equalTo(PROMPT_LABEL_1)),
                        withJsonPath(format("$.%s[0].%s", FIELD_PROMPTS, FIELD_PROMPT_VALUE), equalTo(PROMPT_VALUE_1)),
                        withJsonPath(format("$.%s[1].%s", FIELD_PROMPTS, FIELD_PROMPT_LABEL), equalTo(PROMPT_LABEL_2)),
                        withJsonPath(format("$.%s[1].%s", FIELD_PROMPTS, FIELD_PROMPT_VALUE), equalTo(PROMPT_VALUE_2))
                        )
                )).thatMatchesSchema());
    }

    @Test
    public void publishAdjournDateUpdatedPublicEvent() throws Exception {
        // given
        final JsonEnvelope event = createEnvelope("hearing.adjourn-date-updated", createObjectBuilder().add("startDate", STARTDATE).build());

        //when
        hearingEventProcessor.publishHearingDateAdjournedPublicEvent(event);

        // then
        final ArgumentCaptor<JsonEnvelope> envelopeArgumentCaptor = ArgumentCaptor.forClass(JsonEnvelope.class);
        verify(sender).send(envelopeArgumentCaptor.capture());

        assertThat(envelopeArgumentCaptor.getValue(), jsonEnvelope(
                metadata().withName("public.hearing.adjourn-date-updated"),
                payloadIsJson(
                        withJsonPath(format("$.%s", "startDate"), equalTo(STARTDATE))
                )));


    }

    @Test
    public void publishHearingEventLoggedPublicEvent() throws Exception {
        // given
        final JsonEnvelope event = createEnvelope("hearing.hearing-event-logged", createObjectBuilder().add(FIELD_EVENT_TIME, STARTDATE)
                .add(FIELD_RECORDED_LABEL, LABEL_VALUE)
                .add(FIELD_HEARING_EVENT_ID, GENERIC_ID.toString())
                .add(FIELD_HEARING_ID, GENERIC_ID.toString())
                .add(FIELD_HEARING_DEFINITION_ID, GENERIC_ID.toString())
                .add(FIELD_LAST_MODIFIED_TIME, STARTDATE)
                .add(FIELD_ALTERABLE, true).build());

        fakeCaseResponse();

        //when
        hearingEventProcessor.publishHearingEventLoggedPublicEvent(event);

        // then
        final ArgumentCaptor<JsonEnvelope> envelopeArgumentCaptor = ArgumentCaptor.forClass(JsonEnvelope.class);
        verify(sender).send(envelopeArgumentCaptor.capture());

        assertThat(envelopeArgumentCaptor.getValue(), jsonEnvelope(
                metadata().withName("public.hearing.event-logged"),
                payloadIsJson(allOf(
                        withJsonPath(format("$.%s", FIELD_HEARING_DEFINITION_ID), equalTo(GENERIC_ID.toString())),
                        withJsonPath(format("$.%s", FIELD_HEARING_EVENT_ID), equalTo(GENERIC_ID.toString())),
                        withJsonPath(format("$.%s", FIELD_EVENT_TIME), equalTo(STARTDATE)),
                        withJsonPath(format("$.%s", FIELD_RECORDED_LABEL), equalTo(LABEL_VALUE)),
                        withJsonPath(format("$.%s", FIELD_LAST_MODIFIED_TIME), equalTo(STARTDATE)),
                        withJsonPath(format("$.%s", FIELD_CASE_URN), equalTo(URN_VALUE)),
                        withJsonPath(format("$.%s", FIELD_PRIORITY), equalTo(true))
                        )
                )));
    }

    @Test
    public void shouldNotPublishHearingEventLoggedPublicEvent() throws Exception {
        // given
        final JsonEnvelope event = createEnvelope("hearing.hearing-event-logged", createObjectBuilder().add(FIELD_EVENT_TIME, STARTDATE)
                .add(FIELD_RECORDED_LABEL, LABEL_VALUE)
                .add(FIELD_HEARING_EVENT_ID, GENERIC_ID.toString())
                .add(FIELD_HEARING_ID, GENERIC_ID.toString())
                .add(FIELD_HEARING_DEFINITION_ID, GENERIC_ID.toString())
                .add(FIELD_LAST_MODIFIED_TIME, STARTDATE)
                .add(FIELD_ALTERABLE, true).build());

        fakeCaseResponseWithoutUrn();

        //when
        hearingEventProcessor.publishHearingEventLoggedPublicEvent(event);

        // then
        final ArgumentCaptor<JsonEnvelope> envelopeArgumentCaptor = ArgumentCaptor.forClass(JsonEnvelope.class);
        verify(sender,times(0)).send(envelopeArgumentCaptor.capture());

    }


    @Test
    public void publishHearingEventTimeStampCorrectedPublicEvent() throws Exception {
        // given
        final JsonEnvelope event = createEnvelope("hearing.hearing-event-logged", createObjectBuilder().add(FIELD_EVENT_TIME, STARTDATE)
                .add(FIELD_RECORDED_LABEL, LABEL_VALUE)
                .add(FIELD_HEARING_EVENT_ID, GENERIC_ID.toString())
                .add(FIELD_LAST_HEARING_EVENT_ID, GENERIC_ID.toString())
                .add(FIELD_HEARING_DEFINITION_ID, GENERIC_ID.toString())
                .add(FIELD_HEARING_ID, GENERIC_ID.toString())
                .add(FIELD_LAST_MODIFIED_TIME, STARTDATE)
                .add(FIELD_ALTERABLE, true).build());

        fakeCaseResponse();

        //when
        hearingEventProcessor.publishHearingEventLoggedPublicEvent(event);

        // then
        final ArgumentCaptor<JsonEnvelope> envelopeArgumentCaptor = ArgumentCaptor.forClass(JsonEnvelope.class);
        verify(sender).send(envelopeArgumentCaptor.capture());

        assertThat(envelopeArgumentCaptor.getValue(), jsonEnvelope(
                metadata().withName("public.hearing.event-timestamp-corrected"),
                payloadIsJson(allOf(
                        withJsonPath(format("$.%s", FIELD_HEARING_DEFINITION_ID), equalTo(GENERIC_ID.toString())),
                        withJsonPath(format("$.%s", FIELD_HEARING_EVENT_ID), equalTo(GENERIC_ID.toString())),
                        withJsonPath(format("$.%s", FIELD_LAST_HEARING_EVENT_ID), equalTo(GENERIC_ID.toString())),
                        withJsonPath(format("$.%s", FIELD_EVENT_TIME), equalTo(STARTDATE)),
                        withJsonPath(format("$.%s", FIELD_RECORDED_LABEL), equalTo(LABEL_VALUE)),
                        withJsonPath(format("$.%s", FIELD_LAST_MODIFIED_TIME), equalTo(STARTDATE)),
                        withJsonPath(format("$.%s", FIELD_CASE_URN), equalTo(URN_VALUE)),
                        withJsonPath(format("$.%s", FIELD_PRIORITY), equalTo(true))
                        )
                )));
    }


    @Test
    public void shouldNotPublishHearingEventTimeStampCorrectedPublicEvent() throws Exception {
        // given
        final JsonEnvelope event = createEnvelope("hearing.hearing-event-logged", createObjectBuilder().add(FIELD_EVENT_TIME, STARTDATE)
                .add(FIELD_RECORDED_LABEL, LABEL_VALUE)
                .add(FIELD_HEARING_EVENT_ID, GENERIC_ID.toString())
                .add(FIELD_LAST_HEARING_EVENT_ID, GENERIC_ID.toString())
                .add(FIELD_HEARING_DEFINITION_ID, GENERIC_ID.toString())
                .add(FIELD_HEARING_ID, GENERIC_ID.toString())
                .add(FIELD_LAST_MODIFIED_TIME, STARTDATE)
                .add(FIELD_ALTERABLE, true).build());

        fakeCaseResponseWithoutUrn();

        //when
        hearingEventProcessor.publishHearingEventLoggedPublicEvent(event);

        // then
        final ArgumentCaptor<JsonEnvelope> envelopeArgumentCaptor = ArgumentCaptor.forClass(JsonEnvelope.class);
        verify(sender,times(0)).send(envelopeArgumentCaptor.capture());
    }

    private JsonEnvelope createResultsSharedEvent() {
        final JsonArray resultLines = createArrayBuilder().add(
                createObjectBuilder()
                        .add(FIELD_GENERIC_ID, GENERIC_ID.toString())
                        .add(FIELD_PERSON_ID, PERSON_ID.toString())
                        .add(FIELD_CASE_ID, CASE_ID.toString())
                        .add(FIELD_OFFENCE_ID, OFFENCE_ID.toString())
                        .add(FIELD_LEVEL, LEVEL)
                        .add(FIELD_RESULT_LABEL, RESULT_LABEL)
                        .add(FIELD_COURT,FIELD_COURT_VALUE)
                        .add(FIELD_COURT_ROOM,FIELD_COURT_ROOM_VALUE)
                        .add(FIELD_CLERK_OF_THE_COURT_ID,FIELD_CLERK_OF_THE_COURT_ID_VALUE.toString())
                        .add(FIELD_CLERK_OF_THE_COURT_FIRST_NAME,FIELD_CLERK_OF_THE_COURT_FIRST_NAME_VALUE)
                        .add(FIELD_CLERK_OF_THE_COURT_LAST_NAME,FIELD_CLERK_OF_THE_COURT_LAST_NAME_VALUE)
                        .add(FIELD_PROMPTS, createArrayBuilder()
                                .add(createObjectBuilder()
                                        .add(FIELD_PROMPT_LABEL, PROMPT_LABEL_1)
                                        .add(FIELD_PROMPT_VALUE, PROMPT_VALUE_1))
                                .add(createObjectBuilder()
                                        .add(FIELD_PROMPT_LABEL, PROMPT_LABEL_2)
                                        .add(FIELD_PROMPT_VALUE, PROMPT_VALUE_2))))
                .build();

        final JsonObject shareResult = createObjectBuilder()
                .add(FIELD_HEARING_ID, HEARING_ID.toString())
                .add(FIELD_SHARED_TIME, ZonedDateTimes.toString(SHARED_TIME))
                .add(FIELD_RESULT_LINES, resultLines)
                .build();

        return envelopeFrom(metadataWithRandomUUID(RESULTS_SHARED_EVENT), shareResult);
    }

    private JsonEnvelope createResultAmendedEvent() {
        final JsonObjectBuilder amendedResult = createObjectBuilder()
                .add(FIELD_GENERIC_ID, GENERIC_ID.toString())
                .add(FIELD_LAST_SHARED_RESULT_ID, LAST_SHARED_RESULT_ID.toString())
                .add(FIELD_SHARED_TIME, ZonedDateTimes.toString(SHARED_TIME))
                .add(FIELD_HEARING_ID, HEARING_ID.toString())
                .add(FIELD_PERSON_ID, PERSON_ID.toString())
                .add(FIELD_CASE_ID, CASE_ID.toString())
                .add(FIELD_OFFENCE_ID, OFFENCE_ID.toString())
                .add(FIELD_LEVEL, LEVEL)
                .add(FIELD_RESULT_LABEL, RESULT_LABEL)
                .add(FIELD_COURT,FIELD_COURT_VALUE)
                .add(FIELD_COURT_ROOM,FIELD_COURT_ROOM_VALUE)
                .add(FIELD_CLERK_OF_THE_COURT_ID,FIELD_CLERK_OF_THE_COURT_ID_VALUE.toString())
                .add(FIELD_CLERK_OF_THE_COURT_FIRST_NAME,FIELD_CLERK_OF_THE_COURT_FIRST_NAME_VALUE)
                .add(FIELD_CLERK_OF_THE_COURT_LAST_NAME,FIELD_CLERK_OF_THE_COURT_LAST_NAME_VALUE)
                .add(FIELD_PROMPTS, createArrayBuilder()
                        .add(createObjectBuilder()
                                .add(FIELD_PROMPT_LABEL, PROMPT_LABEL_1)
                                .add(FIELD_PROMPT_VALUE, PROMPT_VALUE_1))
                        .add(createObjectBuilder()
                                .add(FIELD_PROMPT_LABEL, PROMPT_LABEL_2)
                                .add(FIELD_PROMPT_VALUE, PROMPT_VALUE_2)));

        return envelopeFrom(metadataWithRandomUUID(RESULT_AMENDED_EVENT), amendedResult.build());
    }

    private void fakeCaseResponse() {
        final JsonObject jsonObject = Json.createObjectBuilder().add("urn",URN_VALUE).add("caseIds",
                Json.createArrayBuilder().add(CASE_ID.toString())
                        .build()).build();
        when(requester.request(any(JsonEnvelope.class))).thenReturn(responseEnvelope);
        when(responseEnvelope.payloadAsJsonObject()).thenReturn(jsonObject);
    }

    private void fakeCaseResponseWithoutUrn() {
        final JsonObject jsonObject = Json.createObjectBuilder().add("urn", JsonValue.NULL).add("caseIds",
                Json.createArrayBuilder().add(CASE_ID.toString())
                        .build()).build();
        when(requester.request(any(JsonEnvelope.class))).thenReturn(responseEnvelope);
        when(responseEnvelope.payloadAsJsonObject()).thenReturn(jsonObject);
    }

}