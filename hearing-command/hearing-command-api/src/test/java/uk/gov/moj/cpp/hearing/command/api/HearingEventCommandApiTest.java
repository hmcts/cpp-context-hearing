package uk.gov.moj.cpp.hearing.command.api;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withoutJsonPath;
import static java.lang.String.format;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataWithRandomUUIDAndName;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloper;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.withMetadataEnvelopedFrom;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payloadIsJson;
import static uk.gov.justice.services.test.utils.core.messaging.JsonEnvelopeBuilder.envelope;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_UTC_DATE_TIME;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;

import uk.gov.justice.services.common.converter.ZonedDateTimes;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.UUID;

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

@SuppressWarnings({"unused", "unchecked"})
@RunWith(DataProviderRunner.class)
public class HearingEventCommandApiTest {

    private static final String FIELD_HEARING_ID = "hearingId";
    private static final String FIELD_HEARING_EVENT_ID = "hearingEventId";
    private static final String FIELD_HEARING_EVENT_DEFINITION_ID = "hearingEventDefinitionId";
    private static final String FIELD_EVENT_TIME = "eventTime";
    private static final String FIELD_LAST_MODIFIED_TIME = "lastModifiedTime";
    private static final String FIELD_RECORDED_LABEL = "recordedLabel";
    private static final String FIELD_ALTERABLE = "alterable";

    private static final UUID HEARING_ID = randomUUID();

    private static final UUID HEARING_EVENT_ID = randomUUID();
    private static final UUID HEARING_EVENT_DEFINITION_ID = randomUUID();
    private static final String EVENT_TIME = ZonedDateTimes.toString(PAST_UTC_DATE_TIME.next());
    private static final String LAST_MODIFIED_TIME = ZonedDateTimes.toString(PAST_UTC_DATE_TIME.next());
    private static final String RECORDED_LABEL = STRING.next();

    @InjectMocks
    private HearingEventCommandApi hearingEventCommandApi;

    @Mock
    private Sender sender;

    @Mock
    private Requester requester;

    @Spy
    private Enveloper enveloper = createEnveloper();

    @Captor
    private ArgumentCaptor<JsonEnvelope> senderArgumentCaptor;

    @Captor
    private ArgumentCaptor<JsonEnvelope> requesterArgumentCaptor;

    @DataProvider
    public static Object[][] provideCorrectAlterableFlags() {
        return new Object[][]{
                //isAlterable, expectation
                {true, true},
                {false, false}
        };
    }

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @UseDataProvider("provideCorrectAlterableFlags")
    @Test
    public void shouldEnrichWithCorrectAlterableFlagForHearingEventToBeLogged(final boolean alterable, final boolean expectation) {
        final JsonEnvelope command = prepareLogHearingEventCommand();
        fakeEventDefinitionResponse(alterable);
        hearingEventCommandApi.logHearingEvent(command);

        verify(sender).send(senderArgumentCaptor.capture());
        assertLogHearingEventSent(expectation, command, "hearing.log-hearing-event");

        verify(requester).request(requesterArgumentCaptor.capture());
        assertGetHearingEventDefinitionEventRequested(command);
    }

    @UseDataProvider("provideCorrectAlterableFlags")
    @Test
    public void shouldEnrichWithCorrectAlterableFlagForCorrectHearingEventToBeLogged(final boolean alterable, final boolean expectation) {
        final JsonEnvelope command = prepareLogHearingEventCommand();
        fakeEventDefinitionResponse(alterable);
        hearingEventCommandApi.correctEvent(command);

        verify(sender).send(senderArgumentCaptor.capture());
        assertLogHearingEventSent(expectation, command, "hearing.correct-hearing-event");

        verify(requester).request(requesterArgumentCaptor.capture());
        assertGetHearingEventDefinitionEventRequested(command);
    }

    private void fakeEventDefinitionResponse(final boolean alterable) {
        when(requester.request(any(JsonEnvelope.class))).thenReturn(prepareHearingEventDefinitionResponse(alterable));
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

    private JsonEnvelope prepareHearingEventDefinitionResponse(final boolean alterable) {
        return envelope()
                .with(metadataWithRandomUUIDAndName())
                .withPayloadOf(alterable, FIELD_ALTERABLE)
                .build();
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

}