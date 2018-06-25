package uk.gov.moj.cpp.hearing.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared;
import uk.gov.moj.cpp.hearing.event.delegates.GenerateNowsDelegate;
import uk.gov.moj.cpp.hearing.event.delegates.PublishResultsDelegate;
import uk.gov.moj.cpp.hearing.event.delegates.SaveNowVariantsDelegate;
import uk.gov.moj.cpp.hearing.event.delegates.UpdateResultLineStatusDelegate;
import uk.gov.moj.cpp.hearing.event.nows.NowsGenerator;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.Nows;

import java.util.List;

import static org.mockito.Mockito.verify;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.moj.cpp.hearing.event.NowsTemplates.basicNowsTemplate;
import static uk.gov.moj.cpp.hearing.event.NowsTemplates.resultsSharedTemplate;

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
    private GenerateNowsDelegate generateNowsDelegate;

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

        final List<Nows> nows = basicNowsTemplate();

        Mockito.when(nowsGenerator.createNows(Mockito.any())).thenReturn(nows);

        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("hearing.results-shared"),
                objectToJsonObjectConverter.convert(resultsShared));

        Mockito.when(jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), ResultsShared.class)).thenReturn(resultsShared);

        publishResultsEventProcessor.resultsShared(event);

        verify(generateNowsDelegate).generateNows(sender, event, nows, resultsShared);

        verify(saveNowVariantsDelegate).saveNowsVariants(sender, event, nows, resultsShared);

        verify(publishResultsDelegate).shareResults(sender, event, resultsShared, resultsShared.getVariantDirectory());

        verify(updateResultLineStatusDelegate).updateResultLineStatus(sender, event, resultsShared);
    }
}