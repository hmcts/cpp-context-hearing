package uk.gov.moj.cpp.hearing.command.handler;

import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.common.reflection.ReflectionUtils.setField;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloperWithEvents;
import static uk.gov.justice.services.test.utils.core.helper.EventStreamMockHelper.verifyAppendAndGetArgumentFrom;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.metadata;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payloadIsJson;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_ZONED_DATE_TIME;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.HearingEventDefinitionsTemplates.buildCreateHearingEventDefinitionsCommand;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.print;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.with;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;

import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.hamcrest.core.IsNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import uk.gov.justice.domain.aggregate.Aggregate;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.aggregate.AggregateService;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.command.logEvent.CorrectLogEventCommand;
import uk.gov.moj.cpp.hearing.command.logEvent.CreateHearingEventDefinitionsCommand;
import uk.gov.moj.cpp.hearing.command.logEvent.LogEventCommand;
import uk.gov.moj.cpp.hearing.domain.CourtCentre;
import uk.gov.moj.cpp.hearing.domain.HearingType;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingEventDefinitionAggregate;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventDefinitionsCreated;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventDefinitionsDeleted;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventDeleted;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventIgnored;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventLogged;
import uk.gov.moj.cpp.hearing.domain.event.HearingInitiated;

@SuppressWarnings({"unchecked"})
@RunWith(MockitoJUnitRunner.class)
public class HearingEventCommandHandlerTest {

    @Mock
    private EventStream eventStream;
    @Mock
    private EventSource eventSource;
    @Mock
    private AggregateService aggregateService;
    @Spy
    private final Enveloper enveloper = createEnveloperWithEvents(
            HearingEventLogged.class, HearingEventDefinitionsCreated.class,
            HearingEventDeleted.class, HearingEventIgnored.class,
            HearingEventDefinitionsDeleted.class);
    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;
    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;
    @InjectMocks
    private HearingEventCommandHandler hearingEventCommandHandler;

