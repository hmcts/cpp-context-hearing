package uk.gov.moj.cpp.hearing.event;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.lang.String.format;
import static java.time.ZonedDateTime.now;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataWithDefaults;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.core.enveloper.EnvelopeFactory.createEnvelope;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloper;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.metadata;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payloadIsJson;
import static uk.gov.justice.services.test.utils.core.messaging.JsonEnvelopeBuilder.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_ZONED_DATE_TIME;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonValueConverter;
import uk.gov.justice.services.common.converter.ZonedDateTimes;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.DefaultJsonEnvelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;

import java.time.ZonedDateTime;
import java.util.UUID;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

@SuppressWarnings({"unchecked", "unused"})
@RunWith(DataProviderRunner.class)
public class HearingEventProcessorTest {

    @Mock
    private Sender sender;

    @Mock
    private Requester requester;

    @Mock
    private JsonEnvelope responseEnvelope;

    @Spy
    private final Enveloper enveloper = createEnveloper();

    @Captor
    private ArgumentCaptor<JsonEnvelope> envelopeArgumentCaptor;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapperProducer().objectMapper();

    @Spy
    @InjectMocks
    private JsonObjectToObjectConverter jsonObjectToObjectConverter = new JsonObjectToObjectConverter();

    @Spy
    @InjectMocks
    private ObjectToJsonValueConverter objectToJsonValueConverter = new ObjectToJsonValueConverter(objectMapper);

    @InjectMocks
    private HearingEventProcessor hearingEventProcessor;

    private static final String RESULTS_SHARED_EVENT = "hearing.results-shared";
    private static final String RESULT_AMENDED_EVENT = "hearing.result-amended";

    private static final String FIELD_GENERIC_ID = "id";
    private static final String FIELD_GENERIC_TYPE = "type";


    private static final String FIELD_LAST_SHARED_RESULT_ID = "lastSharedResultId";
    private static final String FIELD_LEVEL = "level";
    private static final String FIELD_RESULT_LINES = "resultLines";
    private static final String FIELD_RESULT_LABEL = "resultLabel";
    private static final String FIELD_PROMPTS = "prompts";
    private static final String FIELD_PROMPT_LABEL = "label";
    private static final String FIELD_PROMPT_VALUE = "value";
    private static final String FIELD_PERSON_ID = "personId";
    private static final String FIELD_OFFENCE_ID = "offenceId";
    private static final String FIELD_CASE = "case";
    private static final String FIELD_CASE_ID = "caseId";
    private static final String FIELD_HEARING_ID = "hearingId";
    private static final String FIELD_SHARED_TIME = "sharedTime";
    private static final String FIELD_HEARING_DEFINITION = "hearingEventDefinition";
    private static final String FIELD_HEARING_DEFINITION_ID = "hearingEventDefinitionId";
    private static final String FIELD_HEARING_EVENT = "hearingEvent";
    private static final String FIELD_HEARING = "hearing";
    private static final String FIELD_EVENT_TIME = "eventTime";
    private static final String FIELD_RECORDED_LABEL = "recordedLabel";
    private static final String FIELD_ALTERABLE = "alterable";
    private static final String FIELD_PRIORITY = "priority";
    private static final String FIELD_HEARING_EVENT_ID = "hearingEventId";
    private static final String FIELD_LAST_HEARING_EVENT_ID = "lastHearingEventId";
    private static final String FIELD_COURT = "court";
    private static final String FIELD_COURT_ROOM = "courtRoom";
    private static final String FIELD_CLERK_OF_THE_COURT_ID = "clerkOfTheCourtId";
    private static final String FIELD_CLERK_OF_THE_COURT_FIRST_NAME = "clerkOfTheCourtFirstName";
    private static final String FIELD_CLERK_OF_THE_COURT_LAST_NAME = "clerkOfTheCourtLastName";

    private static final String FIELD_START_DATE = "startDate";

