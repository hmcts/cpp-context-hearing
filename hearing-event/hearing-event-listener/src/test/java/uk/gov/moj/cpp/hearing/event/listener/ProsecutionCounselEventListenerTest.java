package uk.gov.moj.cpp.hearing.event.listener;


import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.common.reflection.ReflectionUtils.setField;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;

import uk.gov.justice.core.courts.ProsecutionCounsel;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.moj.cpp.hearing.domain.event.ProsecutionCounselAdded;
import uk.gov.moj.cpp.hearing.domain.event.ProsecutionCounselRemoved;
import uk.gov.moj.cpp.hearing.domain.event.ProsecutionCounselUpdated;
import uk.gov.moj.cpp.hearing.mapping.HearingProsecutionCounselJPAMapper;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingProsecutionCounsel;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.repository.HearingProsecutionCounselRepository;
import uk.gov.moj.cpp.hearing.repository.HearingRepository;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ProsecutionCounselEventListenerTest {

    @Mock
    private HearingRepository hearingRepository;

    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @InjectMocks
    private ProsecutionCounselEventListener prosecutionCounselEventListener;

    @Captor
    private ArgumentCaptor<HearingProsecutionCounsel> ahearingArgumentCaptor;

    @Mock
    HearingProsecutionCounselJPAMapper hearingProsecutionCounselJPAMapper;

    @Mock
    HearingProsecutionCounselRepository hearingProsecutionCounselRepository;

    @Before
    public void setUp() {
        setField(this.jsonObjectToObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
    }

    @Test
    public void shouldStoreProsecutionCounselOnAddEvent() {

        //Given
        uk.gov.justice.core.courts.ProsecutionCounsel prosecutionCounsel = new ProsecutionCounsel(
                Arrays.asList(LocalDate.now()),
                STRING.next(),
                randomUUID(),
                STRING.next(),
                null,
                Arrays.asList(UUID.randomUUID()),
                STRING.next(),
                STRING.next()
        );

        final ProsecutionCounselAdded prosecutionCounselAdded = new ProsecutionCounselAdded(prosecutionCounsel, randomUUID());

        final Hearing hearing = new Hearing();
        hearing.setId(prosecutionCounselAdded.getHearingId());

        HearingProsecutionCounsel hearingProsecutionCounsel = new HearingProsecutionCounsel();
        hearingProsecutionCounsel.setId(new HearingSnapshotKey(randomUUID(), hearing.getId()));
        hearingProsecutionCounsel.setHearing(hearing);
        hearingProsecutionCounsel.setPayload(getEntityPayload(prosecutionCounsel));

        when(this.hearingRepository.findBy(prosecutionCounselAdded.getHearingId())).thenReturn(hearing);
        when(this.hearingProsecutionCounselJPAMapper.toJPA(hearing,prosecutionCounsel)).thenReturn(hearingProsecutionCounsel);

        //When
        this.prosecutionCounselEventListener.prosecutionCounselAdded(envelopeFrom(metadataWithRandomUUID("hearing.prosecution-counsel-added"),
                objectToJsonObjectConverter.convert(prosecutionCounselAdded)));

        //Then
        verify(this.hearingProsecutionCounselRepository).saveAndFlush(ahearingArgumentCaptor.capture());

        final HearingProsecutionCounsel savedHearing = ahearingArgumentCaptor.getValue();
        assertThat(savedHearing, Matchers.is(hearingProsecutionCounsel));

    }

    @Test
    public void shouldUpdateProsecutionCounselOnUpdateEvent() {

        //Given
        uk.gov.justice.core.courts.ProsecutionCounsel prosecutionCounsel = new ProsecutionCounsel(
                Arrays.asList(LocalDate.now()),
                STRING.next(),
                randomUUID(),
                STRING.next(),
                null,
                Arrays.asList(UUID.randomUUID()),
                STRING.next(),
                STRING.next()
        );

        final ProsecutionCounselUpdated prosecutionCounselUpdated = new ProsecutionCounselUpdated(prosecutionCounsel, randomUUID());

        final Hearing hearing = new Hearing();
        hearing.setId(prosecutionCounselUpdated.getHearingId());

        HearingProsecutionCounsel hearingProsecutionCounsel = new HearingProsecutionCounsel();
        hearingProsecutionCounsel.setId(new HearingSnapshotKey(randomUUID(), hearing.getId()));
        hearingProsecutionCounsel.setHearing(hearing);
        hearingProsecutionCounsel.setPayload(getEntityPayload(prosecutionCounsel));

        when(this.hearingRepository.findBy(prosecutionCounselUpdated.getHearingId())).thenReturn(hearing);
        when(this.hearingProsecutionCounselJPAMapper.toJPA(hearing,prosecutionCounsel)).thenReturn(hearingProsecutionCounsel);

        //When
        this.prosecutionCounselEventListener.prosecutionCounselUpdated(envelopeFrom(metadataWithRandomUUID("hearing.defence-counsel-updated"),
                objectToJsonObjectConverter.convert(prosecutionCounselUpdated)));

        //Then
        verify(this.hearingProsecutionCounselRepository).saveAndFlush(ahearingArgumentCaptor.capture());

        final HearingProsecutionCounsel savedHearing = ahearingArgumentCaptor.getValue();
        assertThat(savedHearing, Matchers.is(hearingProsecutionCounsel));

    }

    @Test
    public void shouldRemoveProsecutionCounselOnRemoveEvent() {

        //Given
        uk.gov.justice.core.courts.ProsecutionCounsel prosecutionCounsel = new ProsecutionCounsel(
                Arrays.asList(LocalDate.now()),
                STRING.next(),
                randomUUID(),
                STRING.next(),
                null,
                Arrays.asList(UUID.randomUUID()),
                STRING.next(),
                STRING.next()
        );

        final ProsecutionCounselRemoved prosecutionCounselRemoved = new ProsecutionCounselRemoved(randomUUID(),prosecutionCounsel.getId());

        final Hearing hearing = new Hearing();
        hearing.setId(prosecutionCounselRemoved.getHearingId());

        HearingProsecutionCounsel hearingProsecutionCounsel = new HearingProsecutionCounsel();
        hearingProsecutionCounsel.setId(new HearingSnapshotKey(prosecutionCounselRemoved.getId(), hearing.getId()));
        hearingProsecutionCounsel.setHearing(hearing);
        hearingProsecutionCounsel.setPayload(getEntityPayload(prosecutionCounsel));

        hearing.setProsecutionCounsels(Collections.singleton(hearingProsecutionCounsel));
        when(this.hearingRepository.findBy(prosecutionCounselRemoved.getHearingId())).thenReturn(hearing);

        //When
        this.prosecutionCounselEventListener.prosecutionCounselRemoved(envelopeFrom(metadataWithRandomUUID("hearing.prosecution-counsel-removed"),
                objectToJsonObjectConverter.convert(prosecutionCounselRemoved)));

        //Then
        verify(this.hearingProsecutionCounselRepository).saveAndFlush(ahearingArgumentCaptor.capture());

        final HearingProsecutionCounsel savedHearing = ahearingArgumentCaptor.getValue();
        assertThat(savedHearing, Matchers.is(hearingProsecutionCounsel));

    }

    private JsonNode getEntityPayload(ProsecutionCounsel prosecutionCounsel){

        final ObjectMapper mapper = new ObjectMapperProducer().objectMapper();

        return mapper.valueToTree(prosecutionCounsel);

    }
}
