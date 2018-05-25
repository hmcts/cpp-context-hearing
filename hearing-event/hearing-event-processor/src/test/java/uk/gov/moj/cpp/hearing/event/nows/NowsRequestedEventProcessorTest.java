package uk.gov.moj.cpp.hearing.event.nows;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.fileservice.api.FileServiceException;
import uk.gov.justice.services.fileservice.api.FileStorer;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.nows.NowsMaterialStatusType;
import uk.gov.moj.cpp.hearing.event.nows.service.DocmosisService;
import uk.gov.moj.cpp.hearing.event.nows.service.exception.DocumentGenerationException;
import uk.gov.moj.cpp.hearing.event.nows.service.exception.FileUploadException;
import uk.gov.moj.cpp.hearing.nows.events.NowsMaterialStatusUpdated;
import uk.gov.moj.cpp.hearing.nows.events.NowsRequested;

import javax.json.JsonObject;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloper;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.metadata;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payloadIsJson;
import static uk.gov.justice.services.test.utils.core.messaging.JsonEnvelopeBuilder.envelopeFrom;

public class NowsRequestedEventProcessorTest {

    @InjectMocks
    private NowsRequestedEventProcessor nowsRequestedEventProcessor;

    @Mock
    private Sender sender;

    @Mock
    private Requester requester;

    @Mock
    private JsonEnvelope responseEnvelope;

    @Mock
    private DocmosisService docmosisService;

    @Mock
    private FileStorer fileStorer;

    @Spy
    private final Enveloper enveloper = createEnveloper();

    @Captor
    private ArgumentCaptor<JsonEnvelope> envelopeArgumentCaptor;

    @Captor
    private ArgumentCaptor<InputStream> inputStreamArgumentCaptor;

    @Captor
    private ArgumentCaptor<JsonObject> jsonObjectArgumentCaptor;

    @Spy
    private final ObjectMapper objectMapper = new ObjectMapperProducer().objectMapper();

    @Spy
    @InjectMocks
    private final JsonObjectToObjectConverter jsonObjectToObjectConverter = new JsonObjectToObjectConverter();

    @Spy
    @InjectMocks
    private final ObjectToJsonObjectConverter objectToJsonObjectConverter = new ObjectToJsonObjectConverter();

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void shouldGenerateNowAndStoreInFileStore() throws IOException, FileServiceException {

        final InputStream is = NowsRequestedToOrderConvertorTest.class
                .getResourceAsStream("/data/hearing.events.nows-requested.json");
        NowsRequested nowsRequested = new ObjectMapperProducer().objectMapper().readValue(is, NowsRequested.class);

        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("hearing.events.nows-requested"),
                objectToJsonObjectConverter.convert(nowsRequested));

        byte[] bytesIn = new byte[2];
        when(docmosisService.generateDocument(any(), any(), any())).thenReturn(bytesIn);
        this.nowsRequestedEventProcessor.processNowsRequested(event);

        verify(this.sender).send(this.envelopeArgumentCaptor.capture());
        verify(this.fileStorer).store(jsonObjectArgumentCaptor.capture(), inputStreamArgumentCaptor.capture());

        assertThat(envelopeArgumentCaptor.getValue(), jsonEnvelope(
                metadata().withName("public.hearing.events.nows-requested"),
                payloadIsJson(allOf(withJsonPath("$.hearing.id", is(nowsRequested.getHearing().getId().toString())),
                        withJsonPath("$.hearing.hearingType",
                                is(nowsRequested.getHearing().getHearingType().toString()))))));

        assertThat(jsonObjectArgumentCaptor.getValue().getString("fileName"),
                is(startsWith(nowsRequested.getHearing().getNowTypes().get(0).getDescription())));
        assertThat(inputStreamArgumentCaptor.getValue().read(new byte[2]), is(bytesIn.length));
    }

    @Test
    public void shouldNotGenerateNowOnDocumentGenerationException() throws IOException, FileServiceException {

        final InputStream is = NowsRequestedToOrderConvertorTest.class
                .getResourceAsStream("/data/hearing.events.nows-requested.json");
        NowsRequested nowsRequested = new ObjectMapperProducer().objectMapper().readValue(is, NowsRequested.class);

        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("hearing.events.nows-requested"),
                objectToJsonObjectConverter.convert(nowsRequested));

        expectedException.expect(DocumentGenerationException.class);
        when(docmosisService.generateDocument(any(), any(), any())).thenThrow(new DocumentGenerationException());
        this.nowsRequestedEventProcessor.processNowsRequested(event);

        verifyNoMoreInteractions(this.sender);
        verifyNoMoreInteractions(this.fileStorer);
    }

    @Test
    public void shouldNotGenerateNowOnFileUploadException() throws IOException, FileServiceException {

        final InputStream is = NowsRequestedToOrderConvertorTest.class
                .getResourceAsStream("/data/hearing.events.nows-requested.json");
        NowsRequested nowsRequested = new ObjectMapperProducer().objectMapper().readValue(is, NowsRequested.class);

        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("hearing.events.nows-requested"),
                objectToJsonObjectConverter.convert(nowsRequested));

        expectedException.expect(FileUploadException.class);
        byte[] bytesIn = new byte[2];
        when(docmosisService.generateDocument(any(), any(), any())).thenReturn(bytesIn);
        doThrow(new FileUploadException()).when(fileStorer).store(any(), any());
        this.nowsRequestedEventProcessor.processNowsRequested(event);

        verifyNoMoreInteractions(this.sender);
        verify(this.fileStorer).store(jsonObjectArgumentCaptor.capture(), inputStreamArgumentCaptor.capture());
        assertThat(inputStreamArgumentCaptor.getValue().read(new byte[2]), is(bytesIn.length));
    }

    @Test
    public void shouldRaiseAnPublicEventNowsMaterialStatusWasUpdated() {

        final NowsMaterialStatusUpdated nowsMaterialStatusUpdated = new NowsMaterialStatusUpdated(UUID.randomUUID(),
                UUID.randomUUID(), NowsMaterialStatusType.GENERATED);

        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("hearing.events.nows-material-status-updated"),
                objectToJsonObjectConverter.convert(nowsMaterialStatusUpdated));

        this.nowsRequestedEventProcessor.propagateNowsMaterialStatusUpdated(event);

        final ArgumentCaptor<JsonEnvelope> jsonEnvelopeCaptor = ArgumentCaptor.forClass(JsonEnvelope.class);

        verify(sender).send(jsonEnvelopeCaptor.capture());

        assertThat(jsonEnvelopeCaptor.getValue().metadata().name(), is("public.hearing.events.nows-material-status-updated"));
        assertThat(jsonEnvelopeCaptor.getValue().payloadAsJsonObject().getString("materialId"), is(nowsMaterialStatusUpdated.getMaterialId().toString()));

    }
}