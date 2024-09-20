package uk.gov.moj.cpp.hearing.command.api;

import static com.jayway.jsonassert.impl.matcher.IsCollectionWithSize.hasSize;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withoutJsonPath;
import static java.lang.Boolean.TRUE;
import static java.lang.String.format;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloper;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.withMetadataEnvelopedFrom;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payloadIsJson;
import static uk.gov.justice.services.test.utils.core.messaging.JsonEnvelopeBuilder.envelope;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUIDAndName;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_ZONED_DATE_TIME;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;

import uk.gov.justice.services.common.converter.ZonedDateTimes;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher;
import uk.gov.moj.cpp.hearing.command.api.service.ReferenceDataService;

import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Stream;

import javax.json.JsonObject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

@SuppressWarnings({"unused", "unchecked"})
public class HearingEventCommandApiTest {

    private static final String FIELD_HEARING_ID = "hearingId";
    private static final String FIELD_HEARING_EVENT_ID = "hearingEventId";
    private static final String FIELD_HEARING_EVENT_DEFINITION_ID = "hearingEventDefinitionId";
    private static final String FIELD_EVENT_TIME = "eventTime";
    private static final String FIELD_LAST_MODIFIED_TIME = "lastModifiedTime";
    private static final String FIELD_RECORDED_LABEL = "recordedLabel";
    private static final String FIELD_ALTERABLE = "alterable";
    private static final String FIELD_ACTIVE_HEARINGS = "activeHearings";
    private static final String FIELD_OVERRIDE = "override";
    private static final String FIELD_HEARING_TYPE_IDS = "hearingTypeIds";

    private static final UUID HEARING_ID = randomUUID();
    private static final UUID HEARING_TYPE_ID = randomUUID();

    private static final UUID HEARING_EVENT_ID = randomUUID();
    private static final UUID HEARING_EVENT_DEFINITION_ID = randomUUID();
    private static final String EVENT_TIME = ZonedDateTimes.toString(PAST_ZONED_DATE_TIME.next());
    private static final String LAST_MODIFIED_TIME = ZonedDateTimes.toString(PAST_ZONED_DATE_TIME.next());
    private static final String RECORDED_LABEL = STRING.next();

    @InjectMocks
    private HearingEventCommandApi hearingEventCommandApi;

    @Mock
    private Sender sender;

    @Mock
    private Requester requester;

    @Mock
    private ReferenceDataService referenceDataService;

    @Spy
    private Enveloper enveloper = createEnveloper();

    @Captor
    private ArgumentCaptor<JsonEnvelope> senderArgumentCaptor;

    @Captor
    private ArgumentCaptor<JsonEnvelope> requesterArgumentCaptor;


    public static Stream<Arguments> provideCorrectAlterableFlags() {
        return Stream.of(
                //isAlterable, expectation
                Arguments.of(true, true), // null strings should be considered blank
                Arguments.of(false, false)
        );
    }

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @ParameterizedTest
    @MethodSource("provideCorrectAlterableFlags")
    public void shouldEnrichWithCorrectAlterableFlagForHearingEventToBeLogged(final boolean alterable, final boolean expectation) {
        final JsonEnvelope command = prepareLogHearingEventCommand();
        fakeEventDefinitionResponse(alterable);
        hearingEventCommandApi.logHearingEvent(command);

        verify(sender).send(senderArgumentCaptor.capture());
        assertLogHearingEventSent(expectation, command, "hearing.command.log-hearing-event");

        verify(requester).request(requesterArgumentCaptor.capture());
        assertGetHearingEventDefinitionEventRequested(command);
    }

    @ParameterizedTest
    @MethodSource("provideCorrectAlterableFlags")
    public void shouldEnrichWithCorrectAlterableFlagForCorrectHearingEventToBeLogged(final boolean alterable, final boolean expectation) {
        final JsonEnvelope command = prepareLogHearingEventCommand();
        fakeEventDefinitionResponse(alterable);
        hearingEventCommandApi.correctEvent(command);

        verify(sender).send(senderArgumentCaptor.capture());
        assertLogHearingEventSent(expectation, command, "hearing.command.correct-hearing-event");

        verify(requester).request(requesterArgumentCaptor.capture());
        assertGetHearingEventDefinitionEventRequested(command);
    }

