package uk.gov.moj.cpp.hearing.event.listener;

import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;

import uk.gov.justice.core.courts.Attendant;
import uk.gov.justice.core.courts.InterpreterIntermediary;
import uk.gov.justice.hearing.courts.AttendantType;
import uk.gov.justice.hearing.courts.Role;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.moj.cpp.hearing.domain.event.InterpreterIntermediaryAdded;
import uk.gov.moj.cpp.hearing.domain.event.InterpreterIntermediaryRemoved;
import uk.gov.moj.cpp.hearing.domain.event.InterpreterIntermediaryUpdated;
import uk.gov.moj.cpp.hearing.mapping.HearingInterpreterIntermediaryJPAMapper;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingInterpreterIntermediary;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.repository.HearingInterpreterIntermediaryRepository;
import uk.gov.moj.cpp.hearing.repository.HearingRepository;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class InterpreterIntermediaryEventListenerTest {

    @Mock
    private HearingRepository hearingRepository;

    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @InjectMocks
    private InterpreterIntermediaryEventListener interpreterIntermediaryEventListener;

    @Captor
    private ArgumentCaptor<HearingInterpreterIntermediary> ahearingArgumentCaptor;

    @Mock
    HearingInterpreterIntermediaryJPAMapper interpreterIntermediaryJPAMapper;

    @Mock
    HearingInterpreterIntermediaryRepository interpreterIntermediaryRepository;

    @Before
    public void setUp() {
        setField(this.jsonObjectToObjectConverter, "objectMapper", new ObjectMapperProducer().objectMapper());
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
    }

    @Test
    public void shouldStoreInterpreterIntermediaryOnAddEvent() {

        //Given

        Attendant attendant = new Attendant(AttendantType.WITNESS, randomUUID(), STRING.next());
        InterpreterIntermediary interpreterIntermediary = new InterpreterIntermediary(
                Arrays.asList(LocalDate.now()),
                attendant,
                STRING.next(),
                randomUUID(),
                STRING.next(),
                Role.INTERMEDIARY
        );

        final InterpreterIntermediaryAdded interpreterIntermediaryAdded = new InterpreterIntermediaryAdded(interpreterIntermediary, randomUUID());

        final Hearing hearing = new Hearing();
        hearing.setId(interpreterIntermediaryAdded.getHearingId());

        HearingInterpreterIntermediary hearingInterpreterIntermediary = new HearingInterpreterIntermediary();
        hearingInterpreterIntermediary.setId(new HearingSnapshotKey(UUID.randomUUID(), hearing.getId()));
        hearingInterpreterIntermediary.setHearing(hearing);
        hearingInterpreterIntermediary.setPayload(getEntityPayload(interpreterIntermediary));

        when(this.hearingRepository.findBy(interpreterIntermediaryAdded.getHearingId())).thenReturn(hearing);
        when(this.interpreterIntermediaryJPAMapper.toJPA(hearing, interpreterIntermediary)).thenReturn(hearingInterpreterIntermediary);

        //When
        this.interpreterIntermediaryEventListener.interpreterIntermediaryAdded(envelopeFrom(metadataWithRandomUUID("hearing.interpreterIntermediary-added"),
                objectToJsonObjectConverter.convert(interpreterIntermediaryAdded)));

        //Then
        verify(this.interpreterIntermediaryRepository).saveAndFlush(ahearingArgumentCaptor.capture());

        final HearingInterpreterIntermediary savedHearing = ahearingArgumentCaptor.getValue();
        assertThat(savedHearing, is(hearingInterpreterIntermediary));

    }

    @Test
    public void shouldUpdateInterpreterIntermediaryOnUpdateEvent() {

        //Given
        Attendant attendant = new Attendant(AttendantType.WITNESS, randomUUID(), STRING.next());
        InterpreterIntermediary interpreterIntermediary = new InterpreterIntermediary(
                Arrays.asList(LocalDate.now()),
                attendant,
                STRING.next(),
                randomUUID(),
                STRING.next(),
                Role.INTERMEDIARY
        );

        final InterpreterIntermediaryUpdated interpreterIntermediaryUpdated = new InterpreterIntermediaryUpdated(interpreterIntermediary, randomUUID());

        final Hearing hearing = new Hearing();
        hearing.setId(interpreterIntermediaryUpdated.getHearingId());

        HearingInterpreterIntermediary hearingInterpreterIntermediary = new HearingInterpreterIntermediary();
        hearingInterpreterIntermediary.setId(new HearingSnapshotKey(UUID.randomUUID(), hearing.getId()));
        hearingInterpreterIntermediary.setHearing(hearing);
        hearingInterpreterIntermediary.setPayload(getEntityPayload(interpreterIntermediary));

        when(this.hearingRepository.findBy(interpreterIntermediaryUpdated.getHearingId())).thenReturn(hearing);
        when(this.interpreterIntermediaryJPAMapper.toJPA(hearing, interpreterIntermediary)).thenReturn(hearingInterpreterIntermediary);

        //When
        this.interpreterIntermediaryEventListener.interpreterIntermediaryUpdated(envelopeFrom(metadataWithRandomUUID("hearing.interpreter-intermediary-updated"),
                objectToJsonObjectConverter.convert(interpreterIntermediaryUpdated)));

        //Then
        verify(this.interpreterIntermediaryRepository).saveAndFlush(ahearingArgumentCaptor.capture());

        final HearingInterpreterIntermediary savedHearing = ahearingArgumentCaptor.getValue();
        assertThat(savedHearing, is(hearingInterpreterIntermediary));

    }

    @Test
    public void shouldRemoveInterpreterIntermediaryOnRemoveEvent() {

        //Given
        Attendant attendant = new Attendant(AttendantType.WITNESS, randomUUID(), STRING.next());
        InterpreterIntermediary interpreterIntermediary = new InterpreterIntermediary(
                Arrays.asList(LocalDate.now()),
                attendant,
                STRING.next(),
                randomUUID(),
                STRING.next(),
                Role.INTERMEDIARY
        );

        final InterpreterIntermediaryRemoved interpreterIntermediaryRemoved = new InterpreterIntermediaryRemoved(randomUUID(), interpreterIntermediary.getId());

        final Hearing hearing = new Hearing();
        hearing.setId(interpreterIntermediaryRemoved.getHearingId());

        HearingInterpreterIntermediary hearingInterpreterIntermediary = new HearingInterpreterIntermediary();
        hearingInterpreterIntermediary.setId(new HearingSnapshotKey(interpreterIntermediaryRemoved.getId(), hearing.getId()));
        hearingInterpreterIntermediary.setHearing(hearing);
        hearingInterpreterIntermediary.setPayload(getEntityPayload(interpreterIntermediary));

        hearing.setHearingInterpreterIntermediaries(Collections.singleton(hearingInterpreterIntermediary));
        when(this.hearingRepository.findBy(interpreterIntermediaryRemoved.getHearingId())).thenReturn(hearing);

        //When
        this.interpreterIntermediaryEventListener.interpreterIntermediaryRemoved(envelopeFrom(metadataWithRandomUUID("hearing.interpreter-intermediary-removed"),
                objectToJsonObjectConverter.convert(interpreterIntermediaryRemoved)));

        //Then
        verify(this.interpreterIntermediaryRepository).saveAndFlush(ahearingArgumentCaptor.capture());

        final HearingInterpreterIntermediary savedHearing = ahearingArgumentCaptor.getValue();
        assertThat(savedHearing, Matchers.is(hearingInterpreterIntermediary));

    }
    private JsonNode getEntityPayload(InterpreterIntermediary hearingInterpreterIntermediary) {

        final ObjectMapper mapper = new ObjectMapperProducer().objectMapper();

        return mapper.valueToTree(hearingInterpreterIntermediary);

    }
}
