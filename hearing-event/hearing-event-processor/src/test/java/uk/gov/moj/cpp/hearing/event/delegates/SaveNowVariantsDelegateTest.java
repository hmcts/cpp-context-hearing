package uk.gov.moj.cpp.hearing.event.delegates;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.nowsdomain.variants.Variant;
import uk.gov.moj.cpp.hearing.command.nowsdomain.variants.VariantKey;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.Nows;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.AllOf.allOf;
import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloper;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.metadata;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payloadIsJson;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.moj.cpp.hearing.event.NowsTemplates.basicNowsTemplate;
import static uk.gov.moj.cpp.hearing.event.NowsTemplates.resultsSharedTemplate;

@RunWith(MockitoJUnitRunner.class)
public class SaveNowVariantsDelegateTest {

    @Captor
    private ArgumentCaptor<JsonEnvelope> envelopeArgumentCaptor;

    @InjectMocks
    SaveNowVariantsDelegate saveNowVariantsDelegate;

    @Mock
    private Sender sender;

    @Spy
    private final Enveloper enveloper = createEnveloper();

    @Spy
    private final ObjectMapper objectMapper = new ObjectMapperProducer().objectMapper();

    @Spy
    @InjectMocks
    private final ObjectToJsonObjectConverter objectToJsonObjectConverter = new ObjectToJsonObjectConverter();

    @Mock
    private Nows2VariantTransform nows2VariantTransform;

    @Test
    public void saveNowsVariants() {

        final ResultsShared resultsShared = resultsSharedTemplate();

        final List<Nows> nows = basicNowsTemplate();

        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("hearing.results-shared"),
                objectToJsonObjectConverter.convert(resultsShared));

        final List<Variant> variants = Arrays.asList(
                Variant.variant().setKey(VariantKey.variantKey().setNowsTypeId(UUID.randomUUID())));

        when(nows2VariantTransform.toVariants(resultsShared.getHearingId(), nows, resultsShared.getSharedTime())).thenReturn(variants);

        saveNowVariantsDelegate.saveNowsVariants(sender, event, nows, resultsShared);

        verify(sender).send(envelopeArgumentCaptor.capture());

        final List<JsonEnvelope> outgoingMessages = envelopeArgumentCaptor.getAllValues();

        final JsonEnvelope updatedResultLinesMessage = outgoingMessages.get(0);

        assertThat(updatedResultLinesMessage, jsonEnvelope(
                metadata().withName("hearing.command.save-nows-variants"),
                payloadIsJson(allOf(
                        withJsonPath("$.hearingId", is(resultsShared.getHearing().getId().toString()))))
        ));
    }
}