    @ParameterizedTest
    @MethodSource("provideCorrectAlterableFlags")
    public void shouldLogEventWithCorrectAlterableFlagAnd_OverrideCourtRoomRequested(final boolean alterable, final boolean expectation) {

        final JsonEnvelope command = prepareLogHearingEventCommandWithOverrideFlag();
        when(requester.request(argThat(activeHearingsQuery(command)))).thenReturn(prepareActiveHearingsResponse());
        when(requester.request(argThat(hearingDefinitionQuery(command)))).thenReturn(prepareHearingEventDefinitionResponse(alterable));
        when(referenceDataService.getTrialHearingTypes(command)).thenReturn(Arrays.asList(HEARING_TYPE_ID));

        hearingEventCommandApi.logHearingEvent(command);

        verify(sender).send(senderArgumentCaptor.capture());
        assertLogHearingEventSentWithActiveHearings(expectation, command, "hearing.command.log-hearing-event");
    }

    private void fakeEventDefinitionResponse(final boolean alterable) {
        when(requester.request(any(JsonEnvelope.class))).thenReturn(prepareHearingEventDefinitionResponse(alterable));
    }

    private JsonEnvelopeMatcher activeHearingsQuery(final JsonEnvelope envelope) {
        return jsonEnvelope(
                withMetadataEnvelopedFrom(envelope).withName("hearing.get-active-hearings-for-court-room"),
                payloadIsJson(withJsonPath(format("$.%s", FIELD_HEARING_ID), equalTo(HEARING_ID.toString()))));
    }

    private JsonEnvelopeMatcher hearingDefinitionQuery(final JsonEnvelope envelope) {
        return jsonEnvelope(
                withMetadataEnvelopedFrom(envelope).withName("hearing.get-hearing-event-definition"),
                payloadIsJson(allOf(
                        withJsonPath(format("$.%s", FIELD_HEARING_ID), equalTo(HEARING_ID.toString())),
                        withJsonPath(format("$.%s", FIELD_HEARING_EVENT_DEFINITION_ID), equalTo(HEARING_EVENT_DEFINITION_ID.toString()))
                )));
    }

    private void assertGetHearingEventDefinitionEventRequested(final JsonEnvelope command) {
        assertThat(requesterArgumentCaptor.getValue(), is(jsonEnvelope(
                withMetadataEnvelopedFrom(command)
                        .withName("hearing.get-hearing-event-definition"),
                payloadIsJson(allOf(
                        withJsonPath(format("$.%s", FIELD_HEARING_ID), equalTo(HEARING_ID.toString())),
                        withJsonPath(format("$.%s", FIELD_HEARING_EVENT_DEFINITION_ID), equalTo(HEARING_EVENT_DEFINITION_ID.toString()))
                )))
        ));
    }

    private void assertSenderPassThroughMessage(final JsonEnvelope command) {
        assertThat(senderArgumentCaptor.getValue(), is(jsonEnvelope(
                withMetadataEnvelopedFrom(command)
                        .withName("dummy")
                        .withCausationIds(),
                payloadIsJson(allOf(
                        withoutJsonPath(format("$.%s", FIELD_ALTERABLE))
                )))
        ));
    }

    private void assertLogHearingEventSent(final boolean expectation, final JsonEnvelope command, final String eventName) {
        assertThat(senderArgumentCaptor.getValue(), is(jsonEnvelope(
                withMetadataEnvelopedFrom(command)
                        .withName(eventName),
                payloadIsJson(allOf(
                        withJsonPath(format("$.%s", FIELD_HEARING_ID), equalTo(HEARING_ID.toString())),
                        withJsonPath(format("$.%s", FIELD_HEARING_EVENT_ID), equalTo(HEARING_EVENT_ID.toString())),
                        withJsonPath(format("$.%s", FIELD_HEARING_EVENT_DEFINITION_ID), equalTo(HEARING_EVENT_DEFINITION_ID.toString())),
                        withJsonPath(format("$.%s", FIELD_RECORDED_LABEL), equalTo(RECORDED_LABEL)),
                        withJsonPath(format("$.%s", FIELD_EVENT_TIME), equalTo(EVENT_TIME)),
                        withJsonPath(format("$.%s", FIELD_LAST_MODIFIED_TIME), equalTo(LAST_MODIFIED_TIME)),
                        withJsonPath(format("$.%s", FIELD_ALTERABLE), equalTo(expectation))
                )))
        ));
    }

