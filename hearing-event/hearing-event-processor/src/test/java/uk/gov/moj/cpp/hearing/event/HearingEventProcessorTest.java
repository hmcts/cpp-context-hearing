package uk.gov.moj.cpp.hearing.event;

import static com.google.common.io.Resources.getResource;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.nio.charset.Charset.defaultCharset;
import static java.time.ZoneId.of;
import static java.time.ZonedDateTime.now;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.enveloper.EnvelopeFactory.createEnvelope;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloper;
import static uk.gov.justice.services.test.utils.core.messaging.JsonEnvelopeBuilder.envelope;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithDefaults;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.FUTURE_ZONED_DATE_TIME;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_ZONED_DATE_TIME;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;

import uk.gov.justice.core.courts.CourtApplicationOutcomeType;
import uk.gov.justice.core.courts.HearingDay;
import uk.gov.justice.core.courts.Target;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonValueConverter;
import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.common.converter.ZonedDateTimes;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.moj.cpp.hearing.domain.event.HearingDaysCancelled;
import uk.gov.moj.cpp.hearing.domain.event.HearingEffectiveTrial;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventVacatedTrialCleared;
import uk.gov.moj.cpp.hearing.domain.event.HearingTrialType;
import uk.gov.moj.cpp.hearing.domain.event.HearingTrialVacated;
import uk.gov.moj.cpp.hearing.domain.event.result.ApplicationDraftResulted;
import uk.gov.moj.cpp.hearing.eventlog.PublicHearingEventTrialVacated;
import uk.gov.moj.cpp.hearing.test.CoreTestTemplates;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
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

    private static final String HEARING_INITIATED_EVENT = "hearing.initiated";
    private static final String RESULTS_SHARED_EVENT = "hearing.results-shared";
    private static final String DRAFT_RESULT_SAVED_PRIVATE_EVENT = "hearing.draft-result-saved";
    private static final String FIELD_GENERIC_ID = "id";
    private static final String FIELD_GENERIC_TYPE = "type";
    private static final String FIELD_LEVEL = "level";
    private static final String FIELD_RESULT_LINES = "resultLines";
    private static final String FIELD_RESULT_LABEL = "resultLabel";
    private static final String FIELD_PROMPTS = "prompt";
    private static final String FIELD_PROMPT_LABEL = "label";
    private static final String FIELD_VALUE = "value";
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
    private static final String FIELD_COURT_CENTRE = "courtCentre";
    private static final String FIELD_COURT_CENTRE_NAME = "courtCentreName";
    private static final String FIELD_COURT_CENTER_ID = "courtCentreId";
    private static final String FIELD_COURT_ROOM_NAME = "courtRoomName";
    private static final String FIELD_COURT_ROOM_ID = "courtRoomId";
    private static final String FIELD_ROOM_ID = "roomId";
    private static final String FIELD_TARGET_ID = "targetId";
    private static final String FIELD_DEFENDANT_ID = "defendantId";
    private static final String FIELD_DRAFT_RESULT = "draftResult";
    private static final int DURATION = 15;
    private static final String START_DATE_TIME = PAST_ZONED_DATE_TIME.next().toString();
    private static final String HEARING_TYPE = "TRIAL";
    private static final String COURT = STRING.next();
    private static final String COURT_ROOM_NUMBER = STRING.next();
    private static final UUID CLERK_OF_THE_COURT_ID = randomUUID();
    private static final String CLERK_OF_THE_COURT_FIRST_NAME = STRING.next();
    private static final String CLERK_OF_THE_COURT_LAST_NAME = STRING.next();
    private static final UUID PLEA_ID = randomUUID();
    private static final UUID DEFENDANT_ID = randomUUID();
    private static final UUID GENERIC_ID = randomUUID();
    private static final UUID LAST_SHARED_RESULT_ID = randomUUID();
    private static final String OBJECT_PLEA = "plea";
    private static final String FIELD_PLEA_DATE = "pleaDate";
    private static final String PLEA_VALUE = "GUILTY";
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
    private static final UUID TARGET_ID = randomUUID();
    private static final String LABEL_VALUE = "hearing started";
    private static final String URN_VALUE = "47GD7822616";
    private static final String START_DATE = ZonedDateTimes.toString(now());
    private static final String LAST_MODIFIED_TIME = ZonedDateTimes.toString(PAST_ZONED_DATE_TIME.next());
    private static final String EVENT_TIME = ZonedDateTimes.toString(PAST_ZONED_DATE_TIME.next());
    private static final UUID COURT_CENTER_ID = randomUUID();
    private static final String COURT_CENTER_NAME = STRING.next();
    private static final UUID COURT_ROOM_ID = randomUUID();
    private static final String FIELD_JUDGE = "judge";
    private static final String FIELD_JUDGE_ID = "id";
    private static final String FIELD_JUDGE_FIRST_NAME = "firstName";
    private static final String FIELD_JUDGE_LAST_NAME = "lastName";
    private static final String FIELD_JUDGE_TITLE = "title";
    @Spy
    private final Enveloper enveloper = createEnveloper();
    @Spy
    private final ObjectMapper objectMapper = new ObjectMapperProducer().objectMapper();
    @Spy
    private final JsonObjectToObjectConverter jsonObjectToObjectConverter = new JsonObjectToObjectConverter();
    @Spy
    private final ObjectToJsonValueConverter objectToJsonValueConverter = new ObjectToJsonValueConverter(this.objectMapper);
    @InjectMocks
    private HearingEventProcessor hearingEventProcessor;
    @Mock
    private Sender sender;
    @Mock
    private Requester requester;
    @Mock
    private JsonEnvelope responseEnvelope;
    @Captor
    private ArgumentCaptor<JsonEnvelope> envelopeArgumentCaptor;
    @Spy
    private final ObjectToJsonObjectConverter objectToJsonObjectConverter = new ObjectToJsonObjectConverter();

    @DataProvider
    public static Object[][] provideListOfRequiredHearingField() {
        // @formatter:off
        return new Object[][]{
                {FIELD_CASE_URN},
                {FIELD_COURT_CENTER_ID},
                {FIELD_COURT_CENTRE_NAME},
                {FIELD_COURT_ROOM_NAME},
                {FIELD_ROOM_ID},
                {FIELD_HEARING_TYPE}
        };
        // @formatter:on
    }

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
        setField(this.jsonObjectToObjectConverter, "objectMapper", new ObjectMapperProducer().objectMapper());
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
    }

    @Test
    public void shouldPublishDraftResultSavedPublicEvent() {
        final String draftResult = "some random text";
        final Target target = CoreTestTemplates.target(randomUUID(), randomUUID(), randomUUID(), randomUUID()).build();
        final JsonEnvelope eventIn = createDraftResultSavedPrivateEvent(target);

        this.hearingEventProcessor.publicDraftResultSavedPublicEvent(eventIn);

        verify(this.sender, times(1)).send(this.envelopeArgumentCaptor.capture());
        final JsonEnvelope envelopeOut = this.envelopeArgumentCaptor.getValue();
        assertThat(envelopeOut.metadata().name(), is(HearingEventProcessor.PUBLIC_HEARING_DRAFT_RESULT_SAVED));
        final PublicHearingDraftResultSaved publicEventOut = jsonObjectToObjectConverter.convert(envelopeOut.payloadAsJsonObject(), PublicHearingDraftResultSaved.class);
        assertThat(publicEventOut.getDefendantId(), is(target.getDefendantId()));
        assertThat(publicEventOut.getHearingId(), is(target.getHearingId()));
        assertThat(publicEventOut.getOffenceId(), is(target.getOffenceId()));
        assertThat(publicEventOut.getTargetId(), is(target.getTargetId()));
    }

    @Test
    public void shouldPublishSaveDraftResultFailedPublicEvent() {
        final String draftResult = "some random text";
        final Target target = CoreTestTemplates.target(randomUUID(), randomUUID(), randomUUID(), randomUUID()).build();
        final JsonEnvelope eventIn = createDraftResultSavedPrivateEvent(target);

        this.hearingEventProcessor.handleSaveDraftResultFailedEvent(eventIn);

        verify(this.sender).send(this.envelopeArgumentCaptor.capture());
        final JsonEnvelope envelopeOut = this.envelopeArgumentCaptor.getValue();
        assertThat(envelopeOut.metadata().name(), is(HearingEventProcessor.PUBLIC_HEARING_SAVE_DRAFT_RESULT_FAILED));
        final PublicHearingSaveDraftResultFailed publicEventOut = jsonObjectToObjectConverter.convert(envelopeOut.payloadAsJsonObject(), PublicHearingSaveDraftResultFailed.class);
        assertThat(publicEventOut.getDefendantId(), is(target.getDefendantId()));
        assertThat(publicEventOut.getHearingId(), is(target.getHearingId()));
        assertThat(publicEventOut.getOffenceId(), is(target.getOffenceId()));
        assertThat(publicEventOut.getTargetId(), is(target.getTargetId()));
    }

    @Test
    public void shouldPublishApplicationDraftResultedPublicEvent() {
        final ApplicationDraftResulted applicationDraftResulted = ApplicationDraftResulted.applicationDraftResulted()
                .setDraftResult("result")
                .setTargetId(randomUUID())
                .setHearingId(randomUUID())
                .setApplicationId(randomUUID())
                .setApplicationOutcomeType(CourtApplicationOutcomeType.courtApplicationOutcomeType()
                        .withDescription("Granted")
                        .withId(UUID.randomUUID())
                        .withSequence(1).build())
                .setApplicationOutcomeDate(LocalDate.now());
        final JsonEnvelope eventIn = createJsonEnvelope(applicationDraftResulted);

        this.hearingEventProcessor.publicApplicationDraftResultedPublicEvent(eventIn);

        verify(this.sender, times(1)).send(this.envelopeArgumentCaptor.capture());
        final JsonEnvelope envelopeOut = this.envelopeArgumentCaptor.getValue();
        assertThat(envelopeOut.metadata().name(), is(HearingEventProcessor.PUBLIC_HEARING_APPLICATION_DRAFT_RESULTED));
        final PublicHearingApplicationDraftResulted publicEventOut = jsonObjectToObjectConverter.convert(envelopeOut.payloadAsJsonObject(), PublicHearingApplicationDraftResulted.class);
        assertThat(publicEventOut.getHearingId(), is(applicationDraftResulted.getHearingId()));
        assertThat(publicEventOut.getApplicationId(), is(applicationDraftResulted.getApplicationId()));
        assertThat(publicEventOut.getTargetId(), is(applicationDraftResulted.getTargetId()));
        assertThat(publicEventOut.getApplicationOutcomeType(), is(applicationDraftResulted.getApplicationOutcomeType()));
        assertThat(publicEventOut.getApplicationOutcomeDate(), is(applicationDraftResulted.getApplicationOutcomeDate()));
    }


    @Test
    public void shouldTriggerPublicHearingTrialVacatedEventForEffectiveTrial() {
        final UUID hearingId = randomUUID();
        final HearingEffectiveTrial hearingEffectiveTrial = new HearingEffectiveTrial(hearingId, true);
        final JsonEnvelope eventIn = createJsonEnvelope(hearingEffectiveTrial);

        this.hearingEventProcessor.publicHearingEventEffectiveTrialSetPublicEvent(eventIn);

        verify(this.sender, times(1)).send(this.envelopeArgumentCaptor.capture());
        final JsonEnvelope envelopeOut = this.envelopeArgumentCaptor.getValue();
        assertThat(envelopeOut.metadata().name(), is(HearingEventProcessor.PUBLIC_HEARING_TRIAL_VACATED));
        final PublicHearingEventTrialVacated publicEventOut = jsonObjectToObjectConverter.convert(envelopeOut.payloadAsJsonObject(), PublicHearingEventTrialVacated.class);
        assertThat(publicEventOut.getHearingId(), is(hearingEffectiveTrial.getHearingId()));
    }


    @Test
    public void shouldTriggerPublicHearingTrialVacatedEventForCrackedTrial() {
        final UUID hearingId = randomUUID();
        final UUID trialTypeId = randomUUID();
        final HearingTrialType hearingTrialType = new HearingTrialType(hearingId, trialTypeId, "code", "cracked", "desc");
        final JsonEnvelope eventIn = createJsonEnvelope(hearingTrialType);

        this.hearingEventProcessor.publicHearingEventTrialTypeSetPublicEvent(eventIn);

        verify(this.sender, times(1)).send(this.envelopeArgumentCaptor.capture());
        final JsonEnvelope envelopeOut = this.envelopeArgumentCaptor.getValue();
        assertThat(envelopeOut.metadata().name(), is(HearingEventProcessor.PUBLIC_HEARING_TRIAL_VACATED));
        final PublicHearingEventTrialVacated publicEventOut = jsonObjectToObjectConverter.convert(envelopeOut.payloadAsJsonObject(), PublicHearingEventTrialVacated.class);
        assertThat(publicEventOut.getHearingId(), is(hearingTrialType.getHearingId()));
    }

    @Test
    public void shouldTriggerCommandHearingRescheduled() {
        final UUID hearingId = randomUUID();
        final HearingEventVacatedTrialCleared hearingEventRescheduled = new HearingEventVacatedTrialCleared(hearingId);
        final JsonObject jsonObject = this.objectToJsonObjectConverter.convert(hearingEventRescheduled);
        final Metadata metadata = metadataWithDefaults().build();
        this.hearingEventProcessor.handlePublicListingHearingRescheduled(envelopeFrom(metadata, jsonObject));

        verify(this.sender, times(1)).send(this.envelopeArgumentCaptor.capture());
        final JsonEnvelope envelopeOut = this.envelopeArgumentCaptor.getValue();
        assertThat(envelopeOut.metadata().name(), is(HearingEventProcessor.COMMAND_LISTING_HEARING_RESCHEDULED));
    }

    @Test
    public void shouldTriggerPublicHearingTrialVacatedEventForVacatedTrial() {
        final UUID hearingId = randomUUID();
        final UUID trialTypeId = randomUUID();
        final HearingTrialVacated hearingTrialType = new HearingTrialVacated(hearingId, trialTypeId, "code", "cracked", "desc");
        final JsonEnvelope eventIn = createJsonEnvelope(hearingTrialType);

        this.hearingEventProcessor.publicHearingEventVacateTrialTypeSetPublicEvent(eventIn);

        verify(this.sender, times(1)).send(this.envelopeArgumentCaptor.capture());
        final JsonEnvelope envelopeOut = this.envelopeArgumentCaptor.getValue();
        assertThat(envelopeOut.metadata().name(), is(HearingEventProcessor.PUBLIC_HEARING_TRIAL_VACATED));
        final PublicHearingEventTrialVacated publicEventOut = jsonObjectToObjectConverter.convert(envelopeOut.payloadAsJsonObject(), PublicHearingEventTrialVacated.class);
        assertThat(publicEventOut.getHearingId(), is(hearingTrialType.getHearingId()));
        assertThat(publicEventOut.getVacatedTrialReasonId(), is(hearingTrialType.getVacatedTrialReasonId()));
    }

    @Test
    public void shouldTriggerPublicHearingDaysCancelledForCrackedTrial() {
        final UUID hearingId = randomUUID();
        final ZonedDateTime futureSittingDay = FUTURE_ZONED_DATE_TIME.next();
        final ZonedDateTime pastSittingDay = PAST_ZONED_DATE_TIME.next();
        final List<HearingDay> hearingDayList = Arrays.asList(new HearingDay.Builder().withIsCancelled(true).withSittingDay(futureSittingDay).build(),
                new HearingDay.Builder().withIsCancelled(false).withSittingDay(pastSittingDay).build());
        final HearingDaysCancelled hearingDaysCancelled = new HearingDaysCancelled(hearingId, hearingDayList);
        final JsonEnvelope eventIn = createJsonEnvelope(hearingDaysCancelled);

        this.hearingEventProcessor.handleHearingDaysCancelled(eventIn);

        verify(this.sender).send(this.envelopeArgumentCaptor.capture());
        final JsonEnvelope envelopeOut = this.envelopeArgumentCaptor.getValue();
        assertThat(envelopeOut.metadata().name(), is("public.hearing.hearing-days-cancelled"));
        final HearingDaysCancelled publicEvent = jsonObjectToObjectConverter.convert(envelopeOut.payloadAsJsonObject(), HearingDaysCancelled.class);
        assertThat(publicEvent.getHearingId(), is(hearingDaysCancelled.getHearingId()));
        List<HearingDay> hearingDays = publicEvent.getHearingDays();

        assertThat(hearingDays.size(), is(2));
        assertThat(hearingDays, hasItem(hasProperty("isCancelled", is(true))));
        assertThat(hearingDays, hasItem(hasProperty("sittingDay", is(futureSittingDay.withZoneSameLocal(of("UTC"))))));
        assertThat(hearingDays, hasItem(hasProperty("isCancelled", is(false))));
        assertThat(hearingDays, hasItem(hasProperty("sittingDay", is(pastSittingDay.withZoneSameLocal(of("UTC"))))));
    }

    private <E, C> C transactEvent2Command(final E typedEvent, final Consumer<JsonEnvelope> methodUnderTest, final Class<?> commandClass, int sendCount) {
        final JsonValue payload = this.objectToJsonValueConverter.convert(typedEvent);
        final Metadata metadata = metadataWithDefaults().build();
        final JsonEnvelope event = envelopeFrom(metadata, payload);
        methodUnderTest.accept(event);
        verify(this.sender, times(sendCount)).send(this.envelopeArgumentCaptor.capture());
        List<JsonEnvelope> messages = this.envelopeArgumentCaptor.getAllValues();

        final JsonEnvelope result = messages.get(0);//this.envelopeArgumentCaptor.getValue();
        final JsonObject resultingPayload = result.payloadAsJsonObject();
        return (C) jsonObjectToObjectConverter.convert(resultingPayload, commandClass);
    }

    private JsonEnvelope createResultsSharedEvent() {
        final JsonArray resultLines = createArrayBuilder().add(
                createObjectBuilder()
                        .add(FIELD_GENERIC_ID, GENERIC_ID.toString())
                        .add(FIELD_DEFENDANT_ID, DEFENDANT_ID.toString())
                        .add(FIELD_CASE_ID, CASE_ID.toString())
                        .add(FIELD_OFFENCE_ID, OFFENCE_ID.toString())
                        .add(FIELD_LEVEL, LEVEL)
                        .add(FIELD_RESULT_LABEL, RESULT_LABEL)
                        .add(FIELD_COURT, COURT)
                        .add(FIELD_COURT_ROOM, COURT_ROOM_NUMBER)
                        .add(FIELD_CLERK_OF_THE_COURT_ID, CLERK_OF_THE_COURT_ID.toString())
                        .add(FIELD_CLERK_OF_THE_COURT_FIRST_NAME, CLERK_OF_THE_COURT_FIRST_NAME)
                        .add(FIELD_CLERK_OF_THE_COURT_LAST_NAME, CLERK_OF_THE_COURT_LAST_NAME)
                        .add(FIELD_PROMPTS, createArrayBuilder()
                                .add(createObjectBuilder()
                                        .add(FIELD_PROMPT_LABEL, PROMPT_LABEL_1)
                                        .add(FIELD_VALUE, PROMPT_VALUE_1))
                                .add(createObjectBuilder()
                                        .add(FIELD_PROMPT_LABEL, PROMPT_LABEL_2)
                                        .add(FIELD_VALUE, PROMPT_VALUE_2))))
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

    private JsonEnvelope createDraftResultSavedPrivateEvent(final uk.gov.justice.core.courts.Target target) {
        final JsonObject jsonObject = this.objectToJsonObjectConverter.convert(target);
        return envelope().withPayloadOf(jsonObject, "target").with(metadataWithRandomUUID(DRAFT_RESULT_SAVED_PRIVATE_EVENT)).build();
    }

    private JsonEnvelope createDraftResultSavedPrivateEvent(String draftResult) {

        final JsonObjectBuilder result = createObjectBuilder()
                .add(FIELD_TARGET_ID, TARGET_ID.toString())
                .add(FIELD_DEFENDANT_ID, DEFENDANT_ID.toString())
                .add(FIELD_OFFENCE_ID, OFFENCE_ID.toString())
                .add(FIELD_DRAFT_RESULT, draftResult)
                .add(FIELD_HEARING_ID, HEARING_ID.toString());
        return envelopeFrom(metadataWithRandomUUID(DRAFT_RESULT_SAVED_PRIVATE_EVENT), result.build());
    }

    private <T> JsonEnvelope createJsonEnvelope(final T payload) {
        final JsonObject jsonObject = this.objectToJsonObjectConverter.convert(payload);
        final Metadata metadata = metadataWithDefaults().build();
        return envelopeFrom(metadata, jsonObject);
    }

    private void fakeHearingDetailsAndProgressionCaseDetails() {
        final JsonObject hearingDetailsResponse = createObjectBuilder()
                .add("hearingId", HEARING_ID.toString())
                .add("caseIds", createArrayBuilder().add(CASE_ID.toString()))
                .add("courtCentreId", COURT_CENTER_ID.toString())
                .add("courtCentreName", COURT_CENTER_NAME)
                .add("roomName", COURT_ROOM_NUMBER)
                .add("roomId", COURT_ROOM_ID.toString())
                .add("hearingType", HEARING_TYPE)
                .build();

        final JsonObject caseResponse = createObjectBuilder()
                .add("caseUrn", URN_VALUE)
                .add("caseIds", createArrayBuilder().add(CASE_ID.toString()))
                .build();
        when(this.requester.request(any(JsonEnvelope.class))).thenReturn(this.responseEnvelope);
        when(this.responseEnvelope.payloadAsJsonObject()).thenReturn(hearingDetailsResponse).thenReturn(caseResponse);
    }

    private void fakeCaseResponseWithHearingDetailsWithoutField(final String fieldToRemove) {
        final JsonObjectBuilder objectBuilder = createObjectBuilder()
                .add("caseIds", createArrayBuilder().add(CASE_ID.toString()));

        if (!FIELD_CASE_URN.equals(fieldToRemove)) {
            objectBuilder.add("caseUrn", JsonValue.NULL);
        }

        if (!FIELD_COURT_CENTER_ID.equals(fieldToRemove)) {
            objectBuilder.add("courtCentreId", randomUUID().toString());
        }

        if (!FIELD_COURT_CENTRE_NAME.equals(fieldToRemove)) {
            objectBuilder.add("courtCentreName", STRING.next());
        }

        if (!FIELD_COURT_CENTRE_NAME.equals(fieldToRemove)) {
            objectBuilder.add("roomName", STRING.next());
        }

        if (!FIELD_ROOM_ID.equals(fieldToRemove)) {
            objectBuilder.add("roomId", randomUUID().toString());
        }

        final JsonObject jsonObject = objectBuilder.build();

        when(this.requester.request(any(JsonEnvelope.class))).thenReturn(this.responseEnvelope);
        when(this.responseEnvelope.payloadAsJsonObject()).thenReturn(jsonObject);
    }

    private JsonEnvelope getJsonHearingAddedEnvelope() {
        final JsonObjectBuilder hearing = createObjectBuilder();
        hearing.add(FIELD_GENERIC_ID, GENERIC_ID.toString());
        hearing.add(FIELD_GENERIC_TYPE, HEARING_TYPE);
        hearing.add(FIELD_CASE_ID, CASE_ID.toString());
        final JsonObject jsonObject = createObjectBuilder().add(FIELD_CASE_ID, CASE_ID.toString())
                .add("hearing", hearing).build();

        final Metadata metadata = metadataWithDefaults().build();
        return envelopeFrom(metadata, jsonObject);

    }

    private JsonEnvelope getJsonPublicHearingPleaUpdate() {
        final JsonObject jsonObject = createObjectBuilder().add(FIELD_CASE_ID, CASE_ID.toString()).build();
        final Metadata metadata = metadataWithDefaults().build();
        return envelopeFrom(metadata, jsonObject);

    }


    private JsonEnvelope getJsonPublicHearingVerdictUpdate() {
        final JsonObject jsonObject = createObjectBuilder().add(FIELD_HEARING_ID, HEARING_ID.toString()).build();
        final Metadata metadata = metadataWithDefaults().build();
        return JsonEnvelope.envelopeFrom(metadata, jsonObject);

    }

    public JsonEnvelope getJsonHearingCasePleaAddedOrChangedEnvelope() throws IOException {
        final String hearingCasePleaAddOrUpdate = getStringFromResource("hearing.case.plea-added-or-changed.json")
                .replace("RANDOM_CASE_ID", CASE_ID.toString())
                .replace("RANDOM_HEARING_ID", HEARING_ID.toString())
                .replace("RANDOM_OFFENCE_ID", OFFENCE_ID.toString())
                .replace("RANDOM_PERSON_ID", PERSON_ID.toString())
                .replace("RANDOM_DEFENDANT_ID", DEFENDANT_ID.toString())
                .replace("RANDOM_PLEA_ID", PLEA_ID.toString())
                .replace("RANDOM_PLEA_DATE", START_DATE);

        final Metadata metadata = metadataWithDefaults().build();
        return JsonEnvelope.envelopeFrom(metadata, new StringToJsonObjectConverter().convert(hearingCasePleaAddOrUpdate));
    }

    private String getStringFromResource(final String path) throws IOException {
        return Resources.toString(getResource(path), defaultCharset());
    }
}
