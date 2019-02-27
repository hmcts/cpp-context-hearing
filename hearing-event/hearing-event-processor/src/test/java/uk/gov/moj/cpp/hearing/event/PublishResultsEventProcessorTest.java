package uk.gov.moj.cpp.hearing.event;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.moj.cpp.hearing.event.NowsTemplates.basicNowsTemplate;
import static uk.gov.moj.cpp.hearing.event.NowsTemplates.resultsSharedTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import uk.gov.justice.core.courts.CreateNowsRequest;
import uk.gov.justice.core.courts.FinancialOrderDetails;
import uk.gov.justice.core.courts.JurisdictionType;
import uk.gov.justice.core.courts.Now;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared;
import uk.gov.moj.cpp.hearing.event.delegates.AdjournHearingDelegate;
import uk.gov.moj.cpp.hearing.event.delegates.NowsDelegate;
import uk.gov.moj.cpp.hearing.event.delegates.PublishResultsDelegate;
import uk.gov.moj.cpp.hearing.event.delegates.SaveNowVariantsDelegate;
import uk.gov.moj.cpp.hearing.event.delegates.UpdateResultLineStatusDelegate;
import uk.gov.moj.cpp.hearing.event.nows.NowsGenerator;

import java.util.Collections;
import java.util.List;

public class PublishResultsEventProcessorTest {

    @Spy
    private final ObjectMapper objectMapper = new ObjectMapperProducer().objectMapper();

    @Spy
    @InjectMocks
    private final JsonObjectToObjectConverter jsonObjectToObjectConverter = new JsonObjectToObjectConverter();

    @Spy
    @InjectMocks
    private final ObjectToJsonObjectConverter objectToJsonObjectConverter = new ObjectToJsonObjectConverter();

    @Mock
    private Sender sender;

    @Mock
    private NowsGenerator nowsGenerator;

    @Mock
    private NowsDelegate nowsDelegate;

    @Mock
    private AdjournHearingDelegate adjournHearingDelegate;

    @Mock
    private SaveNowVariantsDelegate saveNowVariantsDelegate;

    @Mock
    private UpdateResultLineStatusDelegate updateResultLineStatusDelegate;

    @Mock
    private PublishResultsDelegate publishResultsDelegate;

    @InjectMocks
    private PublishResultsEventProcessor publishResultsEventProcessor;

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void resultsShared() {

        final ResultsShared resultsShared = resultsSharedTemplate();

        final List<Now> nows = basicNowsTemplate();

        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("hearing.results-shared"),
                objectToJsonObjectConverter.convert(resultsShared));

        when(nowsGenerator.createNows(eq(event), Mockito.any(), Mockito.any())).thenReturn(nows);

        when(jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), ResultsShared.class)).thenReturn(resultsShared);

        when(saveNowVariantsDelegate.saveNowsVariants(sender, event, nows, resultsShared)).thenReturn(resultsShared.getVariantDirectory());

        when(nowsDelegate.generateNows(event, nows, resultsShared)).thenReturn(CreateNowsRequest.createNowsRequest().withNows(nows).build());

        publishResultsEventProcessor.resultsShared(event);

        verify(nowsDelegate).generateNows(event, nows, resultsShared);

        verify(saveNowVariantsDelegate).saveNowsVariants(sender, event, nows, resultsShared);

        verify(publishResultsDelegate).shareResults(event, sender, event, resultsShared, resultsShared.getVariantDirectory());

        verify(updateResultLineStatusDelegate).updateResultLineStatus(sender, event, resultsShared);
    }

    @Test
    public void resultsShared_withNoNewNows() {

        final ResultsShared resultsShared = resultsSharedTemplate();

        final List<Now> nows = Collections.emptyList();

        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("hearing.results-shared"),
                objectToJsonObjectConverter.convert(resultsShared));

        when(nowsGenerator.createNows(eq(event), Mockito.any(), Mockito.any())).thenReturn(nows);

        when(jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), ResultsShared.class)).thenReturn(resultsShared);

        when(saveNowVariantsDelegate.saveNowsVariants(sender, event, nows, resultsShared)).thenReturn(resultsShared.getVariantDirectory());

        publishResultsEventProcessor.resultsShared(event);

        verifyNoMoreInteractions(nowsDelegate, saveNowVariantsDelegate);

        verify(publishResultsDelegate).shareResults(event, sender, event, resultsShared, Collections.emptyList());

        verify(updateResultLineStatusDelegate).updateResultLineStatus(sender, event, resultsShared);

        verify(adjournHearingDelegate).execute(resultsShared, event);
    }

    @Captor
    ArgumentCaptor<CreateNowsRequest> createNowsRequestArgumentCaptor;

    @Captor
    ArgumentCaptor<Sender> senderArgumentCaptor;

    @Captor
    ArgumentCaptor<JsonEnvelope> eventArgumentCaptor;

    @Test
    public void resultsSharedFinancialCrownCourt() {
        resultsSharedFinancial(true);
    }

    @Test
    public void resultsSharedFinancialMagistratesCourt() {
        resultsSharedFinancial(false);
    }

    public void resultsSharedFinancial(boolean crownCourt) {

        final ResultsShared resultsShared = resultsSharedTemplate();
        resultsShared.getHearing().setJurisdictionType(crownCourt ? JurisdictionType.CROWN : JurisdictionType.MAGISTRATES);

        final List<Now> nows = basicNowsTemplate();
        nows.get(0).setFinancialOrders(FinancialOrderDetails.financialOrderDetails()
                .withAccountReference("TBA")
                .build());

        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("hearing.results-shared"),
                objectToJsonObjectConverter.convert(resultsShared));

        when(nowsGenerator.createNows(eq(event), Mockito.any(), Mockito.any())).thenReturn(nows);

        when(jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), ResultsShared.class)).thenReturn(resultsShared);

        when(saveNowVariantsDelegate.saveNowsVariants(sender, event, nows, resultsShared)).thenReturn(resultsShared.getVariantDirectory());

        when(nowsDelegate.generateNows(event, nows, resultsShared)).thenReturn(CreateNowsRequest.createNowsRequest().withNows(nows).build());

        publishResultsEventProcessor.resultsShared(event);

        verify(nowsDelegate).generateNows(event, nows, resultsShared);

        verify(saveNowVariantsDelegate).saveNowsVariants(sender, event, nows, resultsShared);

        verify(publishResultsDelegate).shareResults(event, sender, event, resultsShared, resultsShared.getVariantDirectory());

        verify(updateResultLineStatusDelegate).updateResultLineStatus(sender, event, resultsShared);

        if (crownCourt) {
            verify(nowsDelegate).sendNows(senderArgumentCaptor.capture(), eventArgumentCaptor.capture(), createNowsRequestArgumentCaptor.capture());
        } else {
            verify(nowsDelegate).sendPendingNows(senderArgumentCaptor.capture(), eventArgumentCaptor.capture(), createNowsRequestArgumentCaptor.capture());
        }
    }


}