    private void assertLogHearingEventSentWithActiveHearings(final boolean expectation, final JsonEnvelope command, final String eventName) {
        assertThat(senderArgumentCaptor.getValue(), is(jsonEnvelope(
                withMetadataEnvelopedFrom(command)
                        .withName(eventName),
                payloadIsJson(allOf(
                        withJsonPath(format("$.%s", FIELD_HEARING_ID), equalTo(HEARING_ID.toString())),
                        withJsonPath(format("$.%s", FIELD_HEARING_EVENT_ID), equalTo(HEARING_EVENT_ID.toString())),
                        withJsonPath(format("$.%s", FIELD_HEARING_EVENT_DEFINITION_ID), equalTo(HEARING_EVENT_DEFINITION_ID.toString())),
                        withJsonPath(format("$.%s", FIELD_RECORDED_LABEL), equalTo(RECORDED_LABEL)),
                        withJsonPath(format("$.%s", FIELD_EVENT_TIME), equalTo(EVENT_TIME)),
                        withJsonPath(format("$.%s", FIELD_LAST_MODIFIED_TIME), equalTo(LAST_MODIFIED_TIME)),
                        withJsonPath(format("$.%s", FIELD_OVERRIDE), equalTo(TRUE)),
                        withJsonPath(format("$.%s", FIELD_ALTERABLE), equalTo(expectation)),
                        withJsonPath(format("$.%s", FIELD_ACTIVE_HEARINGS), hasSize(1)),
                        withJsonPath(format("$.%s[0]", FIELD_ACTIVE_HEARINGS), equalTo(HEARING_ID.toString())),
                        withJsonPath(format("$.%s", FIELD_HEARING_TYPE_IDS), hasSize(1)),
                        withJsonPath(format("$.%s[0]", FIELD_HEARING_TYPE_IDS), equalTo(HEARING_TYPE_ID.toString()))
                )))
        ));
    }

    private JsonEnvelope prepareHearingEventDefinitionResponse(final boolean alterable) {
        return envelope()
                .with(metadataWithRandomUUIDAndName())
                .withPayloadOf(alterable, FIELD_ALTERABLE)
                .build();
    }

    private JsonEnvelope prepareActiveHearingsResponse() {
        return envelopeFrom(metadataWithRandomUUIDAndName(), mockActiveHearingsPayload());
    }

    private JsonEnvelope prepareLogHearingEventCommand() {
        return envelope()
                .with(metadataWithRandomUUIDAndName())
                .withPayloadOf(HEARING_ID, FIELD_HEARING_ID)
                .withPayloadOf(HEARING_EVENT_ID, FIELD_HEARING_EVENT_ID)
                .withPayloadOf(HEARING_EVENT_DEFINITION_ID, FIELD_HEARING_EVENT_DEFINITION_ID)
                .withPayloadOf(RECORDED_LABEL, FIELD_RECORDED_LABEL)
                .withPayloadOf(EVENT_TIME, FIELD_EVENT_TIME)
                .withPayloadOf(LAST_MODIFIED_TIME, FIELD_LAST_MODIFIED_TIME)
                .build();
    }

    private JsonEnvelope prepareLogHearingEventCommandWithNoDefinitionId() {
        return envelope()
                .with(metadataWithRandomUUIDAndName())
                .withPayloadOf(HEARING_ID, FIELD_HEARING_ID)
                .withPayloadOf(HEARING_EVENT_ID, FIELD_HEARING_EVENT_ID)
                .withPayloadOf(RECORDED_LABEL, FIELD_RECORDED_LABEL)
                .withPayloadOf(EVENT_TIME, FIELD_EVENT_TIME)
                .withPayloadOf(LAST_MODIFIED_TIME, FIELD_LAST_MODIFIED_TIME)
                .build();
    }

    private JsonEnvelope prepareLogHearingEventCommandWithOverrideFlag() {
        return envelope()
                .with(metadataWithRandomUUIDAndName())
                .withPayloadOf(HEARING_ID, FIELD_HEARING_ID)
                .withPayloadOf(HEARING_EVENT_ID, FIELD_HEARING_EVENT_ID)
                .withPayloadOf(HEARING_EVENT_DEFINITION_ID, FIELD_HEARING_EVENT_DEFINITION_ID)
                .withPayloadOf(RECORDED_LABEL, FIELD_RECORDED_LABEL)
                .withPayloadOf(EVENT_TIME, FIELD_EVENT_TIME)
                .withPayloadOf(LAST_MODIFIED_TIME, FIELD_LAST_MODIFIED_TIME)
                .withPayloadOf(TRUE, FIELD_OVERRIDE)
                .build();
    }

    private static JsonObject mockActiveHearingsPayload() {
        return createObjectBuilder().add(FIELD_ACTIVE_HEARINGS, createArrayBuilder()
                .add(HEARING_ID.toString())
        ).build();
    }
}