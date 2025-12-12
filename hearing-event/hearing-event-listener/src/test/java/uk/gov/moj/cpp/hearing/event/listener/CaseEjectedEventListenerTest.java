package uk.gov.moj.cpp.hearing.event.listener;

import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataOf;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
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
public class CaseEjectedEventListenerTest {
    @Mock
    private HearingRepository hearingRepository;

    @Mock
    private HearingJPAMapper hearingJPAMapper;

    @InjectMocks
    private CaseEjectedEventListener caseEjectedEventListener;

    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @BeforeEach
    public void setup() {
        setField(this.jsonObjectToObjectConverter, "objectMapper", new ObjectMapperProducer().objectMapper());
    }

    @Test
    public void shouldHandleCaseEjected(){
        //given
        final UUID hearingId = UUID.randomUUID();
        when(hearingRepository.findBy(hearingId)).thenReturn(new Hearing());
        caseEjectedEventListener.caseEjected(getCaseEjectedEventEnvelope(hearingId));
        //then
        final ArgumentCaptor<Hearing> hearingArgumentCaptor = ArgumentCaptor.forClass(Hearing.class);

        verify(hearingRepository, times(1)).save(hearingArgumentCaptor.capture());

    }
    private JsonEnvelope getCaseEjectedEventEnvelope(final UUID hearingId) {
        JsonObject payload = Json.createObjectBuilder()
                .add("prosecutionCaseId", randomUUID().toString())
                .add("hearingIds", Json.createArrayBuilder().add(hearingId.toString()))
                .build();
        final Metadata metadata = metadataOf(randomUUID(), "event-name").build();

        return envelopeFrom(metadata, payload);
    }
}
