package uk.gov.moj.cpp.hearing.event.nows;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloper;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.generateNowsRequestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.core.courts.CreateNowsRequest;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.event.delegates.NowsDelegate;
import uk.gov.moj.cpp.hearing.nows.events.NowsMaterialStatusUpdated;
import uk.gov.moj.cpp.hearing.nows.events.NowsRequested;

import java.util.UUID;

import javax.print.DocFlavor;

@RunWith(MockitoJUnitRunner.class)
public class NowsRequestedEventProcessorTest {

    public static final String USER_ID = UUID.randomUUID().toString();
    public static final String ACCOUNT_NUMBER = "201366829";


    @Mock
    private Sender sender;

    @Spy
    private Enveloper enveloper = createEnveloper();

    @Captor
    private ArgumentCaptor<JsonEnvelope> envelopeArgumentCaptor;

    @Captor
    private ArgumentCaptor<CreateNowsRequest> nowsRequestArgumentCaptor;

    @Spy
    private final ObjectMapper objectMapper = new ObjectMapperProducer().objectMapper();

    @Spy
    @InjectMocks
    private ObjectToJsonObjectConverter objectToJsonObjectConverter = new ObjectToJsonObjectConverter();

    @Spy
    @InjectMocks
    private final JsonObjectToObjectConverter jsonObjectToObjectConverter = new JsonObjectToObjectConverter();

    @Mock
    private NowsDelegate nowsDelegate;

    @InjectMocks
    private NowsRequestedEventProcessor nowsRequestedEventProcessor;


    @Test
    public void testProcessNowsRequested_shouldCallNowsDelegate() {
        //TODO update with generated Nowsrequest / Nows classes as part of GPE-6293
        final UUID defendantId = UUID.randomUUID();
        final String accountNumber = "12345678";
        final CreateNowsRequest nowsRequest = generateNowsRequestTemplate(defendantId);
        final UUID requestId = nowsRequest.getNows().get(0).getId();
        final NowsRequested nowsRequested = new NowsRequested(requestId, nowsRequest, accountNumber);
        final JsonEnvelope envelope = envelopeFrom(
                metadataWithRandomUUID("hearing.events.nows-requested"),
                objectToJsonObjectConverter.convert(nowsRequested)
        );
        nowsRequestedEventProcessor.processNowsRequested(envelope);

        verify(nowsDelegate).sendNows(eq(sender), eq(envelope), nowsRequestArgumentCaptor.capture());

        final CreateNowsRequest capturedNowsRequest = nowsRequestArgumentCaptor.getValue();
        assertThat(capturedNowsRequest.getNows().get(0).getId().toString(), is(nowsRequest.getNows().get(0).getId().toString()));
        assertThat(capturedNowsRequest.getNowTypes().get(0).getId().toString(), is(nowsRequest.getNowTypes().get(0).getId().toString()));
        assertThat(capturedNowsRequest.getSharedResultLines().get(0).getId().toString(), is(nowsRequest.getSharedResultLines().get(0).getId().toString()));
        assertThat(capturedNowsRequest.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getId().toString(),
                is(nowsRequest.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getId().toString()));
    }


    @Test
    public void shouldRaiseAnPublicEventNowsMaterialStatusWasUpdated() {

        final NowsMaterialStatusUpdated nowsMaterialStatusUpdated = new NowsMaterialStatusUpdated(UUID.randomUUID(),
                UUID.randomUUID(), "generated");

        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("hearing.events.nows-material-status-updated").withUserId(USER_ID),
                objectToJsonObjectConverter.convert(nowsMaterialStatusUpdated));

        this.nowsRequestedEventProcessor.propagateNowsMaterialStatusUpdated(event);

        final ArgumentCaptor<JsonEnvelope> jsonEnvelopeCaptor = ArgumentCaptor.forClass(JsonEnvelope.class);

        verify(sender).send(jsonEnvelopeCaptor.capture());

        assertThat(jsonEnvelopeCaptor.getValue().metadata().name(), is("public.hearing.events.nows-material-status-updated"));
        assertThat(jsonEnvelopeCaptor.getValue().payloadAsJsonObject().getString("materialId"), is(nowsMaterialStatusUpdated.getMaterialId().toString()));

    }
}