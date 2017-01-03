package uk.gov.moj.cpp.hearing.command.handler;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.lang.String.format;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.common.converter.ZonedDateTimes.fromJsonString;
import static uk.gov.justice.services.messaging.DefaultJsonEnvelope.envelope;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.ID;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.NAME;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataWithRandomUUID;
import static uk.gov.justice.services.messaging.JsonObjects.getJsonNumber;
import static uk.gov.justice.services.messaging.JsonObjects.getJsonString;
import static uk.gov.justice.services.messaging.JsonObjects.getString;
import static uk.gov.justice.services.messaging.JsonObjects.getUUID;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloperWithEvents;
import static uk.gov.justice.services.test.utils.core.helper.EventStreamMockHelper.verifyAppendAndGetArgumentFrom;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.withMetadataEnvelopedFrom;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payLoad;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payloadIsJson;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeStreamMatcher.streamContaining;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.INTEGER;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_ZONED_DATE_TIME;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.UUID;

import uk.gov.justice.services.common.converter.ZonedDateTimes;
import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.core.aggregate.AggregateService;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.messaging.DefaultJsonEnvelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.JsonObjectMetadata;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.moj.cpp.hearing.command.handler.converter.JsonToHearingConverter;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;
import uk.gov.moj.cpp.hearing.domain.command.InitiateHearing;
import uk.gov.moj.cpp.hearing.domain.event.DraftResultSaved;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventLogged;
import uk.gov.moj.cpp.hearing.domain.event.HearingInitiated;
import uk.gov.moj.cpp.hearing.domain.event.ProsecutionCounselAdded;

import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

