package uk.gov.moj.cpp.hearing.event.service;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.OffenceBailStatus;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.OffenceBailStatusResponse;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class OffenceServiceTest {

    @Mock
    private Requester requester;

    @InjectMocks
    private OffenceService offenceService;

    @Test
    public void shouldReturnOffenceBailStatusesForDefendant() {
        final UUID defendantId = randomUUID();
        final OffenceBailStatus offenceBailStatus = new OffenceBailStatus(randomUUID(), randomUUID(), "C", "Remanded into Custody");
        final OffenceBailStatusResponse response = new OffenceBailStatusResponse();
        response.setOffenceBailStatuses(singletonList(offenceBailStatus));

        stubRequesterResponse(response);

        final List<OffenceBailStatus> result = offenceService.getOffenceBailStatus(defendantId);

        assertThat(result, hasSize(1));
        assertThat(result.get(0).getBailStatusCode(), is("C"));
    }

    @Test
    public void shouldReturnEmptyListWhenNoOffenceBailStatusesFound() {
        final UUID defendantId = randomUUID();
        final OffenceBailStatusResponse response = new OffenceBailStatusResponse();
        response.setOffenceBailStatuses(emptyList());

        stubRequesterResponse(response);

        final List<OffenceBailStatus> result = offenceService.getOffenceBailStatus(defendantId);

        assertThat(result, hasSize(0));
    }

    @Test
    public void shouldSendRequestWithCorrectActionNameAndDefendantIdPayload() {
        final UUID defendantId = randomUUID();
        final OffenceBailStatusResponse response = new OffenceBailStatusResponse();
        response.setOffenceBailStatuses(emptyList());

        final ArgumentCaptor<JsonEnvelope> envelopeCaptor = ArgumentCaptor.forClass(JsonEnvelope.class);
        final Envelope<OffenceBailStatusResponse> mockEnvelope = mock(Envelope.class);
        when(mockEnvelope.payload()).thenReturn(response);
        when(requester.requestAsAdmin(envelopeCaptor.capture(), eq(OffenceBailStatusResponse.class))).thenReturn(mockEnvelope);

        offenceService.getOffenceBailStatus(defendantId);

        final JsonEnvelope sentEnvelope = envelopeCaptor.getValue();
        assertThat(sentEnvelope.metadata().name(), is("hearing.offence-bail-status-for-defendant"));
        assertThat(sentEnvelope.payloadAsJsonObject().getString("defendantId"), is(defendantId.toString()));
    }

    @Test
    public void shouldScopeEachRequestToItsOwnDefendantId() {
        final UUID defendantId1 = randomUUID();
        final UUID defendantId2 = randomUUID();
        final OffenceBailStatusResponse emptyResponse = new OffenceBailStatusResponse();
        emptyResponse.setOffenceBailStatuses(emptyList());

        final ArgumentCaptor<JsonEnvelope> envelopeCaptor = ArgumentCaptor.forClass(JsonEnvelope.class);
        final Envelope<OffenceBailStatusResponse> mockEnvelope = mock(Envelope.class);
        when(mockEnvelope.payload()).thenReturn(emptyResponse);
        when(requester.requestAsAdmin(envelopeCaptor.capture(), eq(OffenceBailStatusResponse.class))).thenReturn(mockEnvelope);

        offenceService.getOffenceBailStatus(defendantId1);
        offenceService.getOffenceBailStatus(defendantId2);

        assertThat(envelopeCaptor.getAllValues().get(0).payloadAsJsonObject().getString("defendantId"), is(defendantId1.toString()));
        assertThat(envelopeCaptor.getAllValues().get(1).payloadAsJsonObject().getString("defendantId"), is(defendantId2.toString()));
    }

    @SuppressWarnings("unchecked")
    private void stubRequesterResponse(final OffenceBailStatusResponse response) {
        final Envelope<OffenceBailStatusResponse> mockEnvelope = mock(Envelope.class);
        when(mockEnvelope.payload()).thenReturn(response);
        when(requester.requestAsAdmin(any(JsonEnvelope.class), eq(OffenceBailStatusResponse.class))).thenReturn(mockEnvelope);
    }
}
