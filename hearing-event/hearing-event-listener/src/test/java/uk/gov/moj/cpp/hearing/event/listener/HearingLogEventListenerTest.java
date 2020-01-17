package uk.gov.moj.cpp.hearing.event.listener;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.BOOLEAN;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.INTEGER;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_ZONED_DATE_TIME;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.HearingEventDefinitionsTemplates.buildCreateHearingEventDefinitionsCommand;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.logEvent.CreateHearingEventDefinitionsCommand;
import uk.gov.moj.cpp.hearing.command.logEvent.LogEventCommand;
import uk.gov.moj.cpp.hearing.command.updateEvent.UpdateHearingEventsCommand;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventDeleted;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingEvent;
import uk.gov.moj.cpp.hearing.persist.entity.heda.HearingEventDefinition;
import uk.gov.moj.cpp.hearing.repository.HearingEventDefinitionRepository;
import uk.gov.moj.cpp.hearing.repository.HearingEventRepository;

import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class HearingLogEventListenerTest {

    @Mock
    private HearingEventRepository hearingEventRepository;

    @Mock
    private HearingEventDefinitionRepository hearingEventDefinitionRepository;

    @Captor
    private ArgumentCaptor<HearingEvent> eventLogArgumentCaptor;

    @Captor
    private ArgumentCaptor<HearingEventDefinition> hearingEventDefinitionArgumentCaptor;

    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @InjectMocks
    private HearingLogEventListener hearingLogEventListener;

    @Before
    public void setup() {
        setField(this.jsonObjectToObjectConverter, "objectMapper", new ObjectMapperProducer().objectMapper());
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
    }

    @Test
    public void shouldPersistAHearingEventLog() {

        final LogEventCommand logEventCommand = new LogEventCommand(randomUUID(), randomUUID(), randomUUID(), STRING.next(),
                PAST_ZONED_DATE_TIME.next(), PAST_ZONED_DATE_TIME.next(), BOOLEAN.next(), randomUUID());

        final JsonEnvelope jsonEnvelopCommand = envelopeFrom(metadataWithRandomUUID("hearing.log-hearing-event"), objectToJsonObjectConverter.convert(logEventCommand));

        hearingLogEventListener.hearingEventLogged(jsonEnvelopCommand);

        verify(hearingEventRepository).save(eventLogArgumentCaptor.capture());

        assertThat(eventLogArgumentCaptor.getValue().getId(), is(logEventCommand.getHearingEventId()));
        assertThat(eventLogArgumentCaptor.getValue().getHearingEventDefinitionId(), is(logEventCommand.getHearingEventDefinitionId()));
        assertThat(eventLogArgumentCaptor.getValue().isAlterable(), is(logEventCommand.getAlterable()));
        assertThat(eventLogArgumentCaptor.getValue().getHearingId(), is(logEventCommand.getHearingId()));
        assertThat(eventLogArgumentCaptor.getValue().getRecordedLabel(), is(logEventCommand.getRecordedLabel()));
        assertThat(eventLogArgumentCaptor.getValue().getEventDate(), is(logEventCommand.getEventTime().toLocalDate()));
        assertThat(eventLogArgumentCaptor.getValue().getEventTime(), is(logEventCommand.getEventTime().toLocalDateTime().atZone(ZoneId.of("UTC"))));
    }

    @Test
    public void shouldUpdateHearingEvents() {

        final UpdateHearingEventsCommand updateHearingEventsCommand = UpdateHearingEventsCommand.builder()
                .withHearingId(randomUUID())
                .withHearingEvents(Arrays.asList(uk.gov.moj.cpp.hearing.command.updateEvent.HearingEvent.builder()
                        .withHearingEventId(randomUUID())
                        .withRecordedLabel(STRING.next())
                        .build()))
                .build();

        final HearingEvent hearingEvent = HearingEvent.hearingEvent()
                .setId(updateHearingEventsCommand.getHearingEvents().get(0).getHearingEventId())
                .setHearingId(updateHearingEventsCommand.getHearingId())
                .setRecordedLabel(updateHearingEventsCommand.getHearingEvents().get(0).getRecordedLabel());

        when(hearingEventRepository.findByHearingIdOrderByEventTimeAsc(hearingEvent.getHearingId())).thenReturn(Arrays.asList(hearingEvent));

        final JsonEnvelope jsonEnvelopEvent = envelopeFrom(metadataWithRandomUUID("hearing.hearing-events-updated"), objectToJsonObjectConverter.convert(updateHearingEventsCommand));

        hearingLogEventListener.hearingEventsUpdated(jsonEnvelopEvent);

        verify(hearingEventRepository).save(eventLogArgumentCaptor.capture());

        assertThat(eventLogArgumentCaptor.getValue().getId(), is(updateHearingEventsCommand.getHearingEvents().get(0).getHearingEventId()));
        assertThat(eventLogArgumentCaptor.getValue().getHearingId(), is(updateHearingEventsCommand.getHearingId()));
        assertThat(eventLogArgumentCaptor.getValue().getRecordedLabel(), is(updateHearingEventsCommand.getHearingEvents().get(0).getRecordedLabel()));
    }

    @Test
    public void shouldDeleteAnExistingHearingEvent() {

        final LogEventCommand logEventCommand = new LogEventCommand(randomUUID(), randomUUID(), randomUUID(), STRING.next(),
                PAST_ZONED_DATE_TIME.next(), PAST_ZONED_DATE_TIME.next(), BOOLEAN.next(), randomUUID());

        when(hearingEventRepository.findOptionalById(logEventCommand.getHearingId())).thenReturn(
                of(HearingEvent.hearingEvent()
                        .setId(logEventCommand.getHearingEventId())
                        .setHearingEventDefinitionId(logEventCommand.getHearingEventDefinitionId())
                        .setHearingId(logEventCommand.getHearingId())
                        .setRecordedLabel(logEventCommand.getRecordedLabel())
                        .setEventTime(logEventCommand.getEventTime())
                        .setLastModifiedTime(logEventCommand.getLastModifiedTime())
                        .setAlterable(logEventCommand.getAlterable()))
        );

        final JsonEnvelope jsonEnvelopEvent = envelopeFrom(metadataWithRandomUUID("hearing.hearing-event-deleted"),
                objectToJsonObjectConverter.convert(new HearingEventDeleted(logEventCommand.getHearingId())));

        hearingLogEventListener.hearingEventDeleted(jsonEnvelopEvent);

        verify(hearingEventRepository).save(eventLogArgumentCaptor.capture());

        assertThat(eventLogArgumentCaptor.getValue().getId(), is(logEventCommand.getHearingEventId()));
        assertThat(eventLogArgumentCaptor.getValue().getHearingId(), is(logEventCommand.getHearingId()));
        assertThat(eventLogArgumentCaptor.getValue().getHearingEventDefinitionId(), is(logEventCommand.getHearingEventDefinitionId()));
        assertThat(eventLogArgumentCaptor.getValue().getRecordedLabel(), is(logEventCommand.getRecordedLabel()));
        assertThat(eventLogArgumentCaptor.getValue().getEventTime(), is(logEventCommand.getEventTime()));
        assertThat(eventLogArgumentCaptor.getValue().getLastModifiedTime(), is(logEventCommand.getLastModifiedTime()));
        assertThat(eventLogArgumentCaptor.getValue().isDeleted(), is(true));
    }

    @Test
    public void shouldIgnoreDeletionIfHearingEventDoesNotExist() {

        final UUID hearingEventId = randomUUID();

        when(hearingEventRepository.findOptionalById(hearingEventId)).thenReturn(empty());

        final JsonEnvelope jsonEnvelopEvent = envelopeFrom(metadataWithRandomUUID("hearing.hearing-event-deleted"),
                objectToJsonObjectConverter.convert(new HearingEventDeleted(hearingEventId)));

        hearingLogEventListener.hearingEventDeleted(jsonEnvelopEvent);

        verify(hearingEventRepository, never()).save(any(HearingEvent.class));
    }

    @Test
    public void shouldCreateHearingEventDefinitions() {

        final CreateHearingEventDefinitionsCommand createHearingEventDefinitionsCommand = buildCreateHearingEventDefinitionsCommand();

        final JsonEnvelope jsonEnvelopEvent = envelopeFrom(metadataWithRandomUUID("hearing.hearing-event-definitions-created"),
                objectToJsonObjectConverter.convert(createHearingEventDefinitionsCommand));

        hearingLogEventListener.hearingEventDefinitionsCreated(jsonEnvelopEvent);

        verify(hearingEventDefinitionRepository, times(3)).save(hearingEventDefinitionArgumentCaptor.capture());

        final List<HearingEventDefinition> actualEntities = hearingEventDefinitionArgumentCaptor.getAllValues();
        assertThat(actualEntities.get(0).getId(), is(createHearingEventDefinitionsCommand.getEventDefinitions().get(0).getId()));
        assertThat(actualEntities.get(0).getActionLabel(), is(createHearingEventDefinitionsCommand.getEventDefinitions().get(0).getActionLabel()));
        assertThat(actualEntities.get(0).getRecordedLabel(), is(createHearingEventDefinitionsCommand.getEventDefinitions().get(0).getRecordedLabel()));
        assertThat(actualEntities.get(0).getActionSequence(), is(createHearingEventDefinitionsCommand.getEventDefinitions().get(0).getActionSequence()));
        assertThat(actualEntities.get(0).getGroupSequence(), is(createHearingEventDefinitionsCommand.getEventDefinitions().get(0).getGroupSequence()));
        assertThat(actualEntities.get(0).getCaseAttribute(), is(nullValue()));
        assertThat(actualEntities.get(0).getGroupLabel(), is(createHearingEventDefinitionsCommand.getEventDefinitions().get(0).getGroupLabel()));
        assertThat(actualEntities.get(0).isAlterable(), is(createHearingEventDefinitionsCommand.getEventDefinitions().get(0).isAlterable()));
        assertThat(actualEntities.get(0).isDeleted(), is(false));

        assertThat(actualEntities.get(1).getId(), is(createHearingEventDefinitionsCommand.getEventDefinitions().get(1).getId()));
        assertThat(actualEntities.get(1).getActionLabel(), is(createHearingEventDefinitionsCommand.getEventDefinitions().get(1).getActionLabel()));
        assertThat(actualEntities.get(1).getRecordedLabel(), is(createHearingEventDefinitionsCommand.getEventDefinitions().get(1).getRecordedLabel()));
        assertThat(actualEntities.get(1).getActionSequence(), is(createHearingEventDefinitionsCommand.getEventDefinitions().get(1).getActionSequence()));
        assertThat(actualEntities.get(1).getGroupSequence(), is(createHearingEventDefinitionsCommand.getEventDefinitions().get(1).getGroupSequence()));
        assertThat(actualEntities.get(1).getCaseAttribute(), is(createHearingEventDefinitionsCommand.getEventDefinitions().get(1).getCaseAttribute()));
        assertThat(actualEntities.get(1).getGroupLabel(), is(createHearingEventDefinitionsCommand.getEventDefinitions().get(1).getGroupLabel()));
        assertThat(actualEntities.get(1).isAlterable(), is(createHearingEventDefinitionsCommand.getEventDefinitions().get(1).isAlterable()));
        assertThat(actualEntities.get(1).isDeleted(), is(false));

        assertThat(actualEntities.get(2).getId(), is(createHearingEventDefinitionsCommand.getEventDefinitions().get(2).getId()));
        assertThat(actualEntities.get(2).getActionLabel(), is(createHearingEventDefinitionsCommand.getEventDefinitions().get(2).getActionLabel()));
        assertThat(actualEntities.get(2).getRecordedLabel(), is(createHearingEventDefinitionsCommand.getEventDefinitions().get(2).getRecordedLabel()));
        assertThat(actualEntities.get(2).getActionSequence(), is(createHearingEventDefinitionsCommand.getEventDefinitions().get(2).getActionSequence()));
        assertThat(actualEntities.get(2).getGroupSequence(), is(createHearingEventDefinitionsCommand.getEventDefinitions().get(2).getGroupSequence()));
        assertThat(actualEntities.get(2).getCaseAttribute(), is(nullValue()));
        assertThat(actualEntities.get(2).getGroupLabel(), is(createHearingEventDefinitionsCommand.getEventDefinitions().get(2).getGroupLabel()));
        assertThat(actualEntities.get(2).isAlterable(), is(createHearingEventDefinitionsCommand.getEventDefinitions().get(2).isAlterable()));
        assertThat(actualEntities.get(2).isDeleted(), is(false));

        verifyNoMoreInteractions(hearingEventDefinitionRepository);
    }

    @Test
    public void shouldMarkAllActiveHearingEventDefinitionsAsDeleted() {

        final List<HearingEventDefinition> hearingEventDefinitions = Arrays.asList(
                new HearingEventDefinition(randomUUID(), STRING.next(), STRING.next(), INTEGER.next(), STRING.next(), STRING.next(), INTEGER.next(), BOOLEAN.next()),
                new HearingEventDefinition(randomUUID(), STRING.next(), STRING.next(), INTEGER.next(), STRING.next(), STRING.next(), INTEGER.next(), BOOLEAN.next()),
                new HearingEventDefinition(randomUUID(), STRING.next(), STRING.next(), INTEGER.next(), STRING.next(), STRING.next(), INTEGER.next(), BOOLEAN.next()));

        when(hearingEventDefinitionRepository.findAllActive()).thenReturn(hearingEventDefinitions);

        final JsonEnvelope jsonEnvelopEvent = envelopeFrom(metadataWithRandomUUID("hearing.hearing-event-definitions-deleted"), createObjectBuilder().build());

        hearingLogEventListener.hearingEventDefinitionsDeleted(jsonEnvelopEvent);

        final InOrder inOrder = inOrder(hearingEventDefinitionRepository);

        inOrder.verify(hearingEventDefinitionRepository).findAllActive();
        inOrder.verify(hearingEventDefinitionRepository, times(3)).save(hearingEventDefinitionArgumentCaptor.capture());

        final List<HearingEventDefinition> actualEntities = hearingEventDefinitionArgumentCaptor.getAllValues();
        assertThat(actualEntities.get(0).getId(), is(hearingEventDefinitions.get(0).getId()));
        assertThat(actualEntities.get(0).getActionLabel(), is(hearingEventDefinitions.get(0).getActionLabel()));
        assertThat(actualEntities.get(0).getRecordedLabel(), is(hearingEventDefinitions.get(0).getRecordedLabel()));
        assertThat(actualEntities.get(0).getActionSequence(), is(hearingEventDefinitions.get(0).getActionSequence()));
        assertThat(actualEntities.get(0).getGroupSequence(), is(hearingEventDefinitions.get(0).getGroupSequence()));
        assertThat(actualEntities.get(0).getCaseAttribute(), is(hearingEventDefinitions.get(0).getCaseAttribute()));
        assertThat(actualEntities.get(0).getGroupLabel(), is(hearingEventDefinitions.get(0).getGroupLabel()));
        assertThat(actualEntities.get(0).isAlterable(), is(hearingEventDefinitions.get(0).isAlterable()));
        assertThat(actualEntities.get(0).isDeleted(), is(true));

        assertThat(actualEntities.get(1).getId(), is(hearingEventDefinitions.get(1).getId()));
        assertThat(actualEntities.get(1).getActionLabel(), is(hearingEventDefinitions.get(1).getActionLabel()));
        assertThat(actualEntities.get(1).getRecordedLabel(), is(hearingEventDefinitions.get(1).getRecordedLabel()));
        assertThat(actualEntities.get(1).getActionSequence(), is(hearingEventDefinitions.get(1).getActionSequence()));
        assertThat(actualEntities.get(1).getGroupSequence(), is(hearingEventDefinitions.get(1).getGroupSequence()));
        assertThat(actualEntities.get(1).getCaseAttribute(), is(hearingEventDefinitions.get(1).getCaseAttribute()));
        assertThat(actualEntities.get(1).getGroupLabel(), is(hearingEventDefinitions.get(1).getGroupLabel()));
        assertThat(actualEntities.get(1).isAlterable(), is(hearingEventDefinitions.get(1).isAlterable()));
        assertThat(actualEntities.get(1).isDeleted(), is(true));

        assertThat(actualEntities.get(2).getId(), is(hearingEventDefinitions.get(2).getId()));
        assertThat(actualEntities.get(2).getActionLabel(), is(hearingEventDefinitions.get(2).getActionLabel()));
        assertThat(actualEntities.get(2).getRecordedLabel(), is(hearingEventDefinitions.get(2).getRecordedLabel()));
        assertThat(actualEntities.get(2).getActionSequence(), is(hearingEventDefinitions.get(2).getActionSequence()));
        assertThat(actualEntities.get(2).getGroupSequence(), is(hearingEventDefinitions.get(2).getGroupSequence()));
        assertThat(actualEntities.get(2).getCaseAttribute(), is(hearingEventDefinitions.get(2).getCaseAttribute()));
        assertThat(actualEntities.get(2).getGroupLabel(), is(hearingEventDefinitions.get(2).getGroupLabel()));
        assertThat(actualEntities.get(2).isAlterable(), is(hearingEventDefinitions.get(2).isAlterable()));
        assertThat(actualEntities.get(2).isDeleted(), is(true));

        verifyNoMoreInteractions(hearingEventDefinitionRepository);
    }
}