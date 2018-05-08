package uk.gov.justice.ccr.notepad.result.loader;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.emptyList;
import static java.util.UUID.fromString;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
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

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Named;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonString;

@Named("readStoreResultLoader")
public class ReadStoreResultLoader implements ResultLoader {

    private static final String FIELD_WORD_GROUP = "wordGroup";

    @Inject
    private ResultingQueryService resultingQueryService;

    private JsonEnvelope jsonEnvelope;

    public void setJsonEnvelope(final JsonEnvelope jsonEnvelope) {
        this.jsonEnvelope = jsonEnvelope;
    }

    @Override
    public List<ResultDefinition> loadResultDefinition() {
        final List<ResultDefinition> resultDefinitions = newArrayList();
        resultingQueryService.getAllDefinitions(jsonEnvelope).payloadAsJsonObject().getJsonArray("resultDefinitions").getValuesAs(JsonObject.class)
                .forEach(jsonObjectResultDefinition ->
                        getKeywordsGroups(jsonObjectResultDefinition).forEach(keywords -> {
                                    final ResultDefinition resultDefinition = new ResultDefinition();
                                    resultDefinition.setId(jsonObjectResultDefinition.getString("id"));
                                    resultDefinition.setLabel(jsonObjectResultDefinition.getString("label").trim());
                                    resultDefinition.setShortCode(jsonObjectResultDefinition.getString("shortCode").toLowerCase().trim());
                                    resultDefinition.setLevel(jsonObjectResultDefinition.getString("level"));
                                    resultDefinition.setKeywords(keywords);

                                    resultDefinitions.add(resultDefinition);
                                }
                        ));

        return resultDefinitions;
    }

    private List<List<String>> getKeywordsGroups(final JsonObject resultDefinition) {
        if (!resultDefinition.containsKey("wordGroups")) {
            return emptyList();
        }

        return resultDefinition.getJsonArray("wordGroups").getValuesAs(JsonObject.class)
                .stream()
                .map(wordGroup -> wordGroup.getJsonArray(FIELD_WORD_GROUP).getValuesAs(JsonString.class)
                        .stream()
                        .map(word -> word.getString().toLowerCase())
                        .collect(toList()))
                .collect(toList());
    }

    @Override
    public List<ResultDefinitionSynonym> loadResultDefinitionSynonym() {
        final List<ResultDefinitionSynonym> resultDefinitionSynonyms = newArrayList();
        final JsonArray resultDefinitionSynonymsJson = resultingQueryService.getAllDefinitionKeywordSynonyms(jsonEnvelope).payloadAsJsonObject().getJsonArray("resultDefinitionKeywordSynonyms");
        resultDefinitionSynonymsJson.forEach(jsonValue -> {
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
        final JsonObject resultDefinitionsJson = resultingQueryService.getAllDefinitions(jsonEnvelope).payloadAsJsonObject();
        final Map<UUID, List<JsonObject>> resultPromptsByIdMap = resultDefinitionsJson.getJsonArray("resultDefinitions").getValuesAs(JsonObject.class)
                .stream()
                .filter(jsonObject -> jsonObject.containsKey("prompts"))
                .collect(toMap(jsonObject -> fromString(jsonObject.getString("id")),
                        jsonObject -> jsonObject.getJsonArray("prompts").getValuesAs(JsonObject.class)));

        resultPromptsByIdMap.forEach((id, prompts) ->
                prompts.forEach(promptJson -> {
                    ResultPrompt resultPrompt = new ResultPrompt();
                    resultPrompt.setId(promptJson.getString("id"));
                    resultPrompt.setResultDefinitionId(id);
                    resultPrompt.setLabel(promptJson.getString("label").trim());
                    String durationElement = promptJson.getString("duration", null);
                    if (durationElement != null && !durationElement.isEmpty()) {
                        resultPrompt.setType(DURATION);
                    } else {
                        resultPrompt.setType(valueOf(promptJson.getString("type").trim().toUpperCase()));
                    }
                    resultPrompt.setReference(promptJson.getString("reference", null));
                    resultPrompt.setPromptOrder(promptJson.getInt("sequence"));
                    resultPrompt.setMandatory(promptJson.getBoolean("mandatory"));
                    resultPrompt.setDurationElement(durationElement);
                    resultPrompt.setKeywords(getKeywordsForPrompts(promptJson));

                    final String fixedListId = promptJson.getString("fixedListId", null);
                    if (fixedListId != null && ResultType.FIXL == resultPrompt.getType()) {
                        resultPrompt.setFixedList(resultPromptFixedListMap.get(fixedListId.trim()));
                    }
                    resultPrompts.add(resultPrompt);

                }));
        return resultPrompts;
    }

    private List<String> getKeywordsForPrompts(final JsonObject resultPromptJson) {
        if (!resultPromptJson.containsKey(FIELD_WORD_GROUP)) {
            return emptyList();
        }

        return resultPromptJson.getJsonArray(FIELD_WORD_GROUP).getValuesAs(JsonString.class).stream()
                .map(word -> word.getString().toLowerCase())
                .collect(toList());
    }

    private Map<String, Set<String>> loadResultPromptFixedList() {
        return resultingQueryService.getAllPromptFixedLists(this.jsonEnvelope).payloadAsJsonObject()
                .getJsonArray("fixedListCollection").getValuesAs(JsonObject.class)
                .stream()
                .flatMap(fixedList -> fixedList.getJsonArray("elements").getValuesAs(JsonObject.class)
                        .stream()
                        .map(element -> {
                            final ResultPromptFixedList resultPromptFixedList = new ResultPromptFixedList();
                            resultPromptFixedList.setId(fixedList.getString("id").trim());
                            resultPromptFixedList.setValue(element.getString("value").trim());
                            return resultPromptFixedList;
                        })
                ).collect(groupingBy(ResultPromptFixedList::getId, mapping(ResultPromptFixedList::getValue, toCollection(TreeSet::new))));
    }

    @Override
    public List<ResultPromptSynonym> loadResultPromptSynonym() {
        final List<ResultPromptSynonym> resultPromptSynonyms = newArrayList();
        final JsonArray resultPromptSynonymsJson = resultingQueryService.getAllPromptKeywordSynonyms(jsonEnvelope).payloadAsJsonObject().getJsonArray("resultPromptKeywordSynonyms");
        resultPromptSynonymsJson.forEach(jsonValue -> {
            ResultPromptSynonym resultPromptSynonym = new ResultPromptSynonym();
            resultPromptSynonym.setWord(((JsonObject) jsonValue).getString("word").replaceAll(" ", "").trim().toLowerCase());
            resultPromptSynonym.setSynonym(((JsonObject) jsonValue).getString("synonym").trim().toLowerCase());
            resultPromptSynonyms.add(resultPromptSynonym);
        });
        return resultPromptSynonyms;
    }
}
