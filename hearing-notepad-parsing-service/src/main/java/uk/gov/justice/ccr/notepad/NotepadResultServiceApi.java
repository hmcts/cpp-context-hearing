package uk.gov.justice.ccr.notepad;


import uk.gov.justice.ccr.notepad.process.Knowledge;
import uk.gov.justice.ccr.notepad.view.Part;
import uk.gov.justice.ccr.notepad.view.ResultDefinitionView;
import uk.gov.justice.ccr.notepad.view.ResultDefinitionViewBuilder;
import uk.gov.justice.ccr.notepad.view.ResultPromptViewBuilder;
import uk.gov.justice.ccr.notepad.view.parser.PartsResolver;
import uk.gov.justice.services.common.converter.LocalDates;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;
import javax.json.JsonObject;

@ServiceComponent(Component.QUERY_API)
public class NotepadResultServiceApi {

    @Inject
    private Enveloper enveloper;

    @Inject
    private ResultDefinitionViewBuilder resultDefinitionViewBuilder;

    @Inject
    private ResultPromptViewBuilder resultPromptViewBuilder;

    @Inject
    private ParsingFacade parsingFacade;


    @Inject
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;


    @Handles("hearing.notepad.parse-result-definition")
    public JsonEnvelope getResultDefinition(final JsonEnvelope envelope) throws ExecutionException {
        final JsonObject payload = envelope.payloadAsJsonObject();
        final LocalDate orderedDate = LocalDates.from(payload.getString("orderedDate"));
        lazyResultCacheLoad(envelope, orderedDate);
        final String originalText = payload.getString("originalText");
        List<Part> parts = new PartsResolver().getParts(originalText);
        final Knowledge knowledge = parsingFacade.processParts(parts, orderedDate);
        return enveloper.withMetadataFrom(envelope, "hearing.notepad.parse-result-definition-response")
                        .apply(objectToJsonObjectConverter.convert(buildResultDefinitionView(
                                        originalText, orderedDate.toString(), parts, knowledge)));
    }

    @Handles("hearing.notepad.parse-result-prompt")
    public JsonEnvelope getResultPrompt(final JsonEnvelope envelope) throws ExecutionException {
        final JsonObject payload = envelope.payloadAsJsonObject();
        final String resultCode = payload.getString("resultCode");
        final LocalDate orderedDate = LocalDates.from(payload.getString("orderedDate"));
        lazyResultCacheLoad(envelope, orderedDate);
        final Knowledge knowledge = parsingFacade.processPrompt(resultCode, orderedDate);
        return enveloper.withMetadataFrom(envelope, "hearing.notepad.parse-result-prompt-response")
                .apply(objectToJsonObjectConverter.convert(resultPromptViewBuilder.buildFromKnowledge(knowledge)));

    }

    private void lazyResultCacheLoad(final JsonEnvelope envelope, final LocalDate orderedDate) throws ExecutionException {
        parsingFacade.lazyLoad(envelope, orderedDate);
    }

    ResultDefinitionView buildResultDefinitionView(final String originalText,
                    final String orderedDate, final List<Part> parts, final Knowledge knowledge) {
        final ResultDefinitionView buildFromKnowledge = resultDefinitionViewBuilder.buildFromKnowledge(parts, knowledge);
        buildFromKnowledge.setOriginalText(originalText);
        buildFromKnowledge.setOrderedDate(orderedDate);
        return buildFromKnowledge;
    }

}
