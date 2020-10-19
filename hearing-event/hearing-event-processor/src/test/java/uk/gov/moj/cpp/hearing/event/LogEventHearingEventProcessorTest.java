package uk.gov.moj.cpp.hearing.event;

import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static uk.gov.justice.services.test.utils.core.enveloper.EnvelopeFactory.createEnvelope;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloper;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.metadata;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payloadIsJson;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.BOOLEAN;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_ZONED_DATE_TIME;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.print;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;

import uk.gov.justice.core.courts.JurisdictionType;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonValueConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.test.utils.framework.api.JsonObjectConvertersFactory;
import uk.gov.moj.cpp.hearing.command.updateEvent.HearingEvent;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventIgnored;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventLogged;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventsUpdated;
import uk.gov.moj.cpp.hearing.eventlog.Case;
import uk.gov.moj.cpp.hearing.eventlog.PublicHearingEventLogged;

import java.io.IOException;
import java.time.ZoneId;
import java.util.Arrays;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import org.hamcrest.Matchers;
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
public class LogEventHearingEventProcessorTest {

    @Spy
    private final Enveloper enveloper = createEnveloper();
    @Spy
    private final ObjectMapper objectMapper = new ObjectMapperProducer().objectMapper();
    @Spy
    private final JsonObjectToObjectConverter jsonObjectToObjectConverter = new JsonObjectConvertersFactory().jsonObjectToObjectConverter();
    @Spy
    @InjectMocks
    private final ObjectToJsonValueConverter objectToJsonValueConverter = new ObjectToJsonValueConverter(this.objectMapper);
    @Spy
    private final ObjectToJsonObjectConverter objectToJsonObjectConverter = new JsonObjectConvertersFactory().objectToJsonObjectConverter();
    @InjectMocks
    private LogEventHearingEventProcessor logEventHearingEventProcessor;
    @Mock
    private Sender sender;
    @Mock
    private Requester requester;
    @Mock
    private JsonEnvelope responseEnvelope;
    @Captor
    private ArgumentCaptor<JsonEnvelope> envelopeArgumentCaptor;

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldPublishHearingEventLoggedPublicEvent() {

        final HearingEventLogged hearingEventLogged = new HearingEventLogged(randomUUID(), null, randomUUID(), randomUUID(),
                randomUUID(), STRING.next(), PAST_ZONED_DATE_TIME.next(), PAST_ZONED_DATE_TIME.next(), BOOLEAN.next(),
                new uk.gov.moj.cpp.hearing.domain.CourtCentre(randomUUID(), STRING.next(), randomUUID(), STRING.next(), STRING.next(), STRING.next()),
                new uk.gov.moj.cpp.hearing.domain.HearingType(STRING.next(), randomUUID()), STRING.next(), JurisdictionType.CROWN);

        this.logEventHearingEventProcessor.publishHearingEventLoggedPublicEvent(
                createEnvelope("hearing.hearing-event-logged", this.objectToJsonObjectConverter.convert(hearingEventLogged)));

        verify(this.sender).send(this.envelopeArgumentCaptor.capture());

        assertThat(this.envelopeArgumentCaptor.getValue(), jsonEnvelope(metadata().withName("public.hearing.event-logged"), payloadIsJson(print())));
        assertThat(uk.gov.moj.cpp.hearing.test.ObjectConverters.asPojo(this.envelopeArgumentCaptor.getValue(), PublicHearingEventLogged.class), isBean(PublicHearingEventLogged.class)
                .with(PublicHearingEventLogged::getCase, isBean(Case.class)
                        .with(Case::getCaseUrn, Matchers.is(hearingEventLogged.getCaseURN())))
                .with(PublicHearingEventLogged::getHearingEventDefinition, isBean(uk.gov.moj.cpp.hearing.eventlog.HearingEventDefinition.class)
                        .with(uk.gov.moj.cpp.hearing.eventlog.HearingEventDefinition::getHearingEventDefinitionId, Matchers.is(hearingEventLogged.getHearingEventDefinitionId()))
                        .with(uk.gov.moj.cpp.hearing.eventlog.HearingEventDefinition::isPriority, Matchers.is(!hearingEventLogged.isAlterable())))
                .with(PublicHearingEventLogged::getHearingEvent, isBean(uk.gov.moj.cpp.hearing.eventlog.HearingEvent.class)
                        .with(uk.gov.moj.cpp.hearing.eventlog.HearingEvent::getHearingEventId, Matchers.is(hearingEventLogged.getHearingEventId()))
                        .with(uk.gov.moj.cpp.hearing.eventlog.HearingEvent::getRecordedLabel, Matchers.is(hearingEventLogged.getRecordedLabel()))
                        .with(uk.gov.moj.cpp.hearing.eventlog.HearingEvent::getEventTime, Matchers.is(hearingEventLogged.getEventTime().toLocalDateTime().atZone(ZoneId.of("UTC"))))
                        .with(uk.gov.moj.cpp.hearing.eventlog.HearingEvent::getLastModifiedTime, Matchers.is(hearingEventLogged.getLastModifiedTime().toLocalDateTime().atZone(ZoneId.of("UTC")))))
                .with(PublicHearingEventLogged::getHearing, isBean(uk.gov.moj.cpp.hearing.eventlog.Hearing.class)
                        .with(uk.gov.moj.cpp.hearing.eventlog.Hearing::getCourtCentre, isBean(uk.gov.moj.cpp.hearing.eventlog.CourtCentre.class)
                                .with(uk.gov.moj.cpp.hearing.eventlog.CourtCentre::getCourtCentreId, Matchers.is(hearingEventLogged.getCourtCentre().getId()))
                                .with(uk.gov.moj.cpp.hearing.eventlog.CourtCentre::getCourtCentreName, Matchers.is(hearingEventLogged.getCourtCentre().getName()))
                                .with(uk.gov.moj.cpp.hearing.eventlog.CourtCentre::getCourtRoomId, Matchers.is(hearingEventLogged.getCourtCentre().getRoomId()))
                                .with(uk.gov.moj.cpp.hearing.eventlog.CourtCentre::getCourtRoomName, Matchers.is(hearingEventLogged.getCourtCentre().getRoomName())))
                        .with(uk.gov.moj.cpp.hearing.eventlog.Hearing::getHearingType, Matchers.is(hearingEventLogged.getHearingType().getDescription()))));
    }

