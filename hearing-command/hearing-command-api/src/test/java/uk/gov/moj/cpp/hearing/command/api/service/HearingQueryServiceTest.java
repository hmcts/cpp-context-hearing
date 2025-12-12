package uk.gov.moj.cpp.hearing.command.api.service;

import static java.util.UUID.randomUUID;
import static javax.json.Json.createObjectBuilder;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonEnvelope.metadataBuilder;

import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.UUID;

import javax.json.JsonObject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
public class HearingQueryServiceTest {

    @InjectMocks
    private HearingQueryService hearingQueryService;

    @Mock
    private Requester requester;

    @Test
    public void shouldReturnValidResultDefinition() {
        final UUID hearingId = randomUUID();
        final JsonObject jsonObject = createObjectBuilder()
                .add("hearingId", hearingId.toString()).build();
        final JsonEnvelope jsonEnvelope = envelopeFrom(
                metadataBuilder().withId(randomUUID()).withName("hearing.share-days-results").build(),
                jsonObject);
        hearingQueryService.validateIfUserHasAccessToHearing(jsonEnvelope);
        verify(requester, times(1)).request(any(),any());
    }
}