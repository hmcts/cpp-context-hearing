package uk.gov.justice.ccr.notepad;


import org.apache.commons.collections.CollectionUtils;
import uk.gov.justice.ccr.notepad.process.ChildResultDefinitionDetail;
import uk.gov.justice.ccr.notepad.process.Knowledge;
import uk.gov.justice.ccr.notepad.result.cache.model.ResultDefinition;
import uk.gov.justice.ccr.notepad.view.*;
import uk.gov.justice.ccr.notepad.view.parser.PartsResolver;
import uk.gov.justice.services.common.converter.LocalDates;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.inject.Inject;
import javax.json.JsonObject;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;

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
    public JsonEnvelope getResultDefinition(final JsonEnvelope envelope) {
        final JsonObject payload = envelope.payloadAsJsonObject();
        final LocalDate orderedDate = LocalDates.from(payload.getString("orderedDate"));
        lazyResultCacheLoad(envelope, orderedDate);
        final String originalText = payload.getString("originalText");
        final List<Part> parts = new PartsResolver().getParts(originalText);
        final Knowledge knowledge = parsingFacade.processParts(parts, orderedDate);
        return enveloper.withMetadataFrom(envelope, "hearing.notepad.parse-result-definition-response")
                .apply(objectToJsonObjectConverter.convert(buildResultDefinitionView(
                        originalText, orderedDate, parts, knowledge)));
    }

    @Handles("hearing.notepad.parse-result-prompt")
    public JsonEnvelope getResultPrompt(final JsonEnvelope envelope) {
        final JsonObject payload = envelope.payloadAsJsonObject();
        final String resultCode = payload.getString("resultCode");
        final LocalDate orderedDate = LocalDates.from(payload.getString("orderedDate"));
        lazyResultCacheLoad(envelope, orderedDate);
        return enveloper.withMetadataFrom(envelope, "hearing.notepad.parse-result-prompt-response")
                .apply(objectToJsonObjectConverter.convert(getResultPromptChoices(resultCode, orderedDate)));

    }

    private ResultPromptView getResultPromptChoices(final String resultDefinitionId, final LocalDate orderedDate) {
        final Knowledge knowledge = parsingFacade.processPrompt(resultDefinitionId, orderedDate);
        return resultPromptViewBuilder.buildFromKnowledge(knowledge);
    }

    private void lazyResultCacheLoad(final JsonEnvelope envelope, final LocalDate orderedDate) {
        parsingFacade.lazyLoad(envelope, orderedDate);
    }

    ResultDefinitionView buildResultDefinitionView(final String originalText,
                                                   final LocalDate orderedDate, final List<Part> parts, final Knowledge knowledge) {
        List<ChildResultDefinition> childResultDefinitions = null;
        List<PromptChoice> promptChoices = null;
        final String resultDefinitionId = resultDefinitionViewBuilder.getResultDefinitionIdFromKnowledge(parts, knowledge);
        if (nonNull(resultDefinitionId)) {
            final ChildResultDefinitionDetail childResultDefinitionDetail = parsingFacade.retrieveChildResultDefinitionDetail(resultDefinitionId, orderedDate);
            if (canTransformChildResultDefinitionsView(childResultDefinitionDetail)) {
                childResultDefinitions = transformChildResultDefinitionsView(childResultDefinitionDetail.getResultDefinitions(), childResultDefinitionDetail.getParentResultDefinition().getChildResultDefinitions());
            }

            promptChoices = getResultPromptChoices(resultDefinitionId, orderedDate).getPromptChoices();
        }
        final ResultDefinition resultDefinition = parsingFacade.retrieveResultDefinitionById(resultDefinitionId, orderedDate);
        final Boolean excludedFromResults = getExcludedFromResultsFromResultDefinition(resultDefinition);
        final Boolean booleanResult = getBooleanResultFromResultDefinition(resultDefinition);
        final String label = nonNull(resultDefinition) ? resultDefinition.getLabel() : null;
        final ResultDefinitionView buildFromKnowledge = resultDefinitionViewBuilder.buildFromKnowledge(parts, knowledge, childResultDefinitions, excludedFromResults, booleanResult,label, promptChoices);
        buildFromKnowledge.setOriginalText(originalText);
        buildFromKnowledge.setOrderedDate(orderedDate.toString());
        if(nonNull(resultDefinition)){
            buildFromKnowledge.setShortCode(resultDefinition.getShortCode());
        }
        if (nonNull(resultDefinition)) {
            buildFromKnowledge.setResultDefinitionGroup(resultDefinition.getResultDefinitionGroup());
        }
        return buildFromKnowledge;
    }

    private boolean canTransformChildResultDefinitionsView(final ChildResultDefinitionDetail childResultDefinitionDetail) {
        return nonNull(childResultDefinitionDetail) && nonNull(childResultDefinitionDetail.getParentResultDefinition())
                && nonNull(childResultDefinitionDetail.getResultDefinitions())
                && nonNull(childResultDefinitionDetail.getParentResultDefinition().getChildResultDefinitions());
    }

    private Boolean getExcludedFromResultsFromResultDefinition(final ResultDefinition resultDefinition) {
        Boolean excludedFromResults = Boolean.FALSE;
        if(nonNull(resultDefinition) && nonNull(resultDefinition.getExcludedFromResults()) ) {
            excludedFromResults = resultDefinition.getExcludedFromResults();
        }
        return excludedFromResults;
    }

    private Boolean getBooleanResultFromResultDefinition(final ResultDefinition resultDefinition) {
        Boolean booleanResult = Boolean.FALSE;
        if(nonNull(resultDefinition) && nonNull(resultDefinition.getConditonalMandatory()) ) {
            booleanResult = resultDefinition.getConditonalMandatory();
        }
        return booleanResult;
    }

    private List<ChildResultDefinition> transformChildResultDefinitionsView(List<ResultDefinition> resultDefinitions, List<uk.gov.justice.ccr.notepad.result.cache.model.ChildResultDefinition> childResultDefinitions) {
        return resultDefinitions.stream()
                .map(resultDefinition -> {
                    final ChildResultDefinition childResultDefinition = new ChildResultDefinition();
                    childResultDefinition.setCode(resultDefinition.getId());
                    childResultDefinition.setLabel(resultDefinition.getLabel());
                    childResultDefinition.setShortCode(resultDefinition.getShortCode());
                    childResultDefinition.setRuleType(getRuleType(resultDefinition.getId(), childResultDefinitions));
                    childResultDefinition.setExcludedFromResults(resultDefinition.getExcludedFromResults());
                    if(CollectionUtils.isNotEmpty(resultDefinition.getChildResultDefinitions())) {
                        childResultDefinition.setChildResultCodes(resultDefinition.getChildResultDefinitions().stream().map(uk.gov.justice.ccr.notepad.result.cache.model.ChildResultDefinition ::getChildResultDefinitionId).collect(Collectors.toList()));
                    }
                    return childResultDefinition;
                })
                .collect(Collectors.toList());
    }

    private String getRuleType(String resultDefinitionId, List<uk.gov.justice.ccr.notepad.result.cache.model.ChildResultDefinition> childResultDefinitions) {
        return childResultDefinitions.stream()
                .filter(c -> c.getChildResultDefinitionId().toString().equals(resultDefinitionId))
                .map(uk.gov.justice.ccr.notepad.result.cache.model.ChildResultDefinition::getRuleType)
                .findFirst()
                .orElse(null);
    }

}