    @Test
    public void shouldPublishHearingEventTimeStampCorrectedPublicEvent() {

        final HearingEventLogged hearingEventLogged = new HearingEventLogged(randomUUID(), randomUUID(), randomUUID(), randomUUID(),
                null, STRING.next(), PAST_ZONED_DATE_TIME.next(), PAST_ZONED_DATE_TIME.next(), BOOLEAN.next(),
                new uk.gov.moj.cpp.hearing.domain.CourtCentre(randomUUID(), STRING.next(), randomUUID(), STRING.next(), STRING.next(), STRING.next()),
                new uk.gov.moj.cpp.hearing.domain.HearingType(STRING.next(), randomUUID()), STRING.next(), JurisdictionType.CROWN);

        this.logEventHearingEventProcessor.publishHearingEventLoggedPublicEvent(
                createEnvelope("hearing.hearing-event-logged", this.objectToJsonObjectConverter.convert(hearingEventLogged)));

        verify(this.sender).send(this.envelopeArgumentCaptor.capture());

        assertThat(this.envelopeArgumentCaptor.getValue(), jsonEnvelope(metadata().withName("public.hearing.event-timestamp-corrected"), payloadIsJson(print())));
        assertThat(uk.gov.moj.cpp.hearing.test.ObjectConverters.asPojo(this.envelopeArgumentCaptor.getValue(), PublicHearingEventLogged.class), isBean(PublicHearingEventLogged.class)
                .with(PublicHearingEventLogged::getCase, isBean(Case.class)
                        .with(Case::getCaseUrn, Matchers.is(hearingEventLogged.getCaseURN())))
                .with(PublicHearingEventLogged::getHearingEventDefinition, isBean(uk.gov.moj.cpp.hearing.eventlog.HearingEventDefinition.class)
                        .with(uk.gov.moj.cpp.hearing.eventlog.HearingEventDefinition::getHearingEventDefinitionId, Matchers.is(hearingEventLogged.getHearingEventDefinitionId()))
                        .with(uk.gov.moj.cpp.hearing.eventlog.HearingEventDefinition::isPriority, Matchers.is(!hearingEventLogged.isAlterable())))
                .with(PublicHearingEventLogged::getHearingEvent, isBean(uk.gov.moj.cpp.hearing.eventlog.HearingEvent.class)
                        .with(uk.gov.moj.cpp.hearing.eventlog.HearingEvent::getHearingEventId, Matchers.is(hearingEventLogged.getHearingEventId()))
                        .with(uk.gov.moj.cpp.hearing.eventlog.HearingEvent::getRecordedLabel, Matchers.is(hearingEventLogged.getRecordedLabel()))
                        .with(uk.gov.moj.cpp.hearing.eventlog.HearingEvent::getEventTime, Matchers.is(hearingEventLogged.getEventTime().toLocalDateTime().atZone(ZoneId.of("UTC"))))
                        .with(uk.gov.moj.cpp.hearing.eventlog.HearingEvent::getLastModifiedTime, Matchers.is(hearingEventLogged.getLastModifiedTime().toLocalDateTime().atZone(ZoneId.of("UTC")))))
                .with(PublicHearingEventLogged::getHearing, isBean(uk.gov.moj.cpp.hearing.eventlog.Hearing.class)
                        .with(uk.gov.moj.cpp.hearing.eventlog.Hearing::getCourtCentre, isBean(uk.gov.moj.cpp.hearing.eventlog.CourtCentre.class)
                                .with(uk.gov.moj.cpp.hearing.eventlog.CourtCentre::getCourtCentreId, Matchers.is(hearingEventLogged.getCourtCentre().getId()))
                                .with(uk.gov.moj.cpp.hearing.eventlog.CourtCentre::getCourtCentreName, Matchers.is(hearingEventLogged.getCourtCentre().getName()))
                                .with(uk.gov.moj.cpp.hearing.eventlog.CourtCentre::getCourtRoomId, Matchers.is(hearingEventLogged.getCourtCentre().getRoomId()))
                                .with(uk.gov.moj.cpp.hearing.eventlog.CourtCentre::getCourtRoomName, Matchers.is(hearingEventLogged.getCourtCentre().getRoomName())))
                        .with(uk.gov.moj.cpp.hearing.eventlog.Hearing::getHearingType, Matchers.is(hearingEventLogged.getHearingType().getDescription()))));
    }