    private static final String FIELD_LAST_MODIFIED_TIME = "lastModifiedTime";
    private static final String FIELD_CASE_URN = "caseUrn";
    private static final String FIELD_DURATION = "duration";
    private static final String FIELD_HEARING_TYPE = "hearingType";
    private static final String FIELD_START_DATE_TIME = "startDateTime";
    private static final String FIELD_ESTIMATE_MINUTES = "estimateMinutes";

    private static final String FIELD_COURT_CENTRE = "courtCentre";
    private static final String FIELD_COURT_NAME = "courtCentreName";
    private static final String FIELD_COURT_CENTER_ID = "courtCentreId";
    private static final String FIELD_ROOM_NUMBER = "roomNumber";

    private static final int DURATION = 15;
    private static final String START_DATE_TIME = PAST_ZONED_DATE_TIME.next().toString();
    private static final String TYPE = "TRIAL";
    private static final String COURT = STRING.next();
    private static final String COURT_ROOM = STRING.next();
    private static final UUID CLERK_OF_THE_COURT_ID = randomUUID();
    private static final String CLERK_OF_THE_COURT_FIRST_NAME = STRING.next();
    private static final String CLERK_OF_THE_COURT_LAST_NAME = STRING.next();

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
    private static final String START_DATE = ZonedDateTimes.toString(now());
    private static final String LAST_MODIFIED_TIME = ZonedDateTimes.toString(PAST_ZONED_DATE_TIME.next());
    private static final String EVENT_TIME = ZonedDateTimes.toString(PAST_ZONED_DATE_TIME.next());
    private static final UUID COURT_CENTER_ID = randomUUID();
    private static final String COURT_CENTER_NAME = STRING.next();


    private static final String FIELD_JUDGE = "judge";
    private static final String FIELD_JUDGE_ID = "id";
    private static final String FIELD_JUDGE_FIRST_NAME = "firstName";
    private static final String FIELD_JUDGE_LAST_NAME = "lastName";
    private static final String FIELD_JUDGE_TITLE = "title";


    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void publishCaseStartedPublicEvent() {
        final JsonEnvelope event = createEnvelope("hearing.hearing-initiated",
                createObjectBuilder().add("hearingId", HEARING_ID.toString()).build());

        this.hearingEventProcessor.publishHearingInitiatedPublicEvent(event);

        verify(this.sender).send(this.envelopeArgumentCaptor.capture());

        assertThat(this.envelopeArgumentCaptor.getValue(), jsonEnvelope(
                metadata().withName("public.hearing.hearing-initiated"),
                payloadIsJson(
                        withJsonPath(format("$.%s", "hearingId"), equalTo(HEARING_ID.toString()))
                )).thatMatchesSchema());
    }

