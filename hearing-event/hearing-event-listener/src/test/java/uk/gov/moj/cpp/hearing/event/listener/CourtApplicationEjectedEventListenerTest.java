package uk.gov.moj.cpp.hearing.event.listener;

import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.moj.cpp.hearing.mapping.HearingJPAMapper;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.repository.HearingRepository;

import java.util.UUID;

import javax.json.Json;
import javax.json.JsonObject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CourtApplicationEjectedEventListenerTest {
    @Mock
    private HearingRepository hearingRepository;

    @Mock
    private HearingJPAMapper hearingJPAMapper;

    @InjectMocks
    private CourtApplicationEjectedEventListener courtApplicationEjectedEventListener;

    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;


    @BeforeEach
    public void setup() {
        setField(this.jsonObjectToObjectConverter, "objectMapper", new ObjectMapperProducer().objectMapper());
    }

    @Test
    public void shouldHandleCaseEjected() {
        //given
        final UUID hearingId = randomUUID();
        final Hearing hearing = new Hearing();
        hearing.setCourtApplicationsJson("{\"courtApplications\":[{id:\""+randomUUID()+"\"]}");
        when(hearingRepository.findBy(hearingId)).thenReturn(hearing);
        courtApplicationEjectedEventListener.courtApplicationEjected(getCourtApplicationEjectedEventEnvelope(hearingId));
        //then
        final ArgumentCaptor<Hearing> hearingArgumentCaptor = ArgumentCaptor.forClass(Hearing.class);

        verify(hearingRepository, times(1)).save(hearingArgumentCaptor.capture());

    }

    private JsonEnvelope getCourtApplicationEjectedEventEnvelope(final UUID hearingId) {
        JsonObject payload = Json.createObjectBuilder()
                .add("applicationId", randomUUID().toString())
                .add("hearingIds", Json.createArrayBuilder().add(hearingId.toString()))
                .build();
        return envelopeFrom((Metadata) null, payload);
    }
}
