package uk.gov.moj.cpp.hearing.event.listener;


import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;

import uk.gov.justice.core.courts.ApplicantCounsel;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.moj.cpp.hearing.domain.event.ApplicantCounselAdded;
import uk.gov.moj.cpp.hearing.domain.event.ApplicantCounselRemoved;
import uk.gov.moj.cpp.hearing.domain.event.ApplicantCounselUpdated;
import uk.gov.moj.cpp.hearing.mapping.HearingApplicantCounselJPAMapper;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingApplicantCounsel;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.repository.HearingApplicantCounselRepository;
import uk.gov.moj.cpp.hearing.repository.HearingRepository;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
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
public class ApplicantCounselEventListenerTest {

    @Mock
    private HearingRepository hearingRepository;

    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @InjectMocks
    private ApplicantCounselEventListener applicantCounselEventListener;

    @Captor
    private ArgumentCaptor<HearingApplicantCounsel> ahearingArgumentCaptor;

    @Mock
    HearingApplicantCounselJPAMapper hearingApplicantCounselJPAMapper;

    @Mock
    HearingApplicantCounselRepository hearingApplicantCounselRepository;

    @BeforeEach
    public void setUp() {
        setField(this.jsonObjectToObjectConverter, "objectMapper", new ObjectMapperProducer().objectMapper());
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
    }

    @Test
    public void shouldStoreApplicantCounselOnAddEvent() {

        //Given
        ApplicantCounsel applicantCounsel = new ApplicantCounsel(
                Arrays.asList(UUID.randomUUID()),
                Arrays.asList(LocalDate.now()),
                STRING.next(),
                randomUUID(),
                STRING.next(),
                null,
                STRING.next(),
                STRING.next()
        );

        final ApplicantCounselAdded applicantCounselAdded = new ApplicantCounselAdded(applicantCounsel, randomUUID());

        final Hearing hearing = new Hearing();
        hearing.setId(applicantCounselAdded.getHearingId());

        HearingApplicantCounsel hearingApplicantCounsel = new HearingApplicantCounsel();
        hearingApplicantCounsel.setId(new HearingSnapshotKey(randomUUID(), hearing.getId()));
        hearingApplicantCounsel.setHearing(hearing);
        hearingApplicantCounsel.setPayload(getEntityPayload(applicantCounsel));

        when(this.hearingRepository.findBy(applicantCounselAdded.getHearingId())).thenReturn(hearing);
        when(this.hearingApplicantCounselJPAMapper.toJPA(hearing, applicantCounsel)).thenReturn(hearingApplicantCounsel);

        //When
        this.applicantCounselEventListener.applicantCounselAdded(envelopeFrom(metadataWithRandomUUID("hearing.applicant-counsel-added"),
                objectToJsonObjectConverter.convert(applicantCounselAdded)));

        //Then
        verify(this.hearingApplicantCounselRepository).saveAndFlush(ahearingArgumentCaptor.capture());

        final HearingApplicantCounsel savedHearing = ahearingArgumentCaptor.getValue();
        assertThat(savedHearing, Matchers.is(hearingApplicantCounsel));

    }

    @Test
    public void shouldUpdateApplicantCounselOnUpdateEvent() {

        //Given
        ApplicantCounsel applicantCounsel = new ApplicantCounsel(
                Arrays.asList(UUID.randomUUID()),
                Arrays.asList(LocalDate.now()),
                STRING.next(),
                randomUUID(),
                STRING.next(),
                null,
                STRING.next(),
                STRING.next()
        );

        final ApplicantCounselUpdated applicantCounselUpdated = new ApplicantCounselUpdated(applicantCounsel, randomUUID());

        final Hearing hearing = new Hearing();
        hearing.setId(applicantCounselUpdated.getHearingId());

        HearingApplicantCounsel hearingApplicantCounsel = new HearingApplicantCounsel();
        hearingApplicantCounsel.setId(new HearingSnapshotKey(randomUUID(), hearing.getId()));
        hearingApplicantCounsel.setHearing(hearing);
        hearingApplicantCounsel.setPayload(getEntityPayload(applicantCounsel));

        when(this.hearingRepository.findBy(applicantCounselUpdated.getHearingId())).thenReturn(hearing);
        when(this.hearingApplicantCounselJPAMapper.toJPA(hearing, applicantCounsel)).thenReturn(hearingApplicantCounsel);

        //When
        this.applicantCounselEventListener.applicantCounselUpdated(envelopeFrom(metadataWithRandomUUID("hearing.applicant-counsel-updated"),
                objectToJsonObjectConverter.convert(applicantCounselUpdated)));

        //Then
        verify(this.hearingApplicantCounselRepository).saveAndFlush(ahearingArgumentCaptor.capture());

        final HearingApplicantCounsel savedHearing = ahearingArgumentCaptor.getValue();
        assertThat(savedHearing, Matchers.is(hearingApplicantCounsel));

    }

    @Test
    public void shouldRemoveApplicantCounselOnRemoveEvent() {

        //Given
        ApplicantCounsel applicantCounsel = new ApplicantCounsel(
                Arrays.asList(UUID.randomUUID()),
                Arrays.asList(LocalDate.now()),
                STRING.next(),
                randomUUID(),
                STRING.next(),
                null,
                STRING.next(),
                STRING.next()
        );

        final ApplicantCounselRemoved applicantCounselRemoved = new ApplicantCounselRemoved(randomUUID(), applicantCounsel.getId());

        final Hearing hearing = new Hearing();
        hearing.setId(applicantCounselRemoved.getHearingId());

        HearingApplicantCounsel hearingApplicantCounsel = new HearingApplicantCounsel();
        hearingApplicantCounsel.setId(new HearingSnapshotKey(applicantCounselRemoved.getId(), hearing.getId()));
        hearingApplicantCounsel.setHearing(hearing);
        hearingApplicantCounsel.setPayload(getEntityPayload(applicantCounsel));

        hearing.setApplicantCounsels(Collections.singleton(hearingApplicantCounsel));
        when(this.hearingRepository.findBy(applicantCounselRemoved.getHearingId())).thenReturn(hearing);

        //When
        this.applicantCounselEventListener.applicantCounselRemoved(envelopeFrom(metadataWithRandomUUID("hearing.applicant-counsel-removed"),
                objectToJsonObjectConverter.convert(applicantCounselRemoved)));

        //Then
        verify(this.hearingApplicantCounselRepository).saveAndFlush(ahearingArgumentCaptor.capture());

        final HearingApplicantCounsel savedHearing = ahearingArgumentCaptor.getValue();
        assertThat(savedHearing, Matchers.is(hearingApplicantCounsel));

    }

    private JsonNode getEntityPayload(ApplicantCounsel applicantCounsel) {

        final ObjectMapper mapper = new ObjectMapperProducer().objectMapper();

        return mapper.valueToTree(applicantCounsel);

    }
}
