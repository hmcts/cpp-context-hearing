package uk.gov.justice.api.resource;

import static java.util.Optional.of;
import static java.util.UUID.fromString;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.messaging.JsonEnvelopeBuilder.envelope;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUIDAndName;
import static uk.gov.moj.cpp.hearing.query.api.util.FileUtil.getPayload;

import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.core.dispatcher.SystemUserProvider;
import uk.gov.justice.services.core.interceptor.InterceptorChainProcessor;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.query.view.HearingEventQueryView;
import uk.gov.moj.cpp.system.documentgenerator.client.DocumentGeneratorClient;
import uk.gov.moj.cpp.system.documentgenerator.client.DocumentGeneratorClientProducer;

import java.io.IOException;
import java.time.LocalDate;
import java.util.UUID;

import javax.json.JsonObject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
public class DefaultQueryApiHearingEventLogReportResourceTest {

    @InjectMocks
    DefaultQueryApiHearingsEventLogExtractResource defaultQueryApiHearingsEventLogExtractResource;

    @Mock
    HearingEventQueryView hearingEventQueryView;

    @Mock
    private SystemUserProvider systemUserProvider;

    @Mock
    private DocumentGeneratorClientProducer documentGeneratorClientProducer;

    @Mock
    private InterceptorChainProcessor interceptorChainProcessor;

    @Captor
    private ArgumentCaptor<JsonObject> jsonObjectArgumentCaptor;

    @Test
    public void shouldGetHaringEventLogExtractContent() throws IOException {

        final String caseId = randomUUID().toString();
        final String hearingId = randomUUID().toString();
        final String applicationId = randomUUID().toString();
        final String hearingDate = LocalDate.now().toString();
        final UUID userId = fromString(randomUUID().toString());

        final String payload = getPayload("hearing.get-hearing-event-log-for-documents.json");
        final String pdfContent = "PDF Content";

        final Envelope responseEnvelope = envelope()
                .with(metadataWithRandomUUIDAndName())
                .withPayloadFrom(new StringToJsonObjectConverter().convert(payload))
                .build();

        final DocumentGeneratorClient documentGeneratorClient = mock(DocumentGeneratorClient.class);
        final JsonEnvelope jsonEnvelope = mock(JsonEnvelope.class);
        final byte[] documentGeneratorClientResponse = pdfContent.getBytes();

        when(hearingEventQueryView.getHearingEventLogForDocuments(any())).thenReturn(responseEnvelope);
        when(documentGeneratorClientProducer.documentGeneratorClient()).thenReturn(documentGeneratorClient);
        when(systemUserProvider.getContextSystemUserId()).thenReturn(of(UUID.randomUUID()));
        when(documentGeneratorClient.generatePdfDocument(any(), any(), any())).thenReturn(documentGeneratorClientResponse);

        defaultQueryApiHearingsEventLogExtractResource.getHearingsEventLogExtract(hearingId, caseId, applicationId, hearingDate,  userId);

        verify(documentGeneratorClient).generatePdfDocument(jsonObjectArgumentCaptor.capture(), anyString(), any());
        assertThat(payload, is(jsonObjectArgumentCaptor.getValue().toString()));
    }

}
