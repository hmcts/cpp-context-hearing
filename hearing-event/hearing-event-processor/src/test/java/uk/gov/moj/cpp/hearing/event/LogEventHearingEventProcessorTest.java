package uk.gov.moj.cpp.hearing.event;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static uk.gov.justice.services.test.utils.core.enveloper.EnvelopeFactory.createEnvelope;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloper;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.metadata;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payloadIsJson;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_ZONED_DATE_TIME;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;

import java.io.IOException;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonValueConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.updateEvent.HearingEvent;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventIgnored;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventLogged;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventsUpdated;

@SuppressWarnings({"unchecked", "unused"})
@RunWith(DataProviderRunner.class)
public class LogEventHearingEventProcessorTest {

    @InjectMocks
    private LogEventHearingEventProcessor logEventHearingEventProcessor;

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
    private final ObjectMapper objectMapper = new ObjectMapperProducer().objectMapper();

    @Spy
    @InjectMocks
    private final JsonObjectToObjectConverter jsonObjectToObjectConverter = new JsonObjectToObjectConverter();

    @Spy
    @InjectMocks
    private final ObjectToJsonValueConverter objectToJsonValueConverter = new ObjectToJsonValueConverter(this.objectMapper);

    @InjectMocks
    private final ObjectToJsonObjectConverter objectToJsonObjectConverter = new ObjectToJsonObjectConverter();

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldPublishHearingEventLoggedPublicEvent() {

        final UUID lastHearingEventId = null;

        final String caseUrn = (STRING.next() + STRING.next()).substring(0, 11);

        final HearingEventLogged event = new HearingEventLogged(randomUUID(), lastHearingEventId, randomUUID(),
                randomUUID(), STRING.next(), PAST_ZONED_DATE_TIME.next(), PAST_ZONED_DATE_TIME.next(), true,
                randomUUID(), STRING.next(), randomUUID(), STRING.next(), STRING.next(), caseUrn, randomUUID(),
                                        null, null);

        this.logEventHearingEventProcessor.publishHearingEventLoggedPublicEvent(
                createEnvelope("hearing.hearing-event-logged", this.objectToJsonObjectConverter.convert(event))
        );

        verify(this.sender).send(this.envelopeArgumentCaptor.capture());

        assertThat(this.envelopeArgumentCaptor.getValue(), jsonEnvelope(
                metadata().withName("public.hearing.event-logged"),
                payloadIsJson(allOf(
                        withJsonPath("$.case.caseUrn", is(event.getCaseUrn())),
                        withJsonPath("$.hearingEventDefinition.hearingEventDefinitionId", is(event.getHearingEventDefinitionId().toString())),
                        withJsonPath("$.hearingEventDefinition.priority", is(!event.isAlterable())),
                        withJsonPath("$.hearingEvent.hearingEventId", is(event.getHearingEventId().toString())),
                        withJsonPath("$.hearingEvent.recordedLabel", is(event.getRecordedLabel())),
                        withJsonPath("$.hearingEvent.eventTime", is(event.getEventTime().toLocalDateTime().atZone(ZoneId.of("Z")).toString())),
                        withJsonPath("$.hearingEvent.lastModifiedTime", is(event.getLastModifiedTime().toLocalDateTime().atZone(ZoneId.of("Z")).toString())),
                        withJsonPath("$.hearing.courtCentre.courtCentreId", is(event.getCourtCentreId().toString())),
                        withJsonPath("$.hearing.courtCentre.courtCentreName", is(event.getCourtCentreName())),
                        withJsonPath("$.hearing.courtCentre.courtRoomId", is(event.getCourtRoomId().toString())),
                        withJsonPath("$.hearing.courtCentre.courtRoomName", is(event.getCourtRoomName())),
                        withJsonPath("$.hearing.hearingType", is(event.getHearingType()))
                        )
                )).thatMatchesSchema());
    }

    @Test
    public void shouldPublishHearingEventTimeStampCorrectedPublicEvent() {

        final UUID lastHearingEventId = randomUUID();

        final String caseUrn = (STRING.next() + STRING.next()).substring(0, 11);

        final HearingEventLogged event = new HearingEventLogged(randomUUID(), lastHearingEventId, randomUUID(),
                randomUUID(), STRING.next(), PAST_ZONED_DATE_TIME.next(), PAST_ZONED_DATE_TIME.next(), true,
                randomUUID(), STRING.next(), randomUUID(), STRING.next(), STRING.next(), caseUrn, randomUUID(),
                                        null, null);

        this.logEventHearingEventProcessor.publishHearingEventLoggedPublicEvent(
                createEnvelope("hearing.hearing-event-logged", this.objectToJsonObjectConverter.convert(event))
        );

        verify(this.sender).send(this.envelopeArgumentCaptor.capture());

        assertThat(this.envelopeArgumentCaptor.getValue(), jsonEnvelope(
                metadata().withName("public.hearing.event-timestamp-corrected"),
                payloadIsJson(allOf(
                        withJsonPath("$.hearingEvent.hearingEventId", is(event.getHearingEventId().toString()))
                        )
                )).thatMatchesSchema());
    }


    @Test
    public void publishHearingVerdictUpdatedPublicEvent() throws IOException {

        final HearingEventIgnored hearingEventIgnored = new HearingEventIgnored(
                randomUUID(), randomUUID(), randomUUID(), STRING.next(), PAST_ZONED_DATE_TIME.next(), STRING.next(), false
        );

        this.logEventHearingEventProcessor.publishHearingEventIgnoredPublicEvent(
                createEnvelope("hearing.hearing-event-ignored", this.objectToJsonObjectConverter.convert(hearingEventIgnored))
        );

        verify(this.sender).send(this.envelopeArgumentCaptor.capture());
        assertThat(this.envelopeArgumentCaptor.getValue(), jsonEnvelope(
                metadata().withName("public.hearing.event-ignored"),
                payloadIsJson(allOf(
                        withJsonPath("$.hearingId", equalTo(hearingEventIgnored.getHearingId().toString()))
                        )
                )));
    }

    @Test
    public void publishHearingEventsUpdated() throws IOException {

        final HearingEventsUpdated hearingEventsUpdated =
                        new HearingEventsUpdated(randomUUID(),
                                        Arrays.asList(new HearingEvent(randomUUID(), randomUUID(),
                                                        PAST_ZONED_DATE_TIME.next(), "RL",
                                                        PAST_ZONED_DATE_TIME.next(),
                                                        randomUUID())));

        this.logEventHearingEventProcessor.publishHearingEventsUpdatedEvent(createEnvelope(
                        "hearing.hearing-events-updated",
                        this.objectToJsonObjectConverter.convert(hearingEventsUpdated)));

        verify(this.sender).send(this.envelopeArgumentCaptor.capture());
        assertThat(this.envelopeArgumentCaptor.getValue(), jsonEnvelope(
                        metadata().withName("public.hearing.events-updated"),
                        payloadIsJson(allOf(withJsonPath("$.hearingId",
                                        equalTo(hearingEventsUpdated.getHearingId()
                                                        .toString()))))));
    }

}