    @Test
    public void publishHearingResultedPublicEvent() {
        final JsonEnvelope event = createResultsSharedEvent();

        this.hearingEventProcessor.publishHearingResultsSharedPublicEvent(event);

        verify(this.sender).send(this.envelopeArgumentCaptor.capture());

        assertThat(this.envelopeArgumentCaptor.getValue(), jsonEnvelope(
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
                        withJsonPath(format("$.%s[0].%s", FIELD_RESULT_LINES, FIELD_COURT), equalTo(COURT)),
                        withJsonPath(format("$.%s[0].%s", FIELD_RESULT_LINES, FIELD_COURT_ROOM), equalTo(COURT_ROOM)),
                        withJsonPath(format("$.%s[0].%s", FIELD_RESULT_LINES, FIELD_CLERK_OF_THE_COURT_FIRST_NAME), equalTo(CLERK_OF_THE_COURT_FIRST_NAME)),
                        withJsonPath(format("$.%s[0].%s", FIELD_RESULT_LINES, FIELD_CLERK_OF_THE_COURT_LAST_NAME), equalTo(CLERK_OF_THE_COURT_LAST_NAME)),
                        withJsonPath(format("$.%s[0].%s", FIELD_RESULT_LINES, FIELD_CLERK_OF_THE_COURT_ID), equalTo(CLERK_OF_THE_COURT_ID.toString())),
                        withJsonPath(format("$.%s[0].%s[0].%s", FIELD_RESULT_LINES, FIELD_PROMPTS, FIELD_PROMPT_LABEL), equalTo(PROMPT_LABEL_1)),
                        withJsonPath(format("$.%s[0].%s[0].%s", FIELD_RESULT_LINES, FIELD_PROMPTS, FIELD_PROMPT_VALUE), equalTo(PROMPT_VALUE_1)),
                        withJsonPath(format("$.%s[0].%s[1].%s", FIELD_RESULT_LINES, FIELD_PROMPTS, FIELD_PROMPT_LABEL), equalTo(PROMPT_LABEL_2)),
                        withJsonPath(format("$.%s[0].%s[1].%s", FIELD_RESULT_LINES, FIELD_PROMPTS, FIELD_PROMPT_VALUE), equalTo(PROMPT_VALUE_2))
                        )
                )).thatMatchesSchema());
    }

    @Test
    public void publishHearingResultAmendedPublicEvent() {
        final JsonEnvelope event = createResultAmendedEvent();

        this.hearingEventProcessor.publishHearingResultAmendedPublicEvent(event);

        verify(this.sender).send(this.envelopeArgumentCaptor.capture());

        assertThat(this.envelopeArgumentCaptor.getValue(), jsonEnvelope(
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
                        withJsonPath(format("$.%s", FIELD_COURT), equalTo(COURT)),
                        withJsonPath(format("$.%s", FIELD_COURT_ROOM), equalTo(COURT_ROOM)),
                        withJsonPath(format("$.%s", FIELD_CLERK_OF_THE_COURT_FIRST_NAME), equalTo(CLERK_OF_THE_COURT_FIRST_NAME)),
                        withJsonPath(format("$.%s", FIELD_CLERK_OF_THE_COURT_LAST_NAME), equalTo(CLERK_OF_THE_COURT_LAST_NAME)),
                        withJsonPath(format("$.%s", FIELD_CLERK_OF_THE_COURT_ID), equalTo(CLERK_OF_THE_COURT_ID.toString())),
                        withJsonPath(format("$.%s[0].%s", FIELD_PROMPTS, FIELD_PROMPT_LABEL), equalTo(PROMPT_LABEL_1)),
                        withJsonPath(format("$.%s[0].%s", FIELD_PROMPTS, FIELD_PROMPT_VALUE), equalTo(PROMPT_VALUE_1)),
                        withJsonPath(format("$.%s[1].%s", FIELD_PROMPTS, FIELD_PROMPT_LABEL), equalTo(PROMPT_LABEL_2)),
                        withJsonPath(format("$.%s[1].%s", FIELD_PROMPTS, FIELD_PROMPT_VALUE), equalTo(PROMPT_VALUE_2))
                        )
                )).thatMatchesSchema());
    }

    @Test
    public void publishAdjournDateUpdatedPublicEvent() {
        // given
        final JsonEnvelope event = createEnvelope("hearing.adjourn-date-updated", createObjectBuilder().add(FIELD_START_DATE, START_DATE).build());

        //when
        this.hearingEventProcessor.publishHearingDateAdjournedPublicEvent(event);

        // then
        verify(this.sender).send(this.envelopeArgumentCaptor.capture());

        assertThat(this.envelopeArgumentCaptor.getValue(), jsonEnvelope(
                metadata().withName("public.hearing.adjourn-date-updated"),
                payloadIsJson(
                        withJsonPath(format("$.%s", FIELD_START_DATE), equalTo(START_DATE))
                )).thatMatchesSchema());
    }

    @Test
    public void shouldPublishHearingEventLoggedPublicEvent() {
        // given
        final JsonEnvelope event = prepareHearingEventLoggedEvent();

        fakeCaseResponse();

        //when
        this.hearingEventProcessor.publishHearingEventLoggedPublicEvent(event);

        // then
        verify(this.sender).send(this.envelopeArgumentCaptor.capture());

        assertThat(this.envelopeArgumentCaptor.getValue(), jsonEnvelope(
                metadata().withName("public.hearing.event-logged"),
                payloadIsJson(allOf(
                        withJsonPath(format("$.%s.%s", FIELD_CASE, FIELD_CASE_URN), equalTo(URN_VALUE)),
                        withJsonPath(format("$.%s.%s", FIELD_HEARING_DEFINITION, FIELD_HEARING_DEFINITION_ID), equalTo(GENERIC_ID.toString())),
                        withJsonPath(format("$.%s.%s", FIELD_HEARING_DEFINITION, FIELD_PRIORITY), equalTo(true)),
                        withJsonPath(format("$.%s.%s", FIELD_HEARING_EVENT, FIELD_HEARING_EVENT_ID), equalTo(GENERIC_ID.toString())),
                        withJsonPath(format("$.%s.%s", FIELD_HEARING_EVENT, FIELD_RECORDED_LABEL), equalTo(LABEL_VALUE)),
                        withJsonPath(format("$.%s.%s", FIELD_HEARING_EVENT, FIELD_EVENT_TIME), equalTo(EVENT_TIME)),
                        withJsonPath(format("$.%s.%s", FIELD_HEARING_EVENT, FIELD_LAST_MODIFIED_TIME), equalTo(LAST_MODIFIED_TIME)),
                        withJsonPath(format("$.%s.%s.%s", FIELD_HEARING, FIELD_COURT_CENTRE, FIELD_COURT_CENTER_ID), equalTo(COURT_CENTER_ID.toString())),
                        withJsonPath(format("$.%s.%s.%s", FIELD_HEARING, FIELD_COURT_CENTRE, FIELD_COURT_NAME), equalTo(COURT_CENTER_NAME)),
                        withJsonPath(format("$.%s.%s.%s", FIELD_HEARING, FIELD_COURT_CENTRE, FIELD_ROOM_NUMBER), equalTo(COURT_ROOM)),
                        withJsonPath(format("$.%s.%s", FIELD_HEARING, FIELD_HEARING_TYPE), equalTo(TYPE)))
                )).thatMatchesSchema());
    }

    @DataProvider
    public static Object[][] provideListOfRequiredHearingField() {
        // @formatter:off
        return new Object[][] {
                {FIELD_CASE_URN},
                {FIELD_COURT_CENTER_ID},
                {FIELD_COURT_NAME},
                {FIELD_ROOM_NUMBER},
                { FIELD_HEARING_TYPE}
        };
        // @formatter:on
    }

    @Test
    @UseDataProvider("provideListOfRequiredHearingField")
    public void shouldNotPublishHearingEventLoggedPublicEventWhenRequiredHearingDataIsNotAvailable(String fieldToRemove) {
        // given
        final JsonEnvelope event = prepareHearingEventLoggedEvent();

        fakeCaseResponseWithHearingDetailsWithoutField(fieldToRemove);

        //when
        this.hearingEventProcessor.publishHearingEventLoggedPublicEvent(event);

        // then
        verifyZeroInteractions(this.sender);
    }

    @Test
    @UseDataProvider("provideListOfRequiredHearingField")
    public void shouldNotPublishHearingEventTimeStampCorrectedPublicEventWhenRequiredHearingDataIsNotAvailable(String fieldToRemove) {
        // given
        final JsonEnvelope event = prepareHearingEventUpdatedEvent();

        fakeCaseResponseWithHearingDetailsWithoutField(fieldToRemove);

        //when
        this.hearingEventProcessor.publishHearingEventLoggedPublicEvent(event);

        // then
        verifyZeroInteractions(this.sender);
    }

    @Test
    public void shouldPublishHearingEventTimeStampCorrectedPublicEvent() {
        // given
        final JsonEnvelope event = prepareHearingEventUpdatedEvent();

        fakeCaseResponse();

        //when
        this.hearingEventProcessor.publishHearingEventLoggedPublicEvent(event);

        // then
        verify(this.sender).send(this.envelopeArgumentCaptor.capture());


        assertThat(this.envelopeArgumentCaptor.getValue(), jsonEnvelope(
                metadata().withName("public.hearing.event-timestamp-corrected"),
                payloadIsJson(allOf(
                        withJsonPath(format("$.%s.%s", FIELD_CASE, FIELD_CASE_URN), equalTo(URN_VALUE)),
                        withJsonPath(format("$.%s.%s", FIELD_HEARING_DEFINITION, FIELD_HEARING_DEFINITION_ID), equalTo(GENERIC_ID.toString())),
                        withJsonPath(format("$.%s.%s", FIELD_HEARING_DEFINITION, FIELD_PRIORITY), equalTo(true)),
                        withJsonPath(format("$.%s.%s", FIELD_HEARING_EVENT, FIELD_HEARING_EVENT_ID), equalTo(GENERIC_ID.toString())),
                        withJsonPath(format("$.%s.%s", FIELD_HEARING_EVENT, FIELD_LAST_HEARING_EVENT_ID), equalTo(GENERIC_ID.toString())),
                        withJsonPath(format("$.%s.%s", FIELD_HEARING_EVENT, FIELD_RECORDED_LABEL), equalTo(LABEL_VALUE)),
                        withJsonPath(format("$.%s.%s", FIELD_HEARING_EVENT, FIELD_EVENT_TIME), equalTo(EVENT_TIME)),
                        withJsonPath(format("$.%s.%s", FIELD_HEARING_EVENT, FIELD_LAST_MODIFIED_TIME), equalTo(LAST_MODIFIED_TIME)),
                        withJsonPath(format("$.%s.%s.%s", FIELD_HEARING, FIELD_COURT_CENTRE, FIELD_COURT_CENTER_ID), equalTo(COURT_CENTER_ID.toString())),
                        withJsonPath(format("$.%s.%s.%s", FIELD_HEARING, FIELD_COURT_CENTRE, FIELD_COURT_NAME), equalTo(COURT_CENTER_NAME)),
                        withJsonPath(format("$.%s.%s.%s", FIELD_HEARING, FIELD_COURT_CENTRE, FIELD_ROOM_NUMBER), equalTo(COURT_ROOM)),
                        withJsonPath(format("$.%s.%s", FIELD_HEARING, FIELD_HEARING_TYPE), equalTo(TYPE)))
                )).thatMatchesSchema());
    }

    @Test
    public void processHearingAddedPublicEvent() {
        this.hearingEventProcessor.createInitiateHearingCommandFromHearingConfirmedRecorded(getJsonHearingAddedEnvelope());
        verify(this.sender).send(this.envelopeArgumentCaptor.capture());
        assertThat(this.envelopeArgumentCaptor.getValue(), jsonEnvelope(
                metadata().withName("hearing.initiate-hearing"),
                payloadIsJson(allOf(
                        withJsonPath(format("$.%s", FIELD_CASE_ID), equalTo(CASE_ID.toString())),
                        withJsonPath(format("$.%s", "hearingId"), equalTo(GENERIC_ID.toString())),
                        withJsonPath(format("$.%s", FIELD_HEARING_TYPE), equalTo(TYPE)),
                        withJsonPath(format("$.%s", FIELD_DURATION), equalTo(DURATION)))
                )));
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
                        .add(FIELD_COURT, COURT)
                        .add(FIELD_COURT_ROOM, COURT_ROOM)
                        .add(FIELD_CLERK_OF_THE_COURT_ID, CLERK_OF_THE_COURT_ID.toString())
                        .add(FIELD_CLERK_OF_THE_COURT_FIRST_NAME, CLERK_OF_THE_COURT_FIRST_NAME)
                        .add(FIELD_CLERK_OF_THE_COURT_LAST_NAME, CLERK_OF_THE_COURT_LAST_NAME)
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

    private JsonEnvelope prepareHearingEventLoggedEvent() {
        return createEnvelope("hearing.hearing-event-logged",
                createObjectBuilder()
                        .add(FIELD_EVENT_TIME, EVENT_TIME)
                        .add(FIELD_RECORDED_LABEL, LABEL_VALUE)
                        .add(FIELD_HEARING_EVENT_ID, GENERIC_ID.toString())
                        .add(FIELD_HEARING_ID, GENERIC_ID.toString())
                        .add(FIELD_HEARING_DEFINITION_ID, GENERIC_ID.toString())
                        .add(FIELD_LAST_MODIFIED_TIME, LAST_MODIFIED_TIME)
                        .add(FIELD_ALTERABLE, true)
                        .build());
    }

    private JsonEnvelope prepareHearingEventUpdatedEvent() {
        return createEnvelope("hearing.hearing-event-logged",
                createObjectBuilder()
                        .add(FIELD_EVENT_TIME, EVENT_TIME)
                        .add(FIELD_RECORDED_LABEL, LABEL_VALUE)
                        .add(FIELD_HEARING_EVENT_ID, GENERIC_ID.toString())
                        .add(FIELD_LAST_HEARING_EVENT_ID, GENERIC_ID.toString())
                        .add(FIELD_HEARING_DEFINITION_ID, GENERIC_ID.toString())
                        .add(FIELD_HEARING_ID, GENERIC_ID.toString())
                        .add(FIELD_LAST_MODIFIED_TIME, LAST_MODIFIED_TIME)
                        .add(FIELD_ALTERABLE, true)
                        .build());
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
                .add(FIELD_COURT, COURT)
                .add(FIELD_COURT_ROOM, COURT_ROOM)
                .add(FIELD_CLERK_OF_THE_COURT_ID, CLERK_OF_THE_COURT_ID.toString())
                .add(FIELD_CLERK_OF_THE_COURT_FIRST_NAME, CLERK_OF_THE_COURT_FIRST_NAME)
                .add(FIELD_CLERK_OF_THE_COURT_LAST_NAME, CLERK_OF_THE_COURT_LAST_NAME)
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
        final JsonObject jsonObject = createObjectBuilder()
                .add("urn", URN_VALUE)
                .add("caseIds", createArrayBuilder().add(CASE_ID.toString()))
                .add("courtCentreId", COURT_CENTER_ID.toString())
                .add("courtCentreName", COURT_CENTER_NAME)
                .add("roomName", COURT_ROOM)
                .add("hearingType", TYPE)
                .build();
        when(this.requester.request(any(JsonEnvelope.class))).thenReturn(this.responseEnvelope);
        when(this.responseEnvelope.payloadAsJsonObject()).thenReturn(jsonObject);
    }

    private void fakeCaseResponseWithHearingDetailsWithoutField(final String fieldToRemove) {
        final JsonObjectBuilder objectBuilder = createObjectBuilder()
                .add("caseIds", createArrayBuilder().add(CASE_ID.toString()));

        if (!FIELD_CASE_URN.equals(fieldToRemove)) {
            objectBuilder.add("urn", JsonValue.NULL);
        }

        if (!FIELD_COURT_CENTER_ID.equals(fieldToRemove)) {
            objectBuilder.add("courtCentreId", randomUUID().toString());
        }

        if (!FIELD_COURT_NAME.equals(fieldToRemove)) {
            objectBuilder.add("courtCentreName", STRING.next());
        }

        if (!FIELD_COURT_NAME.equals(fieldToRemove)) {
            objectBuilder.add("roomName", STRING.next());
        }

        final JsonObject jsonObject = objectBuilder.build();

        when(requester.request(any(JsonEnvelope.class))).thenReturn(responseEnvelope);
        when(responseEnvelope.payloadAsJsonObject()).thenReturn(jsonObject);
    }

    private JsonEnvelope getJsonHearingAddedEnvelope() {
        final JsonObjectBuilder hearing = createObjectBuilder();
        hearing.add(FIELD_GENERIC_ID, GENERIC_ID.toString());
        hearing.add(FIELD_GENERIC_TYPE, TYPE);
        hearing.add(FIELD_START_DATE_TIME, START_DATE_TIME);
        hearing.add(FIELD_ESTIMATE_MINUTES, DURATION);
        hearing.add(FIELD_CASE_ID, CASE_ID.toString());
        final JsonObject jsonObject = createObjectBuilder().add(FIELD_CASE_ID, CASE_ID.toString())
                .add("hearing", hearing).build();

        final Metadata metadata = metadataWithDefaults().build();
        return new DefaultJsonEnvelope(metadata, jsonObject);

    }
}
