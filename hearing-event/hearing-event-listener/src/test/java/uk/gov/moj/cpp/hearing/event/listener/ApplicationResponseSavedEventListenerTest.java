package uk.gov.moj.cpp.hearing.event.listener;

import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;

import uk.gov.justice.core.courts.CourtApplicationResponse;
import uk.gov.justice.core.courts.CourtApplicationResponseType;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.moj.cpp.hearing.domain.event.application.ApplicationResponseSaved;
import uk.gov.moj.cpp.hearing.mapping.HearingJPAMapper;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.repository.HearingRepository;

import java.time.LocalDate;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationResponseSavedEventListenerTest {

    @InjectMocks
    private ApplicationResponseSavedEventListener applicationResponseSavedEventListener;

    @Mock
    private HearingRepository hearingRepository;

    @Mock
    private HearingJPAMapper hearingJPAMapper;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @Before
    public void setup() {
        setField(this.jsonObjectToObjectConverter, "objectMapper", new ObjectMapperProducer().objectMapper());
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
    }

    @Test
    public void shouldSaveApplicationResponse() {
        final ApplicationResponseSaved applicationResponseSaved = ApplicationResponseSaved.applicationResponseSaved()
                .setApplicationPartyId(randomUUID())
                .setCourtApplicationResponse(CourtApplicationResponse.courtApplicationResponse()
                        .withApplicationResponseDate(LocalDate.now())
                        .withOriginatingHearingId(randomUUID())
                        .withApplicationId(randomUUID())
                        .withApplicationResponseType(CourtApplicationResponseType.courtApplicationResponseType()
                                .withDescription("Admitted")
                                .withId(UUID.randomUUID())
                                .withSequence(1).build())
                        .build());

        Hearing hearing = new Hearing();
        hearing.setCourtApplicationsJson("application json");
        final String expectedUpdatedCourtApplicationJson = "application response json";
        when(hearingRepository.findBy(applicationResponseSaved.getCourtApplicationResponse().getOriginatingHearingId())).thenReturn(hearing);
        when(hearingJPAMapper.saveApplicationResponse(hearing.getCourtApplicationsJson(), applicationResponseSaved.getCourtApplicationResponse(), applicationResponseSaved.getApplicationPartyId())).thenReturn(expectedUpdatedCourtApplicationJson);

        applicationResponseSavedEventListener.applicationResponseSave(envelopeFrom((Metadata) null, objectToJsonObjectConverter.convert(applicationResponseSaved)));
        final ArgumentCaptor<Hearing> hearingExArgumentCaptor = ArgumentCaptor.forClass(Hearing.class);

        verify(hearingRepository, times(1)).save(hearingExArgumentCaptor.capture());
        final String updatedCourtApplicationsJson = hearingExArgumentCaptor.getValue().getCourtApplicationsJson();
        assertThat(updatedCourtApplicationsJson, Is.is(expectedUpdatedCourtApplicationJson));
    }

}