    @Before
    public void setup() {
        setField(this.jsonObjectToObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
    }

    @Test
    public void shouldAlwaysRaiseHearingEventDefinitionsDeletedAndCreatedEvents() throws Exception {

        final CreateHearingEventDefinitionsCommand createHearingEventDefinitionsCommand = buildCreateHearingEventDefinitionsCommand();

        setupMockedEventStream(createHearingEventDefinitionsCommand.getId(), this.eventStream, new HearingEventDefinitionAggregate());

        final JsonEnvelope jsonEnvelopCommand = envelopeFrom(metadataWithRandomUUID("hearing.create-hearing-event-definitions"), 
                objectToJsonObjectConverter.convert(createHearingEventDefinitionsCommand));

        hearingEventCommandHandler.createHearingEventDefinitions(jsonEnvelopCommand);

        final List<JsonEnvelope> jsonEnvelopeEvents = verifyAppendAndGetArgumentFrom(eventStream).collect(Collectors.toList());

        assertThat(jsonEnvelopeEvents.get(0), jsonEnvelope(metadata().withName("hearing.hearing-event-definitions-deleted"), payloadIsJson(print())));

        assertThat(uk.gov.moj.cpp.hearing.test.ObjectConverters.asPojo(jsonEnvelopeEvents.get(0), HearingEventDefinitionsDeleted.class), isBean(HearingEventDefinitionsDeleted.class)
                .with(HearingEventDefinitionsDeleted::getId, is(createHearingEventDefinitionsCommand.getId()))
        );

        assertThat(jsonEnvelopeEvents.get(1), jsonEnvelope(metadata().withName("hearing.hearing-event-definitions-created"), payloadIsJson(print())));

        assertThat(uk.gov.moj.cpp.hearing.test.ObjectConverters.asPojo(jsonEnvelopeEvents.get(1), HearingEventDefinitionsCreated.class), isBean(HearingEventDefinitionsCreated.class)
                .with(HearingEventDefinitionsCreated::getId, is(createHearingEventDefinitionsCommand.getId()))
                .with(HearingEventDefinitionsCreated::getEventDefinitions, is(createHearingEventDefinitionsCommand.getEventDefinitions()))
        );
    }

    @Test
    public void logHearingEvent_shouldRaiseHearingEventLogged() throws Exception {

        final InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplate();
        final UUID hearingId = initiateHearingCommand.getHearing().getId();

        final LogEventCommand logEventCommand = new LogEventCommand(randomUUID(), hearingId, randomUUID(), STRING.next(),
                PAST_ZONED_DATE_TIME.next(), PAST_ZONED_DATE_TIME.next(), false, randomUUID());

        setupMockedEventStream(hearingId, this.eventStream, with(new HearingAggregate(), a -> {
            a.apply(new HearingInitiated(initiateHearingCommand.getHearing()));
        }));

        final JsonEnvelope jsonEnvelopCommand = envelopeFrom(metadataWithRandomUUID("hearing.log-hearing-event"), objectToJsonObjectConverter.convert(logEventCommand));

        hearingEventCommandHandler.logHearingEvent(jsonEnvelopCommand);

        final JsonEnvelope jsonEnvelopeEvent = verifyAppendAndGetArgumentFrom(eventStream).findFirst().get();

        assertThat(jsonEnvelopeEvent, jsonEnvelope(metadata().withName("hearing.hearing-event-logged"), payloadIsJson(print())));

        assertThat(uk.gov.moj.cpp.hearing.test.ObjectConverters.asPojo(jsonEnvelopeEvent, HearingEventLogged.class), isBean(HearingEventLogged.class)
                .with(HearingEventLogged::getHearingEventDefinitionId, is(logEventCommand.getHearingEventDefinitionId()))
                .with(HearingEventLogged::getHearingEventId, is(logEventCommand.getHearingEventId()))
                .with(HearingEventLogged::getLastHearingEventId, IsNull.nullValue())
                .with(HearingEventLogged::getDefenceCounselId, is(logEventCommand.getDefenceCounselId()))
                .with(HearingEventLogged::getHearingId, is(logEventCommand.getHearingId()))
                .with(HearingEventLogged::getHearingType, isBean(uk.gov.moj.cpp.hearing.domain.HearingType.class)
                        .with(uk.gov.moj.cpp.hearing.domain.HearingType::getId, is(initiateHearingCommand.getHearing().getType().getId()))
                        .with(uk.gov.moj.cpp.hearing.domain.HearingType::getDescription, is(initiateHearingCommand.getHearing().getType().getDescription())))
                .with(HearingEventLogged::getRecordedLabel, is(logEventCommand.getRecordedLabel()))
                .with(HearingEventLogged::getEventTime, is(logEventCommand.getEventTime().withZoneSameLocal(ZoneId.of("UTC"))))
                .with(HearingEventLogged::isAlterable, is(logEventCommand.getAlterable()))
                .with(HearingEventLogged::getLastModifiedTime, is(logEventCommand.getLastModifiedTime().withZoneSameLocal(ZoneId.of("UTC"))))
                .with(HearingEventLogged::getCourtCentre, isBean(uk.gov.moj.cpp.hearing.domain.CourtCentre.class)
                        .with(uk.gov.moj.cpp.hearing.domain.CourtCentre::getId, is(initiateHearingCommand.getHearing().getCourtCentre().getId()))
                        .with(uk.gov.moj.cpp.hearing.domain.CourtCentre::getName, is(initiateHearingCommand.getHearing().getCourtCentre().getName()))
                        .with(uk.gov.moj.cpp.hearing.domain.CourtCentre::getRoomId, is(initiateHearingCommand.getHearing().getCourtCentre().getRoomId()))
                        .with(uk.gov.moj.cpp.hearing.domain.CourtCentre::getRoomName, is(initiateHearingCommand.getHearing().getCourtCentre().getRoomName()))
                        .with(uk.gov.moj.cpp.hearing.domain.CourtCentre::getWelshName, is(initiateHearingCommand.getHearing().getCourtCentre().getWelshName()))
                        .with(uk.gov.moj.cpp.hearing.domain.CourtCentre::getWelshRoomName, is(initiateHearingCommand.getHearing().getCourtCentre().getWelshRoomName())))
                .with(HearingEventLogged::getCaseURN, is(initiateHearingCommand.getHearing().getProsecutionCases().get(0).getProsecutionCaseIdentifier().getCaseURN()))
        );
    }

    @Test
    public void logHearingEvent_shouldIgnoreLogEvent_givenEventHasAlreadyBeenLogged() throws Exception {

        final InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplate();
        final UUID hearingId = initiateHearingCommand.getHearing().getId();

        final LogEventCommand logEventCommand = new LogEventCommand(randomUUID(), hearingId,
                        randomUUID(), STRING.next(), PAST_ZONED_DATE_TIME.next(), PAST_ZONED_DATE_TIME.next(), false, null);

        setupMockedEventStream(hearingId, this.eventStream, with(new HearingAggregate(), a -> {
            a.apply(new HearingInitiated(initiateHearingCommand.getHearing()));
            a.apply(new HearingEventLogged(
                    logEventCommand.getHearingEventId(),
                    null,
                    logEventCommand.getHearingId(),
                    logEventCommand.getHearingEventDefinitionId(),
                    logEventCommand.getDefenceCounselId(),
                    logEventCommand.getRecordedLabel(),
                    logEventCommand.getEventTime(),
                    logEventCommand.getLastModifiedTime(),
                    logEventCommand.getAlterable(),
                    CourtCentre.courtCentre()
                        .withId(initiateHearingCommand.getHearing().getCourtCentre().getId())
                        .withName(initiateHearingCommand.getHearing().getCourtCentre().getName())
                        .withRoomId(initiateHearingCommand.getHearing().getCourtCentre().getRoomId())
                        .withRoomName(initiateHearingCommand.getHearing().getCourtCentre().getRoomName())
                        .withWelshName(initiateHearingCommand.getHearing().getCourtCentre().getWelshName())
                        .withWelshRoomName(initiateHearingCommand.getHearing().getCourtCentre().getWelshRoomName())
                        .build(),
                    HearingType.hearingType()
                        .withId(initiateHearingCommand.getHearing().getType().getId())
                        .withDescription(initiateHearingCommand.getHearing().getType().getDescription())
                        .build(),
                    initiateHearingCommand.getHearing().getProsecutionCases().get(0).getProsecutionCaseIdentifier().getCaseURN())); //TODO: GPE-5657 Which case URN is expected to be set?
        }));

        final JsonEnvelope jsonEnvelopCommand = envelopeFrom(metadataWithRandomUUID("hearing.log-hearing-event"), objectToJsonObjectConverter.convert(logEventCommand));

        hearingEventCommandHandler.logHearingEvent(jsonEnvelopCommand);

        final JsonEnvelope jsonEnvelopeEvent = verifyAppendAndGetArgumentFrom(eventStream).findFirst().get();

        assertThat(jsonEnvelopeEvent, jsonEnvelope(metadata().withName("hearing.hearing-event-ignored"), payloadIsJson(print())));

        assertThat(uk.gov.moj.cpp.hearing.test.ObjectConverters.asPojo(jsonEnvelopeEvent, HearingEventIgnored.class), isBean(HearingEventIgnored.class)
                .with(HearingEventIgnored::getHearingEventDefinitionId, is(logEventCommand.getHearingEventDefinitionId()))
                .with(HearingEventIgnored::getHearingEventId, is(logEventCommand.getHearingEventId()))
                .with(HearingEventIgnored::getHearingId, is(logEventCommand.getHearingId()))
                .with(HearingEventIgnored::getRecordedLabel, is(logEventCommand.getRecordedLabel()))
                .with(HearingEventIgnored::getEventTime, is(logEventCommand.getEventTime().withZoneSameLocal(ZoneId.of("UTC"))))
                .with(HearingEventIgnored::isAlterable, is(logEventCommand.getAlterable()))
        );
    }

    @Test
    public void correctHearingEvent_shouldDeleteOldEventAndAddANewEvent() throws Exception {

        final InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplate();
        final UUID hearingId = initiateHearingCommand.getHearing().getId();

        final LogEventCommand logEventCommand = new LogEventCommand(randomUUID(), hearingId,
                        randomUUID(), STRING.next(), PAST_ZONED_DATE_TIME.next(), PAST_ZONED_DATE_TIME.next(), false, null);

        final CorrectLogEventCommand correctLogEvenCommand = new CorrectLogEventCommand(logEventCommand.getHearingEventId(), randomUUID(), hearingId,
                        randomUUID(), STRING.next(), PAST_ZONED_DATE_TIME.next(), PAST_ZONED_DATE_TIME.next(), false, randomUUID());

        setupMockedEventStream(hearingId, this.eventStream, with(new HearingAggregate(), a -> {
            a.apply(new HearingInitiated(initiateHearingCommand.getHearing()));
            a.apply(new HearingEventLogged(
                    logEventCommand.getHearingEventId(),
                    null,
                    logEventCommand.getHearingId(),
                    logEventCommand.getHearingEventDefinitionId(),
                    logEventCommand.getDefenceCounselId(),
                    logEventCommand.getRecordedLabel(),
                    logEventCommand.getEventTime(),
                    logEventCommand.getLastModifiedTime(),
                    logEventCommand.getAlterable(),
                    CourtCentre.courtCentre()
                        .withId(initiateHearingCommand.getHearing().getCourtCentre().getId())
                        .withName(initiateHearingCommand.getHearing().getCourtCentre().getName())
                        .withRoomId(initiateHearingCommand.getHearing().getCourtCentre().getRoomId())
                        .withRoomName(initiateHearingCommand.getHearing().getCourtCentre().getRoomName())
                        .withWelshName(initiateHearingCommand.getHearing().getCourtCentre().getWelshName())
                        .withWelshRoomName(initiateHearingCommand.getHearing().getCourtCentre().getWelshRoomName())
                        .build(),
                    HearingType.hearingType()
                        .withId(initiateHearingCommand.getHearing().getType().getId())
                        .withDescription(initiateHearingCommand.getHearing().getType().getDescription())
                        .build(),
                    initiateHearingCommand.getHearing().getProsecutionCases().get(0).getProsecutionCaseIdentifier().getCaseURN())); //TODO: GPE-5657 Which case URN is expected to be set?
        }));

        final JsonEnvelope command = envelopeFrom(metadataWithRandomUUID("hearing.command.correct-hearing-event"), objectToJsonObjectConverter.convert(correctLogEvenCommand));

        hearingEventCommandHandler.correctEvent(command);

        final List<JsonEnvelope> jsonEnvelopeEvents = verifyAppendAndGetArgumentFrom(eventStream).collect(Collectors.toList());

        assertThat(jsonEnvelopeEvents.get(0), jsonEnvelope(metadata().withName("hearing.hearing-event-deleted"), payloadIsJson(print())));

        assertThat(uk.gov.moj.cpp.hearing.test.ObjectConverters.asPojo(jsonEnvelopeEvents.get(0), HearingEventDeleted.class), isBean(HearingEventDeleted.class)
                .with(HearingEventDeleted::getHearingEventId, is(logEventCommand.getHearingEventId()))
        );

        assertThat(jsonEnvelopeEvents.get(1), jsonEnvelope(metadata().withName("hearing.hearing-event-logged"), payloadIsJson(print())));

        assertThat(uk.gov.moj.cpp.hearing.test.ObjectConverters.asPojo(jsonEnvelopeEvents.get(1), HearingEventLogged.class), isBean(HearingEventLogged.class)
                .with(HearingEventLogged::getHearingEventDefinitionId, is(correctLogEvenCommand.getHearingEventDefinitionId()))
                .with(HearingEventLogged::getHearingEventId, is(correctLogEvenCommand.getLatestHearingEventId()))
                .with(HearingEventLogged::getLastHearingEventId, is(correctLogEvenCommand.getHearingEventId()))
                .with(HearingEventLogged::getDefenceCounselId, is(correctLogEvenCommand.getDefenceCounselId()))
                .with(HearingEventLogged::getHearingId, is(correctLogEvenCommand.getHearingId()))
                .with(HearingEventLogged::getHearingType, isBean(uk.gov.moj.cpp.hearing.domain.HearingType.class)
                        .with(uk.gov.moj.cpp.hearing.domain.HearingType::getId, is(initiateHearingCommand.getHearing().getType().getId()))
                        .with(uk.gov.moj.cpp.hearing.domain.HearingType::getDescription, is(initiateHearingCommand.getHearing().getType().getDescription())))
                .with(HearingEventLogged::getRecordedLabel, is(correctLogEvenCommand.getRecordedLabel()))
                .with(HearingEventLogged::getEventTime, is(correctLogEvenCommand.getEventTime().withZoneSameLocal(ZoneId.of("UTC"))))
                .with(HearingEventLogged::isAlterable, is(correctLogEvenCommand.getAlterable()))
                .with(HearingEventLogged::getLastModifiedTime, is(correctLogEvenCommand.getLastModifiedTime().withZoneSameLocal(ZoneId.of("UTC"))))
                .with(HearingEventLogged::getCourtCentre, isBean(uk.gov.moj.cpp.hearing.domain.CourtCentre.class)
                        .with(uk.gov.moj.cpp.hearing.domain.CourtCentre::getId, is(initiateHearingCommand.getHearing().getCourtCentre().getId()))
                        .with(uk.gov.moj.cpp.hearing.domain.CourtCentre::getName, is(initiateHearingCommand.getHearing().getCourtCentre().getName()))
                        .with(uk.gov.moj.cpp.hearing.domain.CourtCentre::getRoomId, is(initiateHearingCommand.getHearing().getCourtCentre().getRoomId()))
                        .with(uk.gov.moj.cpp.hearing.domain.CourtCentre::getRoomName, is(initiateHearingCommand.getHearing().getCourtCentre().getRoomName()))
                        .with(uk.gov.moj.cpp.hearing.domain.CourtCentre::getWelshName, is(initiateHearingCommand.getHearing().getCourtCentre().getWelshName()))
                        .with(uk.gov.moj.cpp.hearing.domain.CourtCentre::getWelshRoomName, is(initiateHearingCommand.getHearing().getCourtCentre().getWelshRoomName())))
                .with(HearingEventLogged::getCaseURN, is(initiateHearingCommand.getHearing().getProsecutionCases().get(0).getProsecutionCaseIdentifier().getCaseURN()))
        );
    }

    @Test
    public void correctHearingEvent_shouldIgnoreCorrection_givenNoPreviousEventFound() throws Exception {

        final InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplate();
        final UUID hearingId = initiateHearingCommand.getHearing().getId();

        final CorrectLogEventCommand correctLogEventCommand = new CorrectLogEventCommand(randomUUID(), randomUUID(), hearingId,
                        randomUUID(), STRING.next(), PAST_ZONED_DATE_TIME.next(), PAST_ZONED_DATE_TIME.next(), false, randomUUID());

        setupMockedEventStream(hearingId, this.eventStream, with(new HearingAggregate(), a -> {
            a.apply(new HearingInitiated(initiateHearingCommand.getHearing()));
        }));

        final JsonEnvelope jsonEnvelopeCommand = envelopeFrom(metadataWithRandomUUID("hearing.command.correct-hearing-event"),
                objectToJsonObjectConverter.convert(correctLogEventCommand));

        hearingEventCommandHandler.correctEvent(jsonEnvelopeCommand);

        final JsonEnvelope jsonEnvelopeEvent = verifyAppendAndGetArgumentFrom(eventStream).findFirst().get();

        assertThat(jsonEnvelopeEvent, jsonEnvelope(metadata().withName("hearing.hearing-event-ignored"), payloadIsJson(print())));

        assertThat(uk.gov.moj.cpp.hearing.test.ObjectConverters.asPojo(jsonEnvelopeEvent, HearingEventIgnored.class), isBean(HearingEventIgnored.class)
                .with(HearingEventIgnored::getHearingEventDefinitionId, is(correctLogEventCommand.getHearingEventDefinitionId()))
                .with(HearingEventIgnored::getHearingEventId, is(correctLogEventCommand.getHearingEventId()))
                .with(HearingEventIgnored::getHearingId, is(correctLogEventCommand.getHearingId()))
                .with(HearingEventIgnored::getRecordedLabel, is(correctLogEventCommand.getRecordedLabel()))
                .with(HearingEventIgnored::getEventTime, is(correctLogEventCommand.getEventTime().withZoneSameLocal(ZoneId.of("UTC"))))
                .with(HearingEventIgnored::isAlterable, is(correctLogEventCommand.getAlterable()))
        );
    }

    private <T extends Aggregate> void setupMockedEventStream(final UUID id, final EventStream eventStream, final T aggregate) {
        when(this.eventSource.getStreamById(id)).thenReturn(eventStream);
        final Class<T> clz = (Class<T>) aggregate.getClass();
        when(this.aggregateService.get(eventStream, clz)).thenReturn(aggregate);
    }
}