    @Test
    public void publishHearingVerdictUpdatedPublicEvent() throws IOException {

        final HearingEventIgnored hearingEventIgnored =
                new HearingEventIgnored(randomUUID(), randomUUID(), randomUUID(), STRING.next(), PAST_ZONED_DATE_TIME.next(), STRING.next(), BOOLEAN.next());

        this.logEventHearingEventProcessor.publishHearingEventIgnoredPublicEvent(createEnvelope("hearing.hearing-event-ignored",
                this.objectToJsonObjectConverter.convert(hearingEventIgnored)));

        verify(this.sender).send(this.envelopeArgumentCaptor.capture());

        assertThat(this.envelopeArgumentCaptor.getValue(), jsonEnvelope(metadata().withName("public.hearing.event-ignored"), payloadIsJson(print())));
        assertThat(uk.gov.moj.cpp.hearing.test.ObjectConverters.asPojo(this.envelopeArgumentCaptor.getValue(), HearingEventIgnored.class), isBean(HearingEventIgnored.class)
                .with(HearingEventIgnored::getHearingId, Matchers.is(hearingEventIgnored.getHearingId())));
    }

    @Test
    public void publishHearingEventsUpdated() throws IOException {

        final HearingEventsUpdated hearingEventsUpdated =
                new HearingEventsUpdated(randomUUID(), Arrays.asList(new HearingEvent(randomUUID(), "RL")));

        this.logEventHearingEventProcessor.publishHearingEventsUpdatedEvent(createEnvelope("hearing.hearing-events-updated",
                this.objectToJsonObjectConverter.convert(hearingEventsUpdated)));

        verify(this.sender).send(this.envelopeArgumentCaptor.capture());

        assertThat(this.envelopeArgumentCaptor.getValue(), jsonEnvelope(metadata().withName("public.hearing.events-updated"), payloadIsJson(print())));
        assertThat(uk.gov.moj.cpp.hearing.test.ObjectConverters.asPojo(this.envelopeArgumentCaptor.getValue(), HearingEventsUpdated.class), isBean(HearingEventsUpdated.class)
                .with(HearingEventsUpdated::getHearingId, Matchers.is(hearingEventsUpdated.getHearingId())));
    }
}