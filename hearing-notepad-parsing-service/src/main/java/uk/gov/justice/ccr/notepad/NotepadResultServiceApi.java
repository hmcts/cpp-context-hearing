package uk.gov.justice.ccr.notepad;


import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;

import uk.gov.justice.ccr.notepad.process.Knowledge;
import uk.gov.justice.ccr.notepad.view.Part;
import uk.gov.justice.ccr.notepad.view.ResultDefinitionView;
import uk.gov.justice.ccr.notepad.view.ResultDefinitionViewBuilder;
import uk.gov.justice.ccr.notepad.view.ResultPromptViewBuilder;
import uk.gov.justice.ccr.notepad.view.parser.PartsResolver;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.messaging.JsonEnvelope;

@ServiceComponent(Component.QUERY_API)
public class NotepadResultServiceApi {

    @Inject
    Enveloper enveloper;

    @Inject
    ResultDefinitionViewBuilder resultDefinitionViewBuilder;

    @Inject
    ResultPromptViewBuilder resultPromptViewBuilder;

    @Inject
    ParsingFacade parsingFacade;


    @Inject
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;


    @Handles("hearing.notepad.parse-result-definition")
    public JsonEnvelope getResultDefinition(final JsonEnvelope envelope) throws ExecutionException {
        lazyResultCacheLoad(envelope);
        final String originalText = envelope.payloadAsJsonObject().getString("originalText");
        // Use current date in case UI does not send orderedDate for the first time, so it can be
        // updated later by UI
        final String orderedDate = envelope.payloadAsJsonObject().getString("orderedDate",
                        LocalDate.now().toString());
        final List<Part> parts = new PartsResolver().getParts(originalText);
        final Knowledge knowledge = parsingFacade.processParts(parts);
        return enveloper.withMetadataFrom(envelope, "hearing.notepad.parse-result-definition-response")
                        .apply(objectToJsonObjectConverter.convert(buildResultDefinitionView(
                                        originalText, orderedDate, parts, knowledge)));
    }

    @Handles("hearing.notepad.parse-result-prompt")
    public JsonEnvelope getResultPrompt(final JsonEnvelope envelope) throws ExecutionException {
        lazyResultCacheLoad(envelope);
        final String resultCode = envelope.payloadAsJsonObject().getString("resultCode");
        final Knowledge knowledge = parsingFacade.processPrompt(resultCode);
        return enveloper.withMetadataFrom(envelope, "hearing.notepad.parse-result-prompt-response")
                .apply(objectToJsonObjectConverter.convert(resultPromptViewBuilder.buildFromKnowledge(knowledge)));

    }

    private void lazyResultCacheLoad(final JsonEnvelope envelope) throws ExecutionException {
        parsingFacade.lazyLoad(envelope);
    }

    ResultDefinitionView buildResultDefinitionView(final String originalText,
                    final String orderedDate, final List<Part> parts, final Knowledge knowledge) {
        final ResultDefinitionView buildFromKnowledge = resultDefinitionViewBuilder.buildFromKnowledge(parts, knowledge);
        buildFromKnowledge.setOriginalText(originalText);
        buildFromKnowledge.setOrderedDate(orderedDate);
        return buildFromKnowledge;
    }

}
