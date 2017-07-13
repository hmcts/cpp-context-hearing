package uk.gov.justice.ccr.notepad;


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

import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;

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
        String originalText = envelope.payloadAsJsonObject().getString("originalText");
        List<Part> parts = new PartsResolver().getParts(originalText);
        Knowledge knowledge = parsingFacade.processParts(parts);
        return enveloper.withMetadataFrom(envelope, "hearing.notepad.parse-result-definition-response")
                .apply(objectToJsonObjectConverter.convert(buildResultDefinitionView(originalText, parts, knowledge)));
    }

    @Handles("hearing.notepad.parse-result-prompt")
    public JsonEnvelope getResultPrompt(final JsonEnvelope envelope) throws ExecutionException {
        lazyResultCacheLoad(envelope);
        String resultCode = envelope.payloadAsJsonObject().getString("resultCode");
        Knowledge knowledge = parsingFacade.processPrompt(resultCode);
        return enveloper.withMetadataFrom(envelope, "hearing.notepad.parse-result-prompt-response")
                .apply(objectToJsonObjectConverter.convert(resultPromptViewBuilder.buildFromKnowledge(knowledge)));

    }

    private void lazyResultCacheLoad(final JsonEnvelope envelope) throws ExecutionException {
        parsingFacade.lazyLoad(envelope);
    }

    ResultDefinitionView buildResultDefinitionView(final String originalText, final List<Part> parts, final Knowledge knowledge) {
        ResultDefinitionView buildFromKnowledge = resultDefinitionViewBuilder.buildFromKnowledge(parts, knowledge);
        buildFromKnowledge.setOriginalText(originalText);
        return buildFromKnowledge;
    }

}
