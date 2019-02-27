package uk.gov.moj.cpp.hearing.event.delegates;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloper;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.metadata;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payloadIsJson;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.moj.cpp.hearing.event.NowsTemplates.resultsSharedTemplate;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.ObjectConverters.asPojo;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.VariantDirectoryTemplates.standardVariantTemplate;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.print;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.test.matchers.ElementAtListMatcher.first;

import uk.gov.justice.core.courts.Now;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.nowsdomain.variants.ResultLineReference;
import uk.gov.moj.cpp.hearing.command.nowsdomain.variants.SaveNowsVariantsCommand;
import uk.gov.moj.cpp.hearing.command.nowsdomain.variants.Variant;
import uk.gov.moj.cpp.hearing.command.nowsdomain.variants.VariantKey;
import uk.gov.moj.cpp.hearing.command.nowsdomain.variants.VariantValue;
import uk.gov.moj.cpp.hearing.test.CommandHelpers;

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SaveNowVariantsDelegateTestInternal {

    @Spy
    private final Enveloper enveloper = createEnveloper();
    @Spy
    private final ObjectMapper objectMapper = new ObjectMapperProducer().objectMapper();
    @Spy
    @InjectMocks
    private final ObjectToJsonObjectConverter objectToJsonObjectConverter = new ObjectToJsonObjectConverter();
    @InjectMocks
    SaveNowVariantsDelegate saveNowVariantsDelegate;
    @Captor
    private ArgumentCaptor<JsonEnvelope> envelopeArgumentCaptor;
    @Mock
    private Sender sender;
    @Mock
    private Nows2VariantTransform nows2VariantTransform;

    @Test
    public void saveNowsVariants() {

        final CommandHelpers.ResultsSharedEventHelper resultsShared = h(resultsSharedTemplate());

        final List<Now> nows = emptyList();

        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("hearing.results-shared"),
                objectToJsonObjectConverter.convert(resultsShared.it()));

        final List<Variant> variants = singletonList(
                standardVariantTemplate(randomUUID(), resultsShared.getHearingId(), resultsShared.getFirstDefendant().getId())
        );

        when(nows2VariantTransform.toVariants(resultsShared.getHearingId(), nows, resultsShared.it().getSharedTime())).thenReturn(variants);

        saveNowVariantsDelegate.saveNowsVariants(sender, event, nows, resultsShared.it());

        verify(sender).send(envelopeArgumentCaptor.capture());

        final JsonEnvelope updatedResultLinesMessage = envelopeArgumentCaptor.getValue();

        assertThat(updatedResultLinesMessage, jsonEnvelope(metadata().withName("hearing.command.save-nows-variants"), payloadIsJson(print())));

        assertThat(asPojo(updatedResultLinesMessage, SaveNowsVariantsCommand.class), isBean(SaveNowsVariantsCommand.class)
                .with(SaveNowsVariantsCommand::getHearingId, is(resultsShared.getHearingId()))
                .with(SaveNowsVariantsCommand::getVariants, first(isBean(Variant.class)
                        .with(Variant::getKey, isBean(VariantKey.class)
                                .with(VariantKey::getHearingId, is(resultsShared.getHearingId()))
                                .with(VariantKey::getDefendantId, is(resultsShared.getFirstDefendant().getId()))
                                .with(VariantKey::getNowsTypeId, is(variants.get(0).getKey().getNowsTypeId()))
                                .with(VariantKey::getUsergroups, containsInAnyOrder(variants.get(0).getKey().getUsergroups().toArray()))
                        )
                        .with(Variant::getValue, isBean(VariantValue.class)
                                .with(VariantValue::getMaterialId, is(variants.get(0).getValue().getMaterialId()))
                                .with(VariantValue::getResultLines, first(isBean(ResultLineReference.class)
                                        .with(ResultLineReference::getResultLineId, is(variants.get(0).getValue().getResultLines().get(0).getResultLineId()))
                                        .with(ResultLineReference::getLastSharedTime, is(variants.get(0).getValue().getResultLines().get(0).getLastSharedTime()))
                                ))
                        )
                ))
        );
    }
}