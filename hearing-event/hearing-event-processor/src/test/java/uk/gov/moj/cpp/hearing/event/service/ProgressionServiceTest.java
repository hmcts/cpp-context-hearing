package uk.gov.moj.cpp.hearing.event.service;

import static java.util.UUID.randomUUID;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.json.Json;
import javax.json.JsonObject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProgressionServiceTest {

    public static final String SEARCH_APPLICATION = "progression.query.application-only";

    @Mock
    private Sender sender;
    @InjectMocks
    private ProgressionService progressionService;
    @Captor
    private ArgumentCaptor<Envelope> envelopeArgumentCaptor;
    @Mock
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;
    @Mock
    private Function<Object, JsonEnvelope> objectJsonEnvelopeFunction;
    @Mock
    private Requester requester;
    @Mock
    private ArgumentCaptor<Envelope<JsonObject>> envelopeCaptor;


    @Test
    public void shouldGetApplicationStatus() {
        final UUID applicationId = randomUUID();
        final JsonEnvelope envelope = mock(JsonEnvelope.class);
        final Metadata metadata = JsonEnvelope.metadataBuilder().withId(randomUUID()).withName(SEARCH_APPLICATION).build();

       final JsonObject courtApplicationObj = createObjectBuilder().add("courtApplication",
                        createObjectBuilder().add("id", applicationId.toString())
                                .add("applicant", createObjectBuilder().add("id", randomUUID().toString()))
                                .add("applicationStatus", "FINALISED")
                                .build()).build();

        when(envelope.metadata()).thenReturn(metadata);
        when(requester.request(any(Envelope.class), eq(JsonObject.class))).thenReturn(Envelope.envelopeFrom(metadata, courtApplicationObj));
        final Optional<JsonObject> courtApplication = progressionService.getApplicationDetails(envelope, applicationId);
        verify(requester, times(1)).request(any(Envelope.class), eq(JsonObject.class));
        assertThat(courtApplication.get(), notNullValue());
    }

}