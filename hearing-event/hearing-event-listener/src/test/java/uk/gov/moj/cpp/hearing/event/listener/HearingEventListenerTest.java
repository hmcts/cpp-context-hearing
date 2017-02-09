package uk.gov.moj.cpp.hearing.event.listener;

import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.DefaultJsonEnvelope.envelope;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_ZONED_DATE_TIME;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ZonedDateTimes;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.HearingAdjournDateUpdated;
import uk.gov.moj.cpp.hearing.domain.event.HearingInitiated;
import uk.gov.moj.cpp.hearing.domain.event.ProsecutionCounselAdded;
import uk.gov.moj.cpp.hearing.event.listener.converter.HearingEventsToHearingConverter;
import uk.gov.moj.cpp.hearing.persist.HearingEventRepository;
import uk.gov.moj.cpp.hearing.persist.HearingOutcomeRepository;
import uk.gov.moj.cpp.hearing.persist.HearingRepository;
import uk.gov.moj.cpp.hearing.persist.ProsecutionCounselRepository;
import uk.gov.moj.cpp.hearing.persist.entity.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.HearingEvent;
import uk.gov.moj.cpp.hearing.persist.entity.HearingOutcome;
import uk.gov.moj.cpp.hearing.persist.entity.ProsecutionCounsel;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import javax.json.JsonObject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class HearingEventListenerTest {

    private static final String FIELD_HEARING_EVENT_ID = "hearingEventId";
    private static final String FIELD_RECORDED_LABEL = "recordedLabel";
    private static final String FIELD_HEARING_ID = "hearingId";
    private static final String FIELD_TIMESTAMP = "timestamp";

    private static final UUID HEARING_EVENT_ID = randomUUID();
    private static final UUID HEARING_ID = randomUUID();
    private static final String RECORDED_LABEL = STRING.next();
    private static final ZonedDateTime TIMESTAMP = PAST_ZONED_DATE_TIME.next();

    private static final String DEFENDANT_ID = "defendantId";
    private static final UUID DEFENDANT_ID_VALUE = randomUUID();
    private static final String TARGET_ID = "targetId";
    private static final UUID TARGET_ID_VALUE = randomUUID();
    private static final String OFFENCE_ID = "offenceId";
    private static final UUID OFFENCE_ID_VALUE = randomUUID();
    private static final String DRAFT_RESULT = "draftResult";
    private static final String ARBITRARY_STRING_IMP_2_YRS = "imp 2 yrs";

    @Mock
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Mock
    private HearingEventsToHearingConverter hearingEventsToHearingConverter;

    @Mock
    private HearingRepository hearingRepository;

    @Mock
    private ProsecutionCounselRepository prosecutionCounselRepository;

    @Mock
    private JsonEnvelope envelope;

    @Mock
    private HearingInitiated hearingInitiated;

    @Mock
    private HearingAdjournDateUpdated hearingAdjournDateUpdated;

    @Mock
    private Hearing hearing;

    @Mock
    private ProsecutionCounsel prosecutionCounsel;

    @Mock
    private JsonObject payload;

    @Mock
    private HearingEventRepository hearingEventRepository;

    @Mock
    private HearingOutcomeRepository hearingOutcomeRepository;

    @Captor
    private ArgumentCaptor<HearingEvent> eventLogArgumentCaptor;

    @Captor
    private ArgumentCaptor<HearingOutcome> hearingOutcomeArgumentCaptor;

    @InjectMocks
    private HearingEventListener hearingEventListener;

    @Test
    public void shouldHandleHearingCreatedEvent() throws Exception {

        when(envelope.payloadAsJsonObject()).thenReturn(payload);
        when(jsonObjectToObjectConverter.convert(payload, HearingInitiated.class)).thenReturn(hearingInitiated);
        when(hearingInitiated.getStartDateTime()).thenReturn(ZonedDateTime.now());
        when(hearingInitiated.getDuration()).thenReturn(1);
        when(hearingRepository.getByHearingId(hearing.getHearingId())).thenReturn(Optional.empty());
        when(hearingEventsToHearingConverter.convert(hearingInitiated)).thenReturn(hearing);
        hearingEventListener.hearingInitiated(envelope);
        verify(hearingRepository).save(hearing);

    }

    @Test
    public void shouldHandleProsecutionCounselAddedEvent() throws Exception {
        when(envelope.payloadAsJsonObject()).thenReturn(payload);

        final ProsecutionCounselAdded prosecutionCounselAdded =
                new ProsecutionCounselAdded(randomUUID(), randomUUID(), randomUUID(), STRING.next());

        when(jsonObjectToObjectConverter.convert(payload, ProsecutionCounselAdded.class)).thenReturn(prosecutionCounselAdded);
        when(hearingEventsToHearingConverter.convert(prosecutionCounselAdded)).thenReturn(prosecutionCounsel);
        hearingEventListener.prosecutionCounselAdded(envelope);
        verify(prosecutionCounselRepository).save(prosecutionCounsel);
    }

    @Test
    public void shouldPersistAHearingEventLog() {
        final JsonEnvelope event = getHearingEventLogJsonEnvelope();

        hearingEventListener.hearingEventLogged(event);

        verify(hearingEventRepository).save(eventLogArgumentCaptor.capture());
        assertThat(eventLogArgumentCaptor.getValue().getId(), is(HEARING_EVENT_ID));
        assertThat(eventLogArgumentCaptor.getValue().getHearingId(), is(HEARING_ID));
        assertThat(eventLogArgumentCaptor.getValue().getRecordedLabel(), is(RECORDED_LABEL));
        assertThat(eventLogArgumentCaptor.getValue().getTimestamp().toString(), is(ZonedDateTimes.toString(TIMESTAMP)));
    }

    @Test
    public void shouldDeleteAnExistingHearingEvent() {
        final JsonEnvelope event = getHearingEventJsonEnvelopeToDelete();
        when(hearingEventRepository.findById(HEARING_EVENT_ID)).thenReturn(
                Optional.of(new HearingEvent(HEARING_EVENT_ID, HEARING_ID, RECORDED_LABEL, TIMESTAMP))
        );

        hearingEventListener.hearingEventDeleted(event);

        verify(hearingEventRepository).save(eventLogArgumentCaptor.capture());
        assertThat(eventLogArgumentCaptor.getValue().getId(), is(HEARING_EVENT_ID));
        assertThat(eventLogArgumentCaptor.getValue().getHearingId(), is(HEARING_ID));
        assertThat(eventLogArgumentCaptor.getValue().getRecordedLabel(), is(RECORDED_LABEL));
        assertThat(eventLogArgumentCaptor.getValue().getTimestamp(), is(TIMESTAMP));
        assertThat(eventLogArgumentCaptor.getValue().isDeleted(), is(true));
    }

    @Test
    public void shouldPersistHearingDraftResult() {
        final JsonEnvelope event = getSaveDraftResultJsonEnvelope();

        hearingEventListener.draftResultSaved(event);

        verify(hearingOutcomeRepository).save(hearingOutcomeArgumentCaptor.capture());
        assertThat(hearingOutcomeArgumentCaptor.getValue().getId(), is(TARGET_ID_VALUE));
        assertThat(hearingOutcomeArgumentCaptor.getValue().getHearingId(), is(HEARING_ID));
        assertThat(hearingOutcomeArgumentCaptor.getValue().getDraftResult(), is(ARBITRARY_STRING_IMP_2_YRS));
        assertThat(hearingOutcomeArgumentCaptor.getValue().getDefendantId(), is(DEFENDANT_ID_VALUE));
        assertThat(hearingOutcomeArgumentCaptor.getValue().getOffenceId(), is(OFFENCE_ID_VALUE));

    }

    @Test
    public void shouldHandleHearingAdjournDateUpdatedEvent() throws Exception {

        when(envelope.payloadAsJsonObject()).thenReturn(payload);
        when(jsonObjectToObjectConverter.convert(payload, HearingAdjournDateUpdated.class)).thenReturn(hearingAdjournDateUpdated);
        when(hearingRepository.getByHearingId(hearing.getHearingId())).thenReturn(Optional.empty());
        when(hearingEventsToHearingConverter.convert(hearingAdjournDateUpdated)).thenReturn(hearing);
        hearingEventListener.hearingAdjournDateUpdated(envelope);
        verify(hearingRepository).save(hearing);

    }

    private JsonEnvelope getHearingEventLogJsonEnvelope() {
        return envelope()
                .withPayloadOf(HEARING_EVENT_ID, FIELD_HEARING_EVENT_ID)
                .withPayloadOf(HEARING_ID, FIELD_HEARING_ID)
                .withPayloadOf(RECORDED_LABEL, FIELD_RECORDED_LABEL)
                .withPayloadOf(ZonedDateTimes.toString(TIMESTAMP), FIELD_TIMESTAMP)
                .build();
    }

    private JsonEnvelope getHearingEventJsonEnvelopeToDelete() {
        return envelope()
                .withPayloadOf(HEARING_EVENT_ID, FIELD_HEARING_EVENT_ID)
                .build();
    }

    private JsonEnvelope getSaveDraftResultJsonEnvelope() {
        return envelope()
                .withPayloadOf(HEARING_ID, FIELD_HEARING_ID)
                .withPayloadOf(DEFENDANT_ID_VALUE, DEFENDANT_ID)
                .withPayloadOf(TARGET_ID_VALUE, TARGET_ID)
                .withPayloadOf(OFFENCE_ID_VALUE, OFFENCE_ID)
                .withPayloadOf(ARBITRARY_STRING_IMP_2_YRS, DRAFT_RESULT)
                .build();
    }
}