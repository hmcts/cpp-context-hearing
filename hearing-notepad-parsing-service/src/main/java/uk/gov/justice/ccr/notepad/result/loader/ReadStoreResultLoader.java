package uk.gov.justice.ccr.notepad.result.loader;


import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.groupingBy;
import static uk.gov.justice.ccr.notepad.result.cache.model.ResultType.DURATION;
import static uk.gov.justice.ccr.notepad.result.cache.model.ResultType.valueOf;

import uk.gov.justice.ccr.notepad.result.cache.model.ResultDefinition;
import uk.gov.justice.ccr.notepad.result.cache.model.ResultDefinitionSynonym;
import uk.gov.justice.ccr.notepad.result.cache.model.ResultPrompt;
import uk.gov.justice.ccr.notepad.result.cache.model.ResultPromptFixedList;
import uk.gov.justice.ccr.notepad.result.cache.model.ResultPromptSynonym;
import uk.gov.justice.ccr.notepad.result.cache.model.ResultType;
import uk.gov.justice.ccr.notepad.service.ResultingQueryService;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;
import javax.json.JsonArray;
import javax.json.JsonObject;

@Named("readStoreResultLoader")
public class ReadStoreResultLoader implements ResultLoader {

    @Inject
    private ResultingQueryService resultingQueryService;

    private JsonEnvelope jsonEnvelope;

    private static final Pattern COMMA_SPLITTER = Pattern.compile(",");

    public void setJsonEnvelope(final JsonEnvelope jsonEnvelope) {
        this.jsonEnvelope = jsonEnvelope;
    }

    @Override
    public List<ResultDefinition> loadResultDefinition() {
        final List<ResultDefinition> resultDefinitions = newArrayList();
        final JsonArray resultDefinitionsJson = resultingQueryService.getAllDefinitions(jsonEnvelope).payloadAsJsonObject().getJsonArray("resultDefinitions");
        resultDefinitionsJson.stream().forEach(jsonValue -> {
            ResultDefinition resultDefinition = new ResultDefinition();
            resultDefinition.setId(((JsonObject) jsonValue).getString("id"));
            resultDefinition.setLabel(((JsonObject) jsonValue).getString("label").trim());
            resultDefinition.setShortCode(((JsonObject) jsonValue).getString("shortCode").toLowerCase().trim());
            resultDefinition.setLevel(((JsonObject) jsonValue).getString("level"));
            resultDefinition.setKeywords(Arrays.asList(COMMA_SPLITTER.split(((JsonObject) jsonValue).getString("keywords").replaceAll(" ", "").toLowerCase())));
            resultDefinitions.add(resultDefinition);
        });
        return resultDefinitions;
    }


    @Override
    public List<ResultDefinitionSynonym> loadResultDefinitionSynonym() {
        final List<ResultDefinitionSynonym> resultDefinitionSynonyms = newArrayList();
        final JsonArray resultDefinitionSynonymsJson = resultingQueryService.getAllDefinitionKeywordSynonyms(jsonEnvelope).payloadAsJsonObject().getJsonArray("resultDefinitionKeywordSynonyms");
        resultDefinitionSynonymsJson.stream().forEach(jsonValue -> {
            ResultDefinitionSynonym resultDefinitionSynonym = new ResultDefinitionSynonym();
            resultDefinitionSynonym.setWord(((JsonObject) jsonValue).getString("word").replaceAll(" ", "").trim().toLowerCase());
            resultDefinitionSynonym.setSynonym(((JsonObject) jsonValue).getString("synonym").trim().toLowerCase());
            resultDefinitionSynonyms.add(resultDefinitionSynonym);
        });
        return resultDefinitionSynonyms;
    }

    @Override
    public List<ResultPrompt> loadResultPrompt() {
        final List<ResultPrompt> resultPrompts = newArrayList();
        final Map<String, Set<String>> resultPromptFixedListMap = loadResultPromptFixedList();

        final JsonArray resultPromptsJson = resultingQueryService.getAllPrompts(jsonEnvelope).payloadAsJsonObject().getJsonArray("resultPrompts");
        resultPromptsJson.stream().forEach(jsonValue -> {
            ResultPrompt resultPrompt = new ResultPrompt();
            resultPrompt.setId(((JsonObject) jsonValue).getString("id"));
            resultPrompt.setLabel(((JsonObject) jsonValue).getString("label").trim());
            resultPrompt.setResultDefinitionLabel(((JsonObject) jsonValue).getString("resultDefinitionLabel").trim());
            String durationElement = ((JsonObject) jsonValue).getString("durationElement").trim();
            if (!durationElement.isEmpty()) {
                resultPrompt.setType(DURATION);
            } else {
                resultPrompt.setType(valueOf(((JsonObject) jsonValue).getString("promptType").trim().toUpperCase()));
            }
            resultPrompt.setMandatory(((JsonObject) jsonValue).getString("mandatory"));
            resultPrompt.setDurationElement(durationElement);
            resultPrompt.setKeywords(Arrays.asList(COMMA_SPLITTER.split(((JsonObject) jsonValue).getString("keywords").replaceAll(" ", "").toLowerCase())));
            String fixedListId = ((JsonObject) jsonValue).getString("fixedListId").trim();
            if (fixedListId != null && ResultType.FIXL == resultPrompt.getType()) {
                resultPrompt.setFixedList(resultPromptFixedListMap.get(fixedListId));
            }
            resultPrompts.add(resultPrompt);
        });
        return resultPrompts;
    }

    private Map<String, Set<String>> loadResultPromptFixedList() {
        final JsonArray resultPromptFixedListsJson = resultingQueryService.getAllPromptFixedLists(jsonEnvelope).payloadAsJsonObject().getJsonArray("resultPromptFixedLists");
        List<ResultPromptFixedList> resultPromptFixedLists = newArrayList();
        resultPromptFixedListsJson.stream().forEach(jsonValue -> {
            ResultPromptFixedList resultPromptFixedList = new ResultPromptFixedList();
            resultPromptFixedList.setId(((JsonObject) jsonValue).getString("fixedListId").trim());
            resultPromptFixedList.setValue(((JsonObject) jsonValue).getString("value").trim());
            resultPromptFixedLists.add(resultPromptFixedList);
        });

        return resultPromptFixedLists.stream().collect(groupingBy(ResultPromptFixedList::getId, Collectors.mapping(ResultPromptFixedList::getValue, Collectors.toCollection(TreeSet::new))));
    }

    @Override
    public List<ResultPromptSynonym> loadResultPromptSynonym() {
        final List<ResultPromptSynonym> resultPromptSynonyms = newArrayList();
        final JsonArray resultPromptSynonymsJson = resultingQueryService.getAllPromptKeywordSynonyms(jsonEnvelope).payloadAsJsonObject().getJsonArray("resultPromptKeywordSynonyms");
        resultPromptSynonymsJson.stream().forEach(jsonValue -> {
            ResultPromptSynonym resultPromptSynonym = new ResultPromptSynonym();
            resultPromptSynonym.setWord(((JsonObject) jsonValue).getString("word").replaceAll(" ", "").trim().toLowerCase());
            resultPromptSynonym.setSynonym(((JsonObject) jsonValue).getString("synonym").trim().toLowerCase());
            resultPromptSynonyms.add(resultPromptSynonym);
        });
        return resultPromptSynonyms;
    }
}
