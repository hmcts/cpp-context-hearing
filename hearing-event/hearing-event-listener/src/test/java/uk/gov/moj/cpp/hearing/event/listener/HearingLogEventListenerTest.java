package uk.gov.moj.cpp.hearing.event.listener;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createArrayBuilder;
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
import static uk.gov.justice.services.test.utils.core.messaging.JsonEnvelopeBuilder.envelope;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUIDAndName;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.BOOLEAN;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_ZONED_DATE_TIME;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;

import uk.gov.justice.services.common.converter.ZonedDateTimes;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingEvent;
import uk.gov.moj.cpp.hearing.persist.entity.heda.HearingEventDefinition;
import uk.gov.moj.cpp.hearing.repository.HearingEventDefinitionRepository;
import uk.gov.moj.cpp.hearing.repository.HearingEventRepository;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.json.JsonArrayBuilder;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class HearingLogEventListenerTest {

    private static final String FIELD_HEARING_EVENT_ID = "hearingEventId";
    private static final String FIELD_HEARING_EVENT_DEFINITION_ID = "hearingEventDefinitionId";
    private static final String FIELD_RECORDED_LABEL = "recordedLabel";
    private static final String FIELD_HEARING_ID = "hearingId";
    private static final String FIELD_WITNESS_ID = "witnessId";
    private static final String FIELD_EVENT_TIME = "eventTime";
    private static final String FIELD_LAST_MODIFIED_TIME = "lastModifiedTime";

    private static final String FIELD_GENERIC_ID = "id";
    private static final String FIELD_ACTION_LABEL = "actionLabel";
    private static final String FIELD_SEQUENCE_NUMBER = "sequence";
    private static final String FIELD_CASE_ATTRIBUTE = "caseAttribute";
    private static final String FIELD_EVENT_DEFINITIONS = "eventDefinitions";
    private static final String FIELD_SEQUENCE_TYPE = "sequenceType";
    private static final String FIELD_GROUP_LABEL = "groupLabel";
    private static final String FIELD_ACTION_LABEL_EXTENSION = "actionLabelExtension";
    private static final String FIELD_ALTERABLE = "alterable";

    private static final UUID HEARING_EVENT_ID = randomUUID();
    private static final UUID HEARING_ID = randomUUID();
    private static final UUID WITNESS_ID = randomUUID();
    private static final String RECORDED_LABEL = STRING.next();
    private static final ZonedDateTime EVENT_TIME = PAST_ZONED_DATE_TIME.next();
    private static final ZonedDateTime LAST_MODIFIED_TIME = PAST_ZONED_DATE_TIME.next();

    private static final UUID HEARING_EVENT_DEFINITION_ID = randomUUID();
    private static final String ACTION_LABEL = STRING.next();
    private static final int SEQUENCE_NUMBER = 1;
    private static final boolean ALTERABLE = BOOLEAN.next();
    private static final UUID HEARING_EVENT_DEFINITION_ID_2 = randomUUID();
    private static final String ACTION_LABEL_2 = STRING.next();
    private static final String RECORDED_LABEL_2 = STRING.next();
    private static final int SEQUENCE_NUMBER_2 = 2;
    private static final String CASE_ATTRIBUTE = STRING.next();
    private static final boolean ALTERABLE_2 = BOOLEAN.next();
    private static final UUID HEARING_EVENT_DEFINITION_ID_3 = randomUUID();
    private static final String ACTION_LABEL_3 = STRING.next();
    private static final String RECORDED_LABEL_3 = STRING.next();
    private static final boolean ALTERABLE_3 = BOOLEAN.next();

    private static final UUID HEARING_EVENT_DEFINITIONS_ID = randomUUID();
    private static final String SEQUENCE_TYPE = STRING.next();
    private static final String GROUP_LABEL = STRING.next();
    private static final String ACTION_LABEL_EXTENSION = STRING.next();
    private static final String FIELD_HEARING_EVENTS = "hearingEvents";


    @Mock
    private HearingEventRepository hearingEventRepository;

    @Mock
    private HearingEventDefinitionRepository hearingEventDefinitionRepository;

    @Captor
    private ArgumentCaptor<HearingEvent> eventLogArgumentCaptor;

    @Captor
    private ArgumentCaptor<HearingEventDefinition> hearingEventDefinitionArgumentCaptor;

    @InjectMocks
    private HearingLogEventListener hearingLogEventListener;

    @Test
    public void shouldPersistAHearingEventLog() {
        final JsonEnvelope event = prepareHearingEventLoggedEvent();

        hearingLogEventListener.hearingEventLogged(event);

        verify(hearingEventRepository).save(eventLogArgumentCaptor.capture());
        assertThat(eventLogArgumentCaptor.getValue().getId(), is(HEARING_EVENT_ID));
        assertThat(eventLogArgumentCaptor.getValue().getHearingEventDefinitionId(), is(HEARING_EVENT_DEFINITION_ID));
        assertThat(eventLogArgumentCaptor.getValue().isAlterable(), is(ALTERABLE));
        assertThat(eventLogArgumentCaptor.getValue().getHearingId(), is(HEARING_ID));
        assertThat(eventLogArgumentCaptor.getValue().getRecordedLabel(), is(RECORDED_LABEL));
        assertThat(eventLogArgumentCaptor.getValue().getEventTime().toString(), is(ZonedDateTimes.toString(EVENT_TIME)));
    }

    @Test
    public void shouldPersistAHearingEventLogWithWitness() {
        final JsonEnvelope event = prepareHearingEventLoggedEventWithWitness();

        hearingLogEventListener.hearingEventLogged(event);

        verify(hearingEventRepository).save(eventLogArgumentCaptor.capture());
        assertThat(eventLogArgumentCaptor.getValue().getId(), is(HEARING_EVENT_ID));
        assertThat(eventLogArgumentCaptor.getValue().getHearingEventDefinitionId(), is(HEARING_EVENT_DEFINITION_ID));
        assertThat(eventLogArgumentCaptor.getValue().isAlterable(), is(ALTERABLE));
        assertThat(eventLogArgumentCaptor.getValue().getHearingId(), is(HEARING_ID));
        assertThat(eventLogArgumentCaptor.getValue().getWitnessId(), is(WITNESS_ID));
        assertThat(eventLogArgumentCaptor.getValue().getRecordedLabel(), is(RECORDED_LABEL));
        assertThat(eventLogArgumentCaptor.getValue().getEventTime().toString(), is(ZonedDateTimes.toString(EVENT_TIME)));
    }

    @Test
    public void shouldUpdateHearingEvents() {
        final HearingEvent repoEvent = new HearingEvent();
        repoEvent.setId(HEARING_EVENT_ID);
        repoEvent.setHearingId(HEARING_ID);
        final List<HearingEvent> repositoryHearingEvents =
                        Arrays.asList(repoEvent);
        final JsonEnvelope hearingEventsUpdated =
                        prepareHearingEventsUpdated();
        when(hearingEventRepository.findByHearingIdOrderByEventTimeAsc(HEARING_ID))
                        .thenReturn(repositoryHearingEvents);
        hearingLogEventListener.hearingEventsUpdated(hearingEventsUpdated);
        verify(hearingEventRepository).save(eventLogArgumentCaptor.capture());
        assertThat(eventLogArgumentCaptor.getValue().getId(), is(HEARING_EVENT_ID));
        assertThat(eventLogArgumentCaptor.getValue().getHearingId(), is(HEARING_ID));
        assertThat(eventLogArgumentCaptor.getValue().getRecordedLabel(), is(RECORDED_LABEL));
    }




    @Test
    public void shouldDeleteAnExistingHearingEvent() {
        final JsonEnvelope event = prepareHearingEventDeletedEvent();
        when(hearingEventRepository.findOptionalById(HEARING_EVENT_ID)).thenReturn(
                of( HearingEvent.hearingEvent()
                        .setId(HEARING_EVENT_ID)
                        .setHearingEventDefinitionId(HEARING_EVENT_DEFINITION_ID)
                        .setHearingId(HEARING_ID)
                        .setRecordedLabel(RECORDED_LABEL)
                        .setEventTime(EVENT_TIME)
                        .setLastModifiedTime(LAST_MODIFIED_TIME)
                        .setAlterable(ALTERABLE))
        );

        hearingLogEventListener.hearingEventDeleted(event);

        verify(hearingEventRepository).save(eventLogArgumentCaptor.capture());
        assertThat(eventLogArgumentCaptor.getValue().getId(), is(HEARING_EVENT_ID));
        assertThat(eventLogArgumentCaptor.getValue().getHearingId(), is(HEARING_ID));
        assertThat(eventLogArgumentCaptor.getValue().getHearingEventDefinitionId(), is(HEARING_EVENT_DEFINITION_ID));
        assertThat(eventLogArgumentCaptor.getValue().getRecordedLabel(), is(RECORDED_LABEL));
        assertThat(eventLogArgumentCaptor.getValue().getEventTime(), is(EVENT_TIME));
        assertThat(eventLogArgumentCaptor.getValue().getLastModifiedTime(), is(LAST_MODIFIED_TIME));
        assertThat(eventLogArgumentCaptor.getValue().isDeleted(), is(true));
    }

    @Test
    public void shouldIgnoreDeletionIfHearingEventDoesNotExist() {
        final JsonEnvelope event = prepareHearingEventDeletedEvent();
        when(hearingEventRepository.findOptionalById(HEARING_EVENT_ID)).thenReturn(empty());

        hearingLogEventListener.hearingEventDeleted(event);

        verify(hearingEventRepository, never()).save(any(HearingEvent.class));
    }

    @Test
    public void shouldCreateHearingEventDefinitions() {
        final JsonEnvelope event = prepareHearingEventDefinitionsCreatedEvent();

        hearingLogEventListener.hearingEventDefinitionsCreated(event);

        verify(hearingEventDefinitionRepository, times(3)).save(hearingEventDefinitionArgumentCaptor.capture());

        final List<HearingEventDefinition> actualEntities = hearingEventDefinitionArgumentCaptor.getAllValues();
        assertThat(actualEntities.get(0).getId(), is(HEARING_EVENT_DEFINITION_ID));
        assertThat(actualEntities.get(0).getActionLabel(), is(ACTION_LABEL));
        assertThat(actualEntities.get(0).getRecordedLabel(), is(RECORDED_LABEL));
        assertThat(actualEntities.get(0).getSequenceNumber(), is(SEQUENCE_NUMBER));
        assertThat(actualEntities.get(0).getSequenceType(), is(SEQUENCE_TYPE));
        assertThat(actualEntities.get(0).getCaseAttribute(), is(nullValue()));
        assertThat(actualEntities.get(0).getGroupLabel(), is(nullValue()));
        assertThat(actualEntities.get(0).getActionLabelExtension(), is(nullValue()));
        assertThat(actualEntities.get(0).isAlterable(), is(ALTERABLE));
        assertThat(actualEntities.get(0).isDeleted(), is(false));

        assertThat(actualEntities.get(1).getId(), is(HEARING_EVENT_DEFINITION_ID_2));
        assertThat(actualEntities.get(1).getActionLabel(), is(ACTION_LABEL_2));
        assertThat(actualEntities.get(1).getRecordedLabel(), is(RECORDED_LABEL_2));
        assertThat(actualEntities.get(1).getSequenceNumber(), is(SEQUENCE_NUMBER_2));
        assertThat(actualEntities.get(1).getSequenceType(), is(SEQUENCE_TYPE));
        assertThat(actualEntities.get(1).getCaseAttribute(), is(CASE_ATTRIBUTE));
        assertThat(actualEntities.get(1).getGroupLabel(), is(GROUP_LABEL));
        assertThat(actualEntities.get(1).getActionLabelExtension(), is(ACTION_LABEL_EXTENSION));
        assertThat(actualEntities.get(1).isAlterable(), is(ALTERABLE_2));
        assertThat(actualEntities.get(1).isDeleted(), is(false));

        assertThat(actualEntities.get(2).getId(), is(HEARING_EVENT_DEFINITION_ID_3));
        assertThat(actualEntities.get(2).getActionLabel(), is(ACTION_LABEL_3));
        assertThat(actualEntities.get(2).getRecordedLabel(), is(RECORDED_LABEL_3));
        assertThat(actualEntities.get(2).getSequenceNumber(), is(nullValue()));
        assertThat(actualEntities.get(2).getSequenceType(), is(nullValue()));
        assertThat(actualEntities.get(2).getCaseAttribute(), is(nullValue()));
        assertThat(actualEntities.get(2).getGroupLabel(), is(nullValue()));
        assertThat(actualEntities.get(2).getActionLabelExtension(), is(nullValue()));
        assertThat(actualEntities.get(2).isAlterable(), is(ALTERABLE_3));
        assertThat(actualEntities.get(2).isDeleted(), is(false));

        verifyNoMoreInteractions(hearingEventDefinitionRepository);
    }

    @Test
    public void shouldMarkAllActiveHearingEventDefinitionsAsDeleted() {
        when(hearingEventDefinitionRepository.findAllActive()).thenReturn(prepareActiveHearingEventDefinitions());

        hearingLogEventListener.hearingEventDefinitionsDeleted(prepareHearingEventDefinitionsDeletedEvent());

        final InOrder inOrder = inOrder(hearingEventDefinitionRepository);
        inOrder.verify(hearingEventDefinitionRepository).findAllActive();
        inOrder.verify(hearingEventDefinitionRepository, times(3)).save(hearingEventDefinitionArgumentCaptor.capture());

        final List<HearingEventDefinition> actualEntities = hearingEventDefinitionArgumentCaptor.getAllValues();
        assertThat(actualEntities.get(0).getId(), is(HEARING_EVENT_DEFINITION_ID));
        assertThat(actualEntities.get(0).getActionLabel(), is(ACTION_LABEL));
        assertThat(actualEntities.get(0).getRecordedLabel(), is(RECORDED_LABEL));
        assertThat(actualEntities.get(0).getSequenceNumber(), is(SEQUENCE_NUMBER));
        assertThat(actualEntities.get(0).getSequenceType(), is(SEQUENCE_TYPE));
        assertThat(actualEntities.get(0).getCaseAttribute(), is(nullValue()));
        assertThat(actualEntities.get(0).getGroupLabel(), is(nullValue()));
        assertThat(actualEntities.get(0).getActionLabelExtension(), is(nullValue()));
        assertThat(actualEntities.get(0).isAlterable(), is(ALTERABLE));
        assertThat(actualEntities.get(0).isDeleted(), is(true));

        assertThat(actualEntities.get(1).getId(), is(HEARING_EVENT_DEFINITION_ID_2));
        assertThat(actualEntities.get(1).getActionLabel(), is(ACTION_LABEL_2));
        assertThat(actualEntities.get(1).getRecordedLabel(), is(RECORDED_LABEL_2));
        assertThat(actualEntities.get(1).getSequenceNumber(), is(SEQUENCE_NUMBER_2));
        assertThat(actualEntities.get(1).getSequenceType(), is(SEQUENCE_TYPE));
        assertThat(actualEntities.get(1).getCaseAttribute(), is(CASE_ATTRIBUTE));
        assertThat(actualEntities.get(1).getGroupLabel(), is(GROUP_LABEL));
        assertThat(actualEntities.get(1).getActionLabelExtension(), is(ACTION_LABEL_EXTENSION));
        assertThat(actualEntities.get(1).isAlterable(), is(ALTERABLE_2));
        assertThat(actualEntities.get(1).isDeleted(), is(true));

        assertThat(actualEntities.get(2).getId(), is(HEARING_EVENT_DEFINITION_ID_3));
        assertThat(actualEntities.get(2).getActionLabel(), is(ACTION_LABEL_3));
        assertThat(actualEntities.get(2).getRecordedLabel(), is(RECORDED_LABEL_3));
        assertThat(actualEntities.get(2).getSequenceNumber(), is(nullValue()));
        assertThat(actualEntities.get(2).getSequenceType(), is(nullValue()));
        assertThat(actualEntities.get(2).getCaseAttribute(), is(nullValue()));
        assertThat(actualEntities.get(2).getGroupLabel(), is(nullValue()));
        assertThat(actualEntities.get(2).getActionLabelExtension(), is(nullValue()));
        assertThat(actualEntities.get(2).isAlterable(), is(ALTERABLE_3));
        assertThat(actualEntities.get(2).isDeleted(), is(true));

        verifyNoMoreInteractions(hearingEventDefinitionRepository);
    }

    private JsonEnvelope prepareHearingEventLoggedEvent() {
        return envelope()
                .withPayloadOf(HEARING_EVENT_ID, FIELD_HEARING_EVENT_ID)
                .withPayloadOf(HEARING_EVENT_DEFINITION_ID, FIELD_HEARING_EVENT_DEFINITION_ID)
                .withPayloadOf(HEARING_ID, FIELD_HEARING_ID)
                .withPayloadOf(RECORDED_LABEL, FIELD_RECORDED_LABEL)
                .withPayloadOf(ALTERABLE, FIELD_ALTERABLE)
                .withPayloadOf(ZonedDateTimes.toString(EVENT_TIME), FIELD_EVENT_TIME)
                .withPayloadOf(ZonedDateTimes.toString(LAST_MODIFIED_TIME), FIELD_LAST_MODIFIED_TIME)
                .build();
    }

    private JsonEnvelope prepareHearingEventLoggedEventWithWitness() {
        return envelope()
                .withPayloadOf(HEARING_EVENT_ID, FIELD_HEARING_EVENT_ID)
                .withPayloadOf(HEARING_EVENT_DEFINITION_ID, FIELD_HEARING_EVENT_DEFINITION_ID)
                .withPayloadOf(HEARING_ID, FIELD_HEARING_ID)
                .withPayloadOf(WITNESS_ID, FIELD_WITNESS_ID)
                .withPayloadOf(RECORDED_LABEL, FIELD_RECORDED_LABEL)
                .withPayloadOf(ALTERABLE, FIELD_ALTERABLE)
                .withPayloadOf(ZonedDateTimes.toString(EVENT_TIME), FIELD_EVENT_TIME)
                .withPayloadOf(ZonedDateTimes.toString(LAST_MODIFIED_TIME), FIELD_LAST_MODIFIED_TIME)
                .build();
    }

    private JsonEnvelope prepareHearingEventDeletedEvent() {
        return envelope()
                .withPayloadOf(HEARING_EVENT_ID, FIELD_HEARING_EVENT_ID)
                .build();
    }

    private JsonEnvelope prepareHearingEventDefinitionsCreatedEvent() {
        final JsonArrayBuilder eventDefinitionsBuilder = createArrayBuilder()
                .add(createObjectBuilder()
                        .add(FIELD_GENERIC_ID, HEARING_EVENT_DEFINITION_ID.toString())
                        .add(FIELD_ACTION_LABEL, ACTION_LABEL)
                        .add(FIELD_RECORDED_LABEL, RECORDED_LABEL)
                        .add(FIELD_SEQUENCE_NUMBER, SEQUENCE_NUMBER)
                        .add(FIELD_SEQUENCE_TYPE, SEQUENCE_TYPE)
                        .add(FIELD_ALTERABLE, ALTERABLE))
                .add(createObjectBuilder()
                        .add(FIELD_GENERIC_ID, HEARING_EVENT_DEFINITION_ID_2.toString())
                        .add(FIELD_GROUP_LABEL, GROUP_LABEL)
                        .add(FIELD_ACTION_LABEL, ACTION_LABEL_2)
                        .add(FIELD_ACTION_LABEL_EXTENSION, ACTION_LABEL_EXTENSION)
                        .add(FIELD_RECORDED_LABEL, RECORDED_LABEL_2)
                        .add(FIELD_SEQUENCE_NUMBER, SEQUENCE_NUMBER_2)
                        .add(FIELD_SEQUENCE_TYPE, SEQUENCE_TYPE)
                        .add(FIELD_CASE_ATTRIBUTE, CASE_ATTRIBUTE)
                        .add(FIELD_ALTERABLE, ALTERABLE_2))
                .add(createObjectBuilder()
                        .add(FIELD_GENERIC_ID, HEARING_EVENT_DEFINITION_ID_3.toString())
                        .add(FIELD_ACTION_LABEL, ACTION_LABEL_3)
                        .add(FIELD_RECORDED_LABEL, RECORDED_LABEL_3)
                        .add(FIELD_ALTERABLE, ALTERABLE_3));

        return envelopeFrom(metadataWithRandomUUIDAndName(),
                createObjectBuilder()
                        .add(FIELD_GENERIC_ID, HEARING_EVENT_DEFINITIONS_ID.toString())
                        .add(FIELD_EVENT_DEFINITIONS, eventDefinitionsBuilder)
                        .build()
        );
    }

    private JsonEnvelope prepareHearingEventDefinitionsDeletedEvent() {
        return envelopeFrom(metadataWithRandomUUIDAndName(), createObjectBuilder().build());
    }

    private List<HearingEventDefinition> prepareActiveHearingEventDefinitions() {
        return newArrayList(
                new HearingEventDefinition(HEARING_EVENT_DEFINITION_ID, RECORDED_LABEL, ACTION_LABEL, SEQUENCE_NUMBER,
                        SEQUENCE_TYPE, null, null, null, ALTERABLE),
                new HearingEventDefinition(HEARING_EVENT_DEFINITION_ID_2, RECORDED_LABEL_2, ACTION_LABEL_2, SEQUENCE_NUMBER_2,
                        SEQUENCE_TYPE, CASE_ATTRIBUTE, GROUP_LABEL, ACTION_LABEL_EXTENSION, ALTERABLE_2),
                new HearingEventDefinition(HEARING_EVENT_DEFINITION_ID_3, RECORDED_LABEL_3, ACTION_LABEL_3, null,
                        null, null, null, null, ALTERABLE_3)
        );
    }

    private JsonEnvelope prepareHearingEventsUpdated() {
        final JsonArrayBuilder arrayhearingEventArrayBuilderBuilder =
                        createArrayBuilder().add(createObjectBuilder()
                                        .add(FIELD_HEARING_EVENT_ID, HEARING_EVENT_ID.toString())
                                        .add(FIELD_RECORDED_LABEL, RECORDED_LABEL.toString()));

        return envelopeFrom(metadataWithRandomUUIDAndName(),
                        createObjectBuilder().add(FIELD_HEARING_ID, HEARING_ID.toString())
                                        .add(FIELD_HEARING_EVENTS,
                                                        arrayhearingEventArrayBuilderBuilder)
                                        .build());
    }
}