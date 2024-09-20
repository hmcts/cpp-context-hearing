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

import uk.gov.justice.core.courts.RespondentCounsel;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.moj.cpp.hearing.domain.event.RespondentCounselAdded;
import uk.gov.moj.cpp.hearing.domain.event.RespondentCounselUpdated;
import uk.gov.moj.cpp.hearing.mapping.HearingRespondentCounselJPAMapper;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingRespondentCounsel;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.repository.HearingRepository;
import uk.gov.moj.cpp.hearing.repository.HearingRespondentCounselRepository;

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
public class RespondentCounselAddedEventListenerTest {

    @Mock
    private HearingRepository hearingRepository;

    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @InjectMocks
    private RespondentCounselEventListener respondentCounselAddedEventListener;

    @Captor
    private ArgumentCaptor<HearingRespondentCounsel> ahearingArgumentCaptor;

    @Mock
    HearingRespondentCounselJPAMapper hearingRespondentCounselJPAMapper;

    @Mock
    HearingRespondentCounselRepository hearingRespondentCounselRepository;

    @BeforeEach
    public void setUp() {
        setField(this.jsonObjectToObjectConverter, "objectMapper", new ObjectMapperProducer().objectMapper());
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
    }

    @Test
    public void shouldStoreRespondentCounselOnAddEvent() {

        //Given
        RespondentCounsel respondentCounsel = new RespondentCounsel(
                Arrays.asList(LocalDate.now()),
                STRING.next(),
                randomUUID(),
                STRING.next(),
                STRING.next(),
                Arrays.asList(UUID.randomUUID()),
                STRING.next(),
                STRING.next()
        );

        final RespondentCounselAdded respondentCounselAdded = new RespondentCounselAdded(respondentCounsel, randomUUID());

        final Hearing hearing = new Hearing();
        hearing.setId(respondentCounselAdded.getHearingId());

        HearingRespondentCounsel hearingRespondentCounsel = new HearingRespondentCounsel();
        hearingRespondentCounsel.setId(new HearingSnapshotKey(UUID.randomUUID(), hearing.getId()));
        hearingRespondentCounsel.setHearing(hearing);
        hearingRespondentCounsel.setPayload(getEntityPayload(respondentCounsel));

        when(this.hearingRepository.findBy(respondentCounselAdded.getHearingId())).thenReturn(hearing);
        when(this.hearingRespondentCounselJPAMapper.toJPA(hearing, respondentCounsel)).thenReturn(hearingRespondentCounsel);

        //When
        this.respondentCounselAddedEventListener.respondentCounselAdded(envelopeFrom(metadataWithRandomUUID("hearing.respondent-counsel-added"),
                objectToJsonObjectConverter.convert(respondentCounselAdded)));

        //Then
        verify(this.hearingRespondentCounselRepository).saveAndFlush(ahearingArgumentCaptor.capture());

        final HearingRespondentCounsel savedHearing = ahearingArgumentCaptor.getValue();
        assertThat(savedHearing, is(hearingRespondentCounsel));

    }

    @Test
    public void shouldUpdateRespondentCounselOnUpdateEvent() {

        //Given
        RespondentCounsel respondentCounsel = new RespondentCounsel(
                Arrays.asList(LocalDate.now()),
                STRING.next(),
                randomUUID(),
                STRING.next(),
                STRING.next(),
                Arrays.asList(UUID.randomUUID()),
                STRING.next(),
                STRING.next()
        );

        final RespondentCounselUpdated respondentCounselUpdated = new RespondentCounselUpdated(respondentCounsel, randomUUID());

        final Hearing hearing = new Hearing();
        hearing.setId(respondentCounselUpdated.getHearingId());

        HearingRespondentCounsel hearingRespondentCounsel = new HearingRespondentCounsel();
        hearingRespondentCounsel.setId(new HearingSnapshotKey(UUID.randomUUID(), hearing.getId()));
        hearingRespondentCounsel.setHearing(hearing);
        hearingRespondentCounsel.setPayload(getEntityPayload(respondentCounsel));

        when(this.hearingRepository.findBy(respondentCounselUpdated.getHearingId())).thenReturn(hearing);
        when(this.hearingRespondentCounselJPAMapper.toJPA(hearing, respondentCounsel)).thenReturn(hearingRespondentCounsel);

        //When
        this.respondentCounselAddedEventListener.respondentCounselUpdated(envelopeFrom(metadataWithRandomUUID("hearing.respondent-counsel-updated"),
                objectToJsonObjectConverter.convert(respondentCounselUpdated)));

        //Then
        verify(this.hearingRespondentCounselRepository).saveAndFlush(ahearingArgumentCaptor.capture());

        final HearingRespondentCounsel savedHearing = ahearingArgumentCaptor.getValue();
        assertThat(savedHearing, is(hearingRespondentCounsel));

    }

    private JsonNode getEntityPayload(RespondentCounsel respondentCounsel) {

        final ObjectMapper mapper = new ObjectMapperProducer().objectMapper();

        return mapper.valueToTree(respondentCounsel);

    }
}
