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

import uk.gov.justice.core.courts.DefenceCounsel;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.moj.cpp.hearing.domain.event.DefenceCounselAdded;
import uk.gov.moj.cpp.hearing.domain.event.DefenceCounselUpdated;
import uk.gov.moj.cpp.hearing.mapping.HearingDefenceCounselJPAMapper;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingDefenceCounsel;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.repository.HearingDefenceCounselRepository;
import uk.gov.moj.cpp.hearing.repository.HearingRepository;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DefenceCounselAddedEventListenerTest {

    @Mock
    private HearingRepository hearingRepository;

    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @InjectMocks
    private DefenceCounselEventListener defenceCounselAddedEventListener;

    @Captor
    private ArgumentCaptor<HearingDefenceCounsel> ahearingArgumentCaptor;

    @Mock
    HearingDefenceCounselJPAMapper hearingDefenceCounselJPAMapper;

    @Mock
    HearingDefenceCounselRepository hearingDefenceCounselRepository;

    @BeforeEach
    public void setUp() {
        setField(this.jsonObjectToObjectConverter, "objectMapper", new ObjectMapperProducer().objectMapper());
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
    }

    @Test
    public void shouldStoreDefenceCounselOnAddEvent() {

        //Given
        DefenceCounsel defenceCounsel = new DefenceCounsel(
                Arrays.asList(LocalDate.now()),
                Arrays.asList(UUID.randomUUID()),
                STRING.next(),
                randomUUID(),
                STRING.next(),
                STRING.next(),
                STRING.next(),
                STRING.next(),
                randomUUID()
        );

        final DefenceCounselAdded defenceCounselAdded = new DefenceCounselAdded(defenceCounsel, randomUUID());

        final Hearing hearing = new Hearing();
        hearing.setId(defenceCounselAdded.getHearingId());

        HearingDefenceCounsel hearingDefenceCounsel = new HearingDefenceCounsel();
        hearingDefenceCounsel.setId(new HearingSnapshotKey(UUID.randomUUID(), hearing.getId()));
        hearingDefenceCounsel.setHearing(hearing);
        hearingDefenceCounsel.setPayload(getEntityPayload(defenceCounsel));

        when(this.hearingRepository.findBy(defenceCounselAdded.getHearingId())).thenReturn(hearing);
        when(this.hearingDefenceCounselJPAMapper.toJPA(hearing, defenceCounsel)).thenReturn(hearingDefenceCounsel);

        //When
        this.defenceCounselAddedEventListener.defenceCounselAdded(envelopeFrom(metadataWithRandomUUID("hearing.defence-counsel-added"),
                objectToJsonObjectConverter.convert(defenceCounselAdded)));

        //Then
        verify(this.hearingDefenceCounselRepository).saveAndFlush(ahearingArgumentCaptor.capture());

        final HearingDefenceCounsel savedHearing = ahearingArgumentCaptor.getValue();
        assertThat(savedHearing, is(hearingDefenceCounsel));

    }

    @Test
    public void shouldUpdateDefenceCounselOnUpdateEvent() {

        //Given
        DefenceCounsel defenceCounsel = new DefenceCounsel(
                Arrays.asList(LocalDate.now()),
                Arrays.asList(UUID.randomUUID()),
                STRING.next(),
                randomUUID(),
                STRING.next(),
                STRING.next(),
                STRING.next(),
                STRING.next(),
                randomUUID()
        );

        final DefenceCounselUpdated defenceCounselUpdated = new DefenceCounselUpdated(defenceCounsel, randomUUID());

        final Hearing hearing = new Hearing();
        hearing.setId(defenceCounselUpdated.getHearingId());

        HearingDefenceCounsel hearingDefenceCounsel = new HearingDefenceCounsel();
        hearingDefenceCounsel.setId(new HearingSnapshotKey(UUID.randomUUID(), hearing.getId()));
        hearingDefenceCounsel.setHearing(hearing);
        hearingDefenceCounsel.setPayload(getEntityPayload(defenceCounsel));

        when(this.hearingRepository.findBy(defenceCounselUpdated.getHearingId())).thenReturn(hearing);
        when(this.hearingDefenceCounselJPAMapper.toJPA(hearing, defenceCounsel)).thenReturn(hearingDefenceCounsel);

        //When
        this.defenceCounselAddedEventListener.defenceCounselUpdated(envelopeFrom(metadataWithRandomUUID("hearing.defence-counsel-updated"),
                objectToJsonObjectConverter.convert(defenceCounselUpdated)));

        //Then
        verify(this.hearingDefenceCounselRepository).saveAndFlush(ahearingArgumentCaptor.capture());

        final HearingDefenceCounsel savedHearing = ahearingArgumentCaptor.getValue();
        assertThat(savedHearing, is(hearingDefenceCounsel));

    }

    private JsonNode getEntityPayload(DefenceCounsel defenceCounsel) {

        final ObjectMapper mapper = new ObjectMapperProducer().objectMapper();

        return mapper.valueToTree(defenceCounsel);

    }
}