import javax.json.JsonObject;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class HearingCommandHandlerTest {

    private static final UUID HEARING_ID = randomUUID();
    private static final String LIST_HEARING_COMMAND = "hearing.initiate-hearing";
    private static final String HEARING_LISTED_EVENT = "hearing.hearing-initiated";
    private static final String ADD_PROSECUTION_COUNSEL_EVENT_NAME = "hearing.prosecution-counsel-added";
    private static final String LOG_HEARING_EVENT_COMMAND = "hearing.log-hearing-event";
    private static final String SAVE_DRAFT_RESULT_COMMAND = "hearing.save-draft-result";
    private static final String HEARING_EVENT_LOGGED_EVENT = "hearing.hearing-event-logged";
    private static final String HEARING_DRAFT_RESULT_SAVED_EVENT = "hearing.draft-result-saved";

    private static final String HEARING_EVENT_ID_FIELD = "id";
    private static final String RECORDED_LABEL_FIELD = "recordedLabel";
    private static final String HEARING_ID_FIELD = "hearingId";
    private static final String TIMESTAMP_FIELD = "timestamp";

    private static final UUID HEARING_EVENT_ID = randomUUID();
    private static final String RECORDED_LABEL = STRING.next();
    private static final String TIMESTAMP = ZonedDateTimes.toString(PAST_ZONED_DATE_TIME.next());
    public static final String DEFENDANT_ID = "defendantId";
    private final UUID DEFENDANT_ID_VALUE = randomUUID();
    public static final String TARGET_ID = "targetId";
    private static final UUID TARGET_ID_VALUE = randomUUID();
    public static final String OFFENCE_ID = "offenceId";
    private static final UUID OFFENCE_ID_VALUE = randomUUID();
    public static final String DRAFT_RESULT = "draftResult";
    public static final String ARBITRARY_STRING_IMP_2_YRS = "imp 2 yrs";

    private final UUID personId = randomUUID();
    private final UUID hearingId = randomUUID();
    private final UUID attendeeId = randomUUID();
    private final String status = STRING.next();

    @Mock
    private EventStream eventStream;

    @Mock
    private EventSource eventSource;

    @Mock
    private JsonToHearingConverter jsonToHearingConverter;

    @Mock
    private AggregateService aggregateService;

    @Mock
    private HearingAggregate hearingAggregate;

    @Spy
    private Enveloper enveloper = createEnveloperWithEvents(HearingEventLogged.class, DraftResultSaved.class);

    @InjectMocks
    private HearingCommandHandler hearingCommandHandler;

    @Before
    public void setup() {
        when(eventSource.getStreamById(HEARING_ID)).thenReturn(eventStream);
    }

    @Test
    public void shouldHandlerListHearingCommand() throws Exception {

        final InitiateHearing initiateHearing = createHearing(HEARING_ID);
        final JsonEnvelope listHearingCommand = createListHearingCommand(initiateHearing);

        when(jsonToHearingConverter.convertToInitiateHearing(listHearingCommand)).thenReturn(initiateHearing);
        when(eventSource.getStreamById(HEARING_ID)).thenReturn(eventStream);
        when(aggregateService.get(eventStream, HearingAggregate.class)).thenReturn(hearingAggregate);
        when(hearingAggregate.initiateHearing(initiateHearing)).thenReturn(Stream.of(createHearingListed(initiateHearing)));
        when(enveloper.withMetadataFrom(listHearingCommand)).thenReturn(
                createEnveloperWithEvents(HearingInitiated.class).withMetadataFrom(listHearingCommand));

        hearingCommandHandler.initiateHearing(listHearingCommand);

        final JsonEnvelope resultEvent = verifyAppendAndGetArgumentFrom(eventStream).findFirst().get();
        final Metadata resultMetadata = resultEvent.metadata();
        final JsonObject resultPayload = resultEvent.payloadAsJsonObject();
        // and
        assertThat(resultMetadata.name(), is(HEARING_LISTED_EVENT));
        // and
        assertThat(resultPayload, isFrom(initiateHearing));

    }

    private JsonEnvelope createListHearingCommand(InitiateHearing initiateHearing) {
        final JsonObject metadataAsJsonObject =
                createObjectBuilder()
                        .add(ID, UUID.next().toString())
                        .add(NAME, LIST_HEARING_COMMAND)
                        .build();

        final JsonObject payloadAsJsonObject = createObjectBuilder()
                .add("hearingId", initiateHearing.getHearingId().toString())
                .add("startDateTime", ZonedDateTimes.toString(initiateHearing.getStartDateTime()))
                .add("duration", initiateHearing.getDuration())
                .build();

        return DefaultJsonEnvelope.envelopeFrom(JsonObjectMetadata.metadataFrom(metadataAsJsonObject), payloadAsJsonObject);

    }

    private HearingInitiated createHearingListed(InitiateHearing initiateHearing) {
        return new HearingInitiated(initiateHearing.getHearingId(),
                initiateHearing.getStartDateTime(), initiateHearing.getDuration(), initiateHearing.getHearingType());
    }

    private InitiateHearing createHearing(UUID hearingId) {
        final ZonedDateTime startDateOfHearing = new UtcClock().now();
        final Integer duration = INTEGER.next();
        final String random = "TRAIL";
        return new InitiateHearing(hearingId, startDateOfHearing, duration, random);
    }

    @Test
    public void shouldAddProsecutionCounsel() throws Exception {

        final JsonEnvelope command = createAddProsecutionCounselCommand();

        when(eventSource.getStreamById(hearingId)).thenReturn(eventStream);
        when(aggregateService.get(eventStream, HearingAggregate.class)).thenReturn(hearingAggregate);
        when(hearingAggregate.addProsecutionCounsel(hearingId, attendeeId, personId, status)).thenReturn(Stream.of(
                new ProsecutionCounselAdded(hearingId, attendeeId, personId, status)));
        when(enveloper.withMetadataFrom(command)).thenReturn(
                createEnveloperWithEvents(ProsecutionCounselAdded.class).withMetadataFrom(command));

        hearingCommandHandler.addProsecutionCounsel(command);

        assertThat(verifyAppendAndGetArgumentFrom(eventStream), streamContaining(
                jsonEnvelope(
                        withMetadataEnvelopedFrom(command)
                                .withName(ADD_PROSECUTION_COUNSEL_EVENT_NAME),
                        payLoad()
                                .isJson(allOf(
                                        withJsonPath("$.personId", equalTo(personId.toString())),
                                        withJsonPath("$.attendeeId", equalTo(attendeeId.toString())),
                                        withJsonPath("$.status", equalTo(status)),
                                        withJsonPath("$.hearingId", equalTo(hearingId.toString()))
                                        )
                                )
                )
        ));
    }

    @Test
    public void shouldRaiseHearingEventLogged() throws Exception {
        final JsonEnvelope command = createHearingEventLoggedCommand();

        hearingCommandHandler.logHearingEvent(command);

        assertThat(verifyAppendAndGetArgumentFrom(eventStream), streamContaining(
                jsonEnvelope(
                        withMetadataEnvelopedFrom(command)
                                .withName(HEARING_EVENT_LOGGED_EVENT),
                        payloadIsJson(allOf(
                                withJsonPath(format("$.%s", HEARING_EVENT_ID_FIELD), equalTo(HEARING_EVENT_ID.toString())),
                                withJsonPath(format("$.%s", HEARING_ID_FIELD), equalTo(HEARING_ID.toString())),
                                withJsonPath(format("$.%s", RECORDED_LABEL_FIELD), equalTo(RECORDED_LABEL)),
                                withJsonPath(format("$.%s", TIMESTAMP_FIELD), notNullValue())
                        ))
                ).thatMatchesSchema()
        ));
    }

    @Test
    public void shouldRaiseDraftResultSaved() throws Exception {
        final JsonEnvelope command = createSaveDraftResultCommand();

        hearingCommandHandler.saveDraftResult(command);

        assertThat(verifyAppendAndGetArgumentFrom(eventStream), streamContaining(
                jsonEnvelope(
                        withMetadataEnvelopedFrom(command)
                                .withName(HEARING_DRAFT_RESULT_SAVED_EVENT),
                        payloadIsJson(allOf(
                                withJsonPath(format("$.%s", DEFENDANT_ID), equalTo(DEFENDANT_ID_VALUE.toString())),
                                withJsonPath(format("$.%s", OFFENCE_ID), equalTo(OFFENCE_ID_VALUE.toString())),
                                withJsonPath(format("$.%s", DRAFT_RESULT), equalTo(ARBITRARY_STRING_IMP_2_YRS)),
                                withJsonPath(format("$.%s", TARGET_ID), equalTo(TARGET_ID_VALUE.toString()))
                        ))
                ).thatMatchesSchema()
        ));
    }

    private JsonEnvelope createHearingEventLoggedCommand() {
        return envelope()
                .with(metadataWithRandomUUID(LOG_HEARING_EVENT_COMMAND))
                .withPayloadOf(HEARING_EVENT_ID, HEARING_EVENT_ID_FIELD)
                .withPayloadOf(HEARING_ID, HEARING_ID_FIELD)
                .withPayloadOf(RECORDED_LABEL, RECORDED_LABEL_FIELD)
                .withPayloadOf(TIMESTAMP, TIMESTAMP_FIELD)
                .build();
    }

    private JsonEnvelope createSaveDraftResultCommand() {
        return envelope()
                .with(metadataWithRandomUUID(SAVE_DRAFT_RESULT_COMMAND))
                .withPayloadOf(HEARING_ID, HEARING_ID_FIELD)
                .withPayloadOf(DEFENDANT_ID_VALUE, DEFENDANT_ID)
                .withPayloadOf(TARGET_ID_VALUE, TARGET_ID)
                .withPayloadOf(OFFENCE_ID_VALUE, OFFENCE_ID)
                .withPayloadOf(ARBITRARY_STRING_IMP_2_YRS, DRAFT_RESULT)
                .build();
    }
    private JsonEnvelope createAddProsecutionCounselCommand() {
        return envelope()
                .with(metadataWithRandomUUID(ADD_PROSECUTION_COUNSEL_EVENT_NAME))
                .withPayloadOf(hearingId, "hearingId")
                .withPayloadOf(personId, "personId")
                .withPayloadOf(attendeeId, "attendeeId")
                .withPayloadOf(status, "status")
                .build();
    }

    private Matcher<JsonObject> isFrom(final InitiateHearing initiateHearing) {
        return new TypeSafeDiagnosingMatcher<JsonObject>() {

            @Override
            public void describeTo(Description description) {
                description.appendText(initiateHearing.toString());
            }

            @Override
            protected boolean matchesSafely(JsonObject resultPayload, Description description) {
                boolean returnStatus = true;

                if (!Objects.equals(initiateHearing.getHearingId(), getUUID(resultPayload, "hearingId").get())) {
                    description.appendText(format("HearingId Mismatch:initiateHearing:%s, hearingListed%s",
                            initiateHearing.getHearingId(), getUUID(resultPayload, "hearingId").get()));
                    returnStatus = false;
                }

                if (!Objects.equals(initiateHearing.getDuration(), getJsonNumber(resultPayload, "duration").get().intValue())) {
                    description.appendText(format("Duration Mismatch:initiateHearing:%s, hearingListed%s",
                            initiateHearing.getDuration(), getJsonNumber(resultPayload, "duration").get().intValue()));
                    returnStatus = false;
                }
                if (!Objects.equals(initiateHearing.getHearingType(), getJsonString(resultPayload, "hearingType").get().getString())) {
                    description.appendText(format("HearingType Mismatch:initiateHearing:%s, hearingType %s",
                            initiateHearing.getHearingType(), getJsonString(resultPayload, "hearingType").get().getString()));
                    returnStatus = false;
                }

                if (!Objects.equals(initiateHearing.getStartDateTime(), fromJsonString(resultPayload.getJsonString("startDateTime")))) {
                    description.appendText(format("StartDateOfHearing Mismatch:initiateHearing:%s, hearingListed%s",
                            initiateHearing.getStartDateTime(), getString(resultPayload, "startDateTime").get()));
                    returnStatus = false;
                }

                return returnStatus;
            }
        };
    }

}
