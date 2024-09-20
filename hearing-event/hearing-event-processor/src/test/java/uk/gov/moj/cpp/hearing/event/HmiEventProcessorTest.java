package uk.gov.moj.cpp.hearing.event;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.AllOf.allOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloper;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payloadIsJson;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import javax.inject.Inject;
import javax.json.Json;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;

@ExtendWith(MockitoExtension.class)
public class HmiEventProcessorTest {

    @Mock
    private Sender sender;

    @Captor
    private ArgumentCaptor<JsonEnvelope> senderJsonEnvelopeCaptor;

    @Spy
    private final Enveloper enveloper = createEnveloper();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter = new JsonObjectToObjectConverter(objectMapper);

    @InjectMocks
    private HmiEventProcessor hmiEventProcessor;


    @Test
    public void shouldDeleteHearingWhenCourtRoomNotExist(){
        final UUID hearingId = randomUUID();

        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("public.staginghmi.hearing-updated-from-hmi"),
                Json.createObjectBuilder().add("hearingId", hearingId.toString()).build());

        hmiEventProcessor.handleHearingUpdatedFromHmi(event);

        verify(sender).send(any());
    }

    @Test
    public void shouldNotDeleteHearingWhenCourtRoomExists(){
        final UUID hearingId = randomUUID();

        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("public.staginghmi.hearing-updated-from-hmi"),
                Json.createObjectBuilder().add("hearingId", hearingId.toString())
                        .add("courtRoomId", randomUUID().toString())
                        .build());

        hmiEventProcessor.handleHearingUpdatedFromHmi(event);

        verify(sender, never()).send(any());
    }

    @Test
    public void shouldDeleteHearingWhenHearingDeletedFromHmi() {
        final UUID hearingId = randomUUID();

        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("public.staginghmi.hearing-deleted-from-hmi"),
                Json.createObjectBuilder().add("hearingId", hearingId.toString()).build());

        hmiEventProcessor.handleHearingDeletedFromHmi(event);

        verify(sender).send(senderJsonEnvelopeCaptor.capture());
        assertThat(senderJsonEnvelopeCaptor.getValue().metadata().name(), is("hearing.command.mark-as-duplicate"));
        assertThat(senderJsonEnvelopeCaptor.getValue().payload(), is(payloadIsJson(allOf(
                withJsonPath("$.hearingId", equalTo(hearingId.toString())),
                withJsonPath("$.overwriteWithResults", equalTo(true))
        ))));
    }

}
