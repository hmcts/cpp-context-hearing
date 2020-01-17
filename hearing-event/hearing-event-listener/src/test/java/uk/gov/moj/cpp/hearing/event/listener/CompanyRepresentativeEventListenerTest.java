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
import uk.gov.justice.core.courts.CompanyRepresentative;
import uk.gov.justice.hearing.courts.Position;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.moj.cpp.hearing.domain.event.CompanyRepresentativeAdded;
import uk.gov.moj.cpp.hearing.domain.event.CompanyRepresentativeRemoved;
import uk.gov.moj.cpp.hearing.domain.event.CompanyRepresentativeUpdated;
import uk.gov.moj.cpp.hearing.mapping.HearingCompanyRepresentativeJPAMapper;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingCompanyRepresentative;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.repository.HearingCompanyRepresentativeRepository;
import uk.gov.moj.cpp.hearing.repository.HearingRepository;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

@RunWith(MockitoJUnitRunner.class)
public class CompanyRepresentativeEventListenerTest {

    @Mock
    private HearingRepository hearingRepository;

    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @InjectMocks
    private CompanyRepresentativeEventListener companyRepresentativeEventListener;

    @Captor
    private ArgumentCaptor<HearingCompanyRepresentative> hearingArgumentCaptor;

    @Mock
    HearingCompanyRepresentativeJPAMapper hearingCompanyRepresentativeJPAMapper;

    @Mock
    HearingCompanyRepresentativeRepository hearingCompanyRepresentativeRepository;

    @Before
    public void setUp() {
        setField(this.jsonObjectToObjectConverter, "objectMapper", new ObjectMapperProducer().objectMapper());
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
    }

    @Test
    public void shouldAddCompanyRepresentative() {
        final CompanyRepresentative companyRepresentative = createCompanyRepresentative();
        final CompanyRepresentativeAdded companyRepresentativeAdded = new CompanyRepresentativeAdded(companyRepresentative, randomUUID());
        final Hearing hearing = new Hearing();
        hearing.setId(companyRepresentativeAdded.getHearingId());
        final HearingCompanyRepresentative hearingCompanyRepresentative = createHearingCompanyRepresentative(hearing, randomUUID(), companyRepresentative);

        when(this.hearingRepository.findBy(companyRepresentativeAdded.getHearingId())).thenReturn(hearing);
        when(this.hearingCompanyRepresentativeJPAMapper.toJPA(hearing, companyRepresentative)).thenReturn(hearingCompanyRepresentative);

        this.companyRepresentativeEventListener.companyRepresentativeAdded(envelopeFrom(metadataWithRandomUUID("hearing.company-representative-added"),
                objectToJsonObjectConverter.convert(companyRepresentativeAdded)));

        verify(this.hearingCompanyRepresentativeRepository).saveAndFlush(hearingArgumentCaptor.capture());
        final HearingCompanyRepresentative savedHearing = hearingArgumentCaptor.getValue();
        assertThat(savedHearing, is(hearingCompanyRepresentative));
    }

    @Test
    public void shouldUpdateCompanyRepresentative() {

        final CompanyRepresentative companyRepresentative = createCompanyRepresentative();
        final CompanyRepresentativeUpdated companyRepresentativeUpdated = new CompanyRepresentativeUpdated(companyRepresentative, randomUUID());
        final Hearing hearing = new Hearing();
        hearing.setId(companyRepresentativeUpdated.getHearingId());
        final HearingCompanyRepresentative hearingCompanyRepresentative = createHearingCompanyRepresentative(hearing, randomUUID(), companyRepresentative);

        when(this.hearingRepository.findBy(companyRepresentativeUpdated.getHearingId())).thenReturn(hearing);
        when(this.hearingCompanyRepresentativeJPAMapper.toJPA(hearing, companyRepresentative)).thenReturn(hearingCompanyRepresentative);

        this.companyRepresentativeEventListener.companyRepresentativeUpdated(envelopeFrom(metadataWithRandomUUID("hearing.company-representative-updated"),
                objectToJsonObjectConverter.convert(companyRepresentativeUpdated)));

        verify(this.hearingCompanyRepresentativeRepository).saveAndFlush(hearingArgumentCaptor.capture());

        final HearingCompanyRepresentative savedHearing = hearingArgumentCaptor.getValue();
        assertThat(savedHearing, is(hearingCompanyRepresentative));
    }

    @Test
    public void shouldRemoveCompanyRepresentative() {

        final CompanyRepresentative companyRepresentative = createCompanyRepresentative();
        final CompanyRepresentativeRemoved companyRepresentativeRemoved = new CompanyRepresentativeRemoved(randomUUID(), companyRepresentative.getId());
        final Hearing hearing = new Hearing();
        hearing.setId(companyRepresentativeRemoved.getHearingId());
        final HearingCompanyRepresentative hearingCompanyRepresentative = createHearingCompanyRepresentative(hearing, companyRepresentativeRemoved.getId(), companyRepresentative);
        hearing.setCompanyRepresentatives(Collections.singleton(hearingCompanyRepresentative));

        when(this.hearingRepository.findBy(companyRepresentativeRemoved.getHearingId())).thenReturn(hearing);

        this.companyRepresentativeEventListener.companyRepresentativeRemoved(envelopeFrom(metadataWithRandomUUID("hearing.company-representative-removed"),
                objectToJsonObjectConverter.convert(companyRepresentativeRemoved)));

        verify(this.hearingCompanyRepresentativeRepository).saveAndFlush(hearingArgumentCaptor.capture());

        final HearingCompanyRepresentative savedHearing = hearingArgumentCaptor.getValue();
        assertThat(savedHearing, Matchers.is(hearingCompanyRepresentative));
    }

    private JsonNode getEntityPayload(final CompanyRepresentative companyRepresentative) {
        final ObjectMapper mapper = new ObjectMapperProducer().objectMapper();
        return mapper.valueToTree(companyRepresentative);
    }

    private HearingCompanyRepresentative createHearingCompanyRepresentative(final Hearing hearing, final UUID companyRepresentativeId, final CompanyRepresentative companyRepresentative) {
        HearingCompanyRepresentative hearingCompanyRepresentative = new HearingCompanyRepresentative();
        hearingCompanyRepresentative.setId(new HearingSnapshotKey(companyRepresentativeId, hearing.getId()));
        hearingCompanyRepresentative.setHearing(hearing);
        hearingCompanyRepresentative.setPayload(getEntityPayload(companyRepresentative));
        return hearingCompanyRepresentative;
    }

    private CompanyRepresentative createCompanyRepresentative() {
        return CompanyRepresentative.companyRepresentative()
                .withId(randomUUID())
                .withTitle(STRING.next())
                .withFirstName(STRING.next())
                .withLastName(STRING.next())
                .withPosition(Position.DIRECTOR)
                .withDefendants(Arrays.asList(UUID.randomUUID()))
                .withAttendanceDays(Arrays.asList(LocalDate.now()))
                .build();
    